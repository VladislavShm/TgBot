package com.giraffes.tgbot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;

@UtilityClass
public class TgUiUtils {
    public static ReplyKeyboardMarkup createBaseButtons() {
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(
                        new KeyboardRow(
                                Arrays.asList(
                                        new KeyboardButton("Купить"),
                                        new KeyboardButton("Инвайт инфо"),
                                        new KeyboardButton("Мои жирафы")
                                )
                        )
                )
                .resizeKeyboard(true)
                .build();
    }
}
