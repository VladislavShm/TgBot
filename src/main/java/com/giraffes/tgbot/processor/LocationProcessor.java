package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.service.TelegramSenderService;
import com.giraffes.tgbot.service.TgUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class LocationProcessor {
    @Autowired
    protected TelegramSenderService telegramSenderService;

    public abstract Location getLocation();

    public Location process(Update update, boolean redirected) {
        return processText(TgUserService.getCurrentUser(), update.getMessage().getText(), redirected);
    }

    protected Location processText(TgUser user, String text, boolean redirected) {
        throw new RuntimeException("Location processor " + getLocation() + " doesn't support text messages");
    }
}
