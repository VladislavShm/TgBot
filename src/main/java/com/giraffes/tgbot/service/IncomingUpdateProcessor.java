package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.processor.LocationProcessor;
import com.giraffes.tgbot.property.BotProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IncomingUpdateProcessor {
    private final BotProperties botProperties;
    private final TgUserService tgUserService;
    private final TgGroupService tgGroupService;
    private final Map<Location, LocationProcessor> processors;

    public IncomingUpdateProcessor(TgUserService tgUserService, List<LocationProcessor> locationProcessors,
                                   BotProperties botProperties, TgGroupService tgGroupService) {
        this.tgUserService = tgUserService;
        this.processors = locationProcessors.stream().collect(
                Collectors.toMap(LocationProcessor::getLocation, processor -> processor)
        );
        this.tgGroupService = tgGroupService;
        this.botProperties = botProperties;
    }

    @SneakyThrows
    @Transactional
    public void process(Update update) {
        log.info("Processing request: {}", update);

        if (update.hasMessage()) {
            processMessageUpdate(update);
        } else if (update.hasMyChatMember()) {
            processMyChatMemberUpdate(update);
        } else {
            log.warn("Update is unsupported: {}", update);
        }

        log.info("Updated has been processed.");
    }

    private void processMyChatMemberUpdate(Update update) {
        Optional<ChatMemberUpdated> myChatMember = Optional.of(update.getMyChatMember());

        Optional<String> chatType =
                myChatMember
                        .map(ChatMemberUpdated::getChat)
                        .map(Chat::getType);

        if (chatType.filter("group"::equals).isPresent()) {
            processGroupMemberStatusChangeEvent(update, myChatMember);
        } else if (chatType.filter("private"::equals).isPresent()) {
            processPrivateMemberStatusChangeEvent(update);
        } else {
            log.warn("Unsupported MyChatMember chat type: {}", chatType.orElse(null));
        }
    }

    private void processGroupMemberStatusChangeEvent(Update update, Optional<ChatMemberUpdated> myChatMember) {
        myChatMember
                .map(ChatMemberUpdated::getNewChatMember)
                .map(ChatMember::getUser)
                .map(User::getUserName)
                .filter(username -> username.equals(botProperties.getBotUserName()))
                .ifPresentOrElse(
                        (v) -> tgGroupService.registerOrUpdateGroup(update),
                        () -> log.info("Unsupported MyChatMember group event")
                );
    }

    private void processPrivateMemberStatusChangeEvent(Update update) {
        TgUser user = tgUserService.authenticateUser(update);
        Optional<String> newStatus =
                Optional.of(update.getMyChatMember())
                        .map(ChatMemberUpdated::getNewChatMember)
                        .map(ChatMember::getStatus);

        if (newStatus.filter("kicked"::equals).isPresent()) {
            tgUserService.onUserBecomeKicked(user);
        } else if (newStatus.filter("member"::equals).isPresent()) {
            tgUserService.onUserBecomeMember(user);
        } else {
            log.warn("Unsupported new member status: {}", newStatus.orElse(null));
        }
    }

    private void processMessageUpdate(Update update) {
        Optional<String> chatType =
                Optional.of(update.getMessage())
                        .map(Message::getChat)
                        .map(Chat::getType);

        if (chatType.filter("group"::equals).isPresent()) {
            log.info("Skipping Message update on group activity");
        } else if (chatType.filter("private"::equals).isPresent()) {
            TgUser user = tgUserService.authenticateUser(update);
            Optional.ofNullable(processors.get(user.getLocation()))
                    .map((processor) -> processor.process(update, false))
                    .ifPresent((location -> {
                        Optional<Location> newLocation = location;
                        while (newLocation.filter(l -> l != user.getLocation()).isPresent()) {
                            user.setLocation(newLocation.get());
                            newLocation = processors.get(user.getLocation()).process(update, true);
                        }
                    }));
        } else {
            log.warn("Unsupported Message chat type: {}", chatType.orElse(null));
        }
    }
}
