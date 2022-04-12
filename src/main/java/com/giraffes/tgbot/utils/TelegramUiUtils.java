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
}
