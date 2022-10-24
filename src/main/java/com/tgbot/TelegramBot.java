package com.tgbot;

import com.tgbot.property.BotProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramWebhookBot {
    private final BotProperties botProperties;

    @Override
    public String getBotUsername() {
        return botProperties.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getBotToken();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotPath() {
        return null;
    }
}
