package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.UserLocation;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface LocationProcessor {
    UserLocation getLocation();
    UserLocation process(Update update, boolean redirected);
}
