package com.giraffes.tgbot;

import com.giraffes.tgbot.property.BotProperties;
import com.giraffes.tgbot.service.IncomingUpdateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class GiraffeTelegramBot extends TelegramWebhookBot {
    private final BotProperties botProperties;

    @Autowired
    private IncomingUpdateProcessor incomingUpdateProcessor;

    public GiraffeTelegramBot(BotProperties botProperties) {
        super(createBotOptions(botProperties));
        this.botProperties = botProperties;
    }

    private static DefaultBotOptions createBotOptions(BotProperties botProperties) {
        DefaultBotOptions options = new DefaultBotOptions();
//        options.setProxyHost(botProperties.getProxyHost());
//        options.setProxyPort(botProperties.getProxyPort());
//        options.setProxyType(botProperties.getProxyType());
        return options;
    }

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
        return incomingUpdateProcessor.process(update);
    }

    @Override
    public String getBotPath() {
        return botProperties.getWebHookPath();
    }
}
