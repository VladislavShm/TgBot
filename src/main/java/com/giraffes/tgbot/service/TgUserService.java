package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.model.ParticipantInvites;
import com.giraffes.tgbot.property.BotProperties;
import com.giraffes.tgbot.repository.TgUserRepository;
import com.giraffes.tgbot.utils.TelegramUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgUserService {
    private final TgUserRepository tgUserRepository;
    private final BotProperties botProperties;

    private static final ThreadLocal<Boolean> USER_JUST_CREATED = new ThreadLocal<>();
    private static final ThreadLocal<TgUser> CURRENT_USER = new ThreadLocal<>();

    public String createInvitationLink(TgUser tgUser) {
        String uniqueCode = new String(Base64.getEncoder().encode(("base64" + tgUser.getId()).getBytes(StandardCharsets.UTF_8)));
        return "https://t.me/" + botProperties.getBotUserName() + "?start=" + uniqueCode;
    }

    public TgUser authenticateUser(Update update) {
        User user = TelegramUtils.extractUser(update);
        String chatId = TelegramUtils.determineChatId(update);
        TgUser tgUser = tgUserRepository.findByChatId(chatId);
        if (tgUser == null) {
            tgUser = createUser(
                    user.getUserName(),
                    user.getFirstName(),
                    user.getLastName(),
                    chatId
            );

            USER_JUST_CREATED.set(true);
        } else {
            USER_JUST_CREATED.set(false);
        }

        CURRENT_USER.set(tgUser);
        return tgUser;
    }

    private TgUser createUser(String username, String firstName, String lastName, String chatId) {
        log.info("Creating new user: {}, {}, {}, {}", username, firstName, lastName, chatId);

        TgUser tgUser = new TgUser();
        tgUser.setName(username);
        tgUser.setFirstName(firstName);
        tgUser.setLastName(lastName);
        tgUser.setChatId(chatId);
        tgUser.setLocation(Location.BASE);
        tgUser = tgUserRepository.save(tgUser);
        log.info("New user was created: {}", tgUser);

        return tgUser;
    }

    public void onUserBecomeKicked(TgUser user) {
        user.setKicked(true);
        log.debug("User with chat ID = {} became kicked", user.getChatId());
    }

    public void onUserBecomeMember(TgUser user) {
        user.setKicked(false);
        log.debug("User with chat ID = {} became member", user.getChatId());
    }

    public TgUser findByUsername(String username) {
        return this.tgUserRepository.findByName(username);
    }

    public String topParticipants() {
        List<ParticipantInvites> topParticipants = tgUserRepository.top10Participants();
        return topParticipants.stream().map(x -> x.getName() + " - " + x.getInvites()).collect(Collectors.joining( "\n"));
    }

    public TgUser findByChatId(String chatId) {
        return this.tgUserRepository.findByChatId(chatId);
    }

    public static boolean isUserJustCreated() {
        return USER_JUST_CREATED.get();
    }

    public static TgUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public Integer invitedCount(TgUser tgUser) {
        return tgUserRepository.invitedCount(tgUser);
    }

    public Optional<TgUser> findById(Long inviterId) {
        return tgUserRepository.findById(inviterId);
    }

    public List<String> queryAllChatIds() {
        return tgUserRepository.queryAllChatIds();
    }
}
