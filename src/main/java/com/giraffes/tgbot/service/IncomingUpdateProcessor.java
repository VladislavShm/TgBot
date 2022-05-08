package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.processor.LocationProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IncomingUpdateProcessor {
    private final TgUserService tgUserService;
    private final Map<Location, LocationProcessor> processors;

    public IncomingUpdateProcessor(TgUserService tgUserService, List<LocationProcessor> locationProcessors) {
        this.tgUserService = tgUserService;
        this.processors = locationProcessors.stream().collect(
                Collectors.toMap(LocationProcessor::getLocation, processor -> processor)
        );
    }

    @SneakyThrows
    @Transactional
    public void process(Update update) {
        log.info("Processing request: {}", update);
        TgUser user = tgUserService.authenticateUser(update);
        log.info("Updated request has been successfully processed");

        if (update.hasMessage()) {
            processIncomingMessageUpdate(update, user);
        } else if (update.hasMyChatMember()) {
            processIncomingMemberStatusChangeEvent(update, user);
        } else {
            log.warn("Received an update without a message from {}: {}", user, update);
        }

        log.info("Updated was successfully processed.");
    }

    private void processIncomingMemberStatusChangeEvent(Update update, TgUser user) {
        Optional<String> newStatus = Optional.ofNullable(update.getMyChatMember())
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

    private void processIncomingMessageUpdate(Update update, TgUser user) {
        Optional.ofNullable(processors.get(user.getLocation()))
                .map((processor) -> processor.process(update, false))
                .ifPresent((location -> {
                    Optional<Location> newLocation = location;
                    while (newLocation.filter(l -> l != user.getLocation()).isPresent()) {
                        user.setLocation(newLocation.get());
                        newLocation = processors.get(user.getLocation()).process(update, true);
                    }
                }));
    }
}
