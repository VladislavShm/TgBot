package com.giraffes.tgbot.controller;

import com.giraffes.tgbot.service.IncomingUpdateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebHookController {
    private final IncomingUpdateProcessor incomingUpdateProcessor;

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        try {
            return incomingUpdateProcessor.process(update);
        } catch (Exception e) {
            log.error("Error happened while processing: {}. ", update, e);
        }

        log.warn("Unexpected situation. Return null!");
        return null;
    }
}