package com.giraffes.tgbot.service;

import com.giraffes.tgbot.screen.ScreenProcessor;
import com.giraffes.tgbot.utils.CollectorsUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomingUpdateProcessor {
    private final TgUserService tgUserService;

    @Autowired
    private List<ScreenProcessor> screenProcessors;

    @SneakyThrows
    @Transactional
    public BotApiMethod<?> process(Update update) {
        log.info("Processing request: {}", update);
        String chatId = determineChatId(update);
        tgUserService.authenticateUser(extractUser(update), chatId);
        log.info("Updated request has been successfully processed");

        BotApiMethod<? extends Serializable> result = processUpdate(update, chatId);

        log.info("Updated was successfully processed.");
        return result;
    }

    private BotApiMethod<? extends Serializable> processUpdate(Update update, String chatId) {
        if (update.hasMessage()) {
            return processIncomingMessage(update, chatId);
        } else {
            return processIncomingAction(update, chatId);
        }
    }

    private User extractUser(Update update) {
        User from;
        if (update.hasMessage()) {
            from = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            from = update.getCallbackQuery().getFrom();
        } else {
            throw new RuntimeException("Can not determine user.");
        }

        return from;
    }

    private String determineChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }

        throw new RuntimeException("Can not determine chat id.");
    }

    private BotApiMethod<? extends Serializable> processIncomingMessage(Update update, String chatId) {
        Message message = update.getMessage();
        String text = message.getText();
        log.info("Text: {}", text);

        return screenProcessors.stream().filter(sp -> sp.shouldProcessIncomingMessage(message, text))
                .collect(CollectorsUtils.zeroOrOne())
                .map(sp -> sp.processIncomingMessage(text, chatId, message))
                .orElse(null);
    }

    private BotApiMethod<? extends Serializable> processIncomingAction(Update update, String chatId) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        log.info("Action: {}", data);

        return screenProcessors.stream().filter(sp -> sp.shouldProcessIncomingAction(data))
                .collect(CollectorsUtils.zeroOrOne())
                .map(sp -> sp.processIncomingAction(update, chatId, data))
                .orElseThrow(() -> new RuntimeException("Can not determine action processor"));
    }
}
