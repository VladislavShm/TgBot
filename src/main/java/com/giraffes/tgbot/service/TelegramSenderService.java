package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class TelegramSenderService {
    private final AbsSender tgSender;

    /**
     * Use a method with an explicit TgUser / Chat ID parameter
     */
    @Deprecated
    public void send(String text, ReplyKeyboard keyboard) {
        send(text, keyboard, TgUserService.getCurrentUser());
    }

    public void send(String text, ReplyKeyboard keyboard, TgUser user) {
        send(text, keyboard, user.getChatId());
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

    @SneakyThrows
    public void sendImage(byte[] image, String imageName, ReplyKeyboard keyboard) {
        tgSender.execute(
                SendPhoto.builder()
                        .photo(new InputFile(new ByteArrayInputStream(image), imageName))
                        .chatId(TgUserService.getCurrentUser().getChatId())
                        .replyMarkup(keyboard)
                        .parseMode("html")
                        .build()
        );
    }
}
