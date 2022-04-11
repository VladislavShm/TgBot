package com.giraffes.tgbot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.Collections;

@UtilityClass
public class TelegramUiUtils {
    public static ReplyKeyboardMarkup createBaseButtons() {
        return ReplyKeyboardMarkup.builder()
                .keyboard(
                        Arrays.asList(
                                new KeyboardRow(
                                        Arrays.asList(
                                                new KeyboardButton("Купить \uD83E\uDD92"),
                                                new KeyboardButton("Инвайт инфо \uD83D\uDC65"),
                                                new KeyboardButton("Мои жирафы \uD83E\uDD92")
                                        )
                                ),
                                new KeyboardRow(
                                        Arrays.asList(
                                                new KeyboardButton("Аукцион ⚖️"),
                                                new KeyboardButton("О нас \uD83D\uDCD6"),
                                                new KeyboardButton("Настройки ⚙️")
                                        )
                                )
                        )
                )
                .resizeKeyboard(true)
                .build();
    }

    public static ReplyKeyboardMarkup createCancelButtonKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(createCancelButtonRow())
                .resizeKeyboard(true)
                .build();
    }

    public static KeyboardRow createCancelButtonRow() {
        return new KeyboardRow(
                Collections.singletonList(
                        new KeyboardButton("Отмена")
                )
        );
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

    public static ReplyKeyboardMarkup createOkKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .keyboard(
                        Collections.singletonList(
                                new KeyboardRow(
                                        Collections.singletonList(
                                                new KeyboardButton("Ок")
                                        )
                                ))
                )
                .resizeKeyboard(true)
                .build();
    }
}
