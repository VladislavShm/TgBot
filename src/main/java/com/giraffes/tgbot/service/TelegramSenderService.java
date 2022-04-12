package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@RequiredArgsConstructor
public class TelegramSenderService {
    private final AbsSender tgSender;

    public void send(String text, ReplyKeyboard keyboard) {
        send(text, keyboard, TgUserService.getCurrentUser().getChatId());
    }

    @SneakyThrows
    public void send(String text, ReplyKeyboard keyboard, String chatId) {
        tgSender.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .replyMarkup(keyboard)
                        .parseMode("html")
                        .text(text)
                        .build()
        );
    }
}
