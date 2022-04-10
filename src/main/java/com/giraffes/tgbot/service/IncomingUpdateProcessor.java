package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import com.giraffes.tgbot.processor.LocationProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IncomingUpdateProcessor {
    private final TgUserService tgUserService;
    private final Map<UserLocation, LocationProcessor> processors;

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
            UserLocation currentUserLocation = user.getLocation();
            UserLocation newUserLocation = processors.get(currentUserLocation).process(update, false);
            if (currentUserLocation != newUserLocation) {
                user.setLocation(newUserLocation);
                processors.get(newUserLocation).process(update, true);
            }

        } else {
            log.warn("Received an update without a message from {}: {}", user, update);
        }

        log.info("Updated was successfully processed.");
    }
}
