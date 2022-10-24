package com.tgbot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@UtilityClass
public class TelegramUtils {
    public static User extractUser(Update update) {
        User from;
        if (update.hasMessage()) {
            from = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            from = update.getCallbackQuery().getFrom();
        } else if (update.hasMyChatMember()) {
            from = update.getMyChatMember().getFrom();
        } else {
            throw new RuntimeException("Can not determine user.");
        }

        return from;
    }

    public static String determineChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        } else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getChat().getId().toString();
        }

        throw new RuntimeException("Can not determine chat id.");
    }
}
