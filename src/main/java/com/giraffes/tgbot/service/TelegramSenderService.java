package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgGroup;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramSenderService {
    private final MessageSource messageSource;
    private final AbsSender tgSender;

    @Deprecated
    public void send(String text, ReplyKeyboard keyboard) {
        send(text, keyboard, TgUserService.getCurrentUser());
    }

    @Deprecated
    public void send(String text, ReplyKeyboard keyboard, TgUser user) {
        send(text, keyboard, user.getChatId());
    }

    public void send(Text message, Keyboard keyboard, TgUser user) {
        send(mapText(message), mapKeyboard(keyboard), user.getChatId());
    }

    @SneakyThrows
    private void send(String text, ReplyKeyboard keyboard, String chatId) {
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
    public void sendImage(byte[] image, String imageName, Keyboard keyboard, TgUser user) {
        tgSender.execute(
                SendPhoto.builder()
                        .photo(new InputFile(new ByteArrayInputStream(image), imageName))
                        .chatId(user.getChatId())
                        .replyMarkup(mapKeyboard(keyboard))
                        .parseMode("html")
                        .build()
        );
    }

    @SneakyThrows
    public void sendImageToGroup(Text caption, byte[] image, String imageName, TgGroup group) {
        tgSender.execute(
                SendPhoto.builder()
                        .photo(new InputFile(new ByteArrayInputStream(image), imageName))
                        .chatId(group.getChatId().toString())
                        .parseMode("html")
                        .caption(mapText(caption, group.getLocale()))
                        .build()
        );
    }

// TODO
//    @SneakyThrows
//    public void sendImages(List<PCloudProvider.ImageData> images, TgUser user) {
//        tgSender.execute(
//                SendMediaGroup.builder()
//                        .medias(
//                                images.stream()
//                                        .map(image ->
//                                                {
//                                                    try {
//                                                        return InputMediaPhoto.builder()
//                                                                .caption(image.getUrl())
//                                                                .media(image.getUrl())
//                                                                .isNewMedia(true)
//                                                                .mediaName(image.getFilename())
////                                                                .newMediaStream(image.getInputStream())
//                                                                .newMediaStream(new ByteArrayInputStream(IOUtils.toByteArray(image.getInputStream())))
//                                                                .build();
//                                                    } catch (IOException e) {
//                                                        throw new RuntimeException(e);
//                                                    }
//                                                }
//                                        )
//                                        .collect(Collectors.toList())
//                        )
//                        .chatId(user.getChatId())
//                        .build()
//        );
//    }

    private ReplyKeyboardMarkup mapKeyboard(Keyboard keyboard) {
        return ReplyKeyboardMarkup.builder()
                .keyboard(
                        keyboard.getButtons().stream()
                                .map(buttonsRow ->
                                        buttonsRow.stream()
                                                .map(
                                                        button ->
                                                                messageSource.getMessage(
                                                                        button.getCode(),
                                                                        null,
                                                                        LocaleContextHolder.getLocale()
                                                                )
                                                )
                                                .map(KeyboardButton::new)
                                                .collect(Collectors.toUnmodifiableList()))
                                .map(KeyboardRow::new)
                                .collect(Collectors.toUnmodifiableList())
                )
                .resizeKeyboard(true)
                .build();
    }

    private String mapText(Text text) {
        return mapText(text, LocaleContextHolder.getLocale());
    }

    private String mapText(Text text, Locale locale) {
        return messageSource.getMessage(
                text.getMessage(),
                text.getParams().toArray(new String[0]),
                locale
        );
    }
}
