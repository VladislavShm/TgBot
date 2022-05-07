package com.giraffes.tgbot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.Collections;

@UtilityClass
public class TelegramUiUtils {
    public static ReplyKeyboardMarkup createCancelButtonKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(
                        Collections.singletonList(
                                new KeyboardButton("Отмена")
                        )
                ))
                .resizeKeyboard(true)
                .build();
    }

    public static ReplyKeyboardMarkup createBackButtonKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(createBackButtonRow())
                .resizeKeyboard(true)
                .build();
    }

    public static KeyboardRow createBackButtonRow() {
        return new KeyboardRow(
                Collections.singletonList(
                        new KeyboardButton("Назад")
                )
        );
    }

    public static ReplyKeyboardMarkup createYesNoKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .keyboard(
                        Collections.singletonList(
                                new KeyboardRow(
                                        Arrays.asList(
                                                new KeyboardButton("Да"),
                                                new KeyboardButton("Нет")
                                        )
                                ))
                )
                .resizeKeyboard(true)
                .build();
    }
}
