package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import com.giraffes.tgbot.service.TgUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public abstract class LocationProcessor {
    @Autowired
    AbsSender tgSender;

    public abstract UserLocation getLocation();

    public UserLocation process(Update update, boolean redirected) {
        return processText(TgUserService.getCurrentUser(), update.getMessage().getText(), redirected);
    }

    UserLocation processText(TgUser user, String text, boolean redirected) {
        throw new RuntimeException("Location processor " + getLocation() + " doesn't support text messages");
    }

}
