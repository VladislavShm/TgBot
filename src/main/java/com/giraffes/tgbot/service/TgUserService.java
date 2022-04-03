package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.repository.TgUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgUserService {
    private final TgUserRepository tgUserRepository;
    private final ThreadLocal<Boolean> USER_JUST_CREATED = new ThreadLocal<>();
    private final ThreadLocal<TgUser> CURRENT_USER = new ThreadLocal<>();

    public void authenticateUser(User user, String chatId) {
        TgUser tgUser = tgUserRepository.findByName(user.getUserName());
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
    }

    private TgUser createUser(String username, String firstName, String lastName, String chatId) {
        log.info("Creating new user: {}, {}, {}, {}", username, firstName, lastName, chatId);

        TgUser tgUser = new TgUser();
        tgUser.setName(username);
        tgUser.setFirstName(firstName);
        tgUser.setLastName(lastName);
        tgUser.setChatId(chatId);
        tgUser = tgUserRepository.save(tgUser);
        log.info("New user was created: {}", tgUser);

        return tgUser;
    }

    public boolean isUserJustCreated() {
        return USER_JUST_CREATED.get();
    }

    public TgUser getCurrentUser() {
        return CURRENT_USER.get();
    }
}
