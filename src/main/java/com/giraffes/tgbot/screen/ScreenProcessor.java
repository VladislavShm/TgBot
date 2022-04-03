package com.giraffes.tgbot.screen;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

public interface ScreenProcessor {
    boolean shouldProcessIncomingMessage(Message message, String text);
    BotApiMethod<?> processIncomingMessage(String text, String chatId, Message message);

    boolean shouldProcessIncomingAction(String data);
    BotApiMethod<? extends Serializable> processIncomingAction(Update update, String chatId, String data);
}