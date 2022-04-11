package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.Location;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.Collections;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createCancelButtonRow;

@Component
@RequiredArgsConstructor
public class SettingsLocationProcessor extends LocationProcessor {
    @Override
    public Location getLocation() {
        return Location.SETTINGS;
    }

    @Override
    @SneakyThrows
    public Location processText(TgUser user, String text, boolean redirected) {
        if (redirected || "Ок".equals(text)) {
            sendDefaultSettingsMessage();
        } else if ("Отмена".equals(text)) {
            return Location.BASE;
        } else if ("Указать кошелек".equals(text)) {
            return Location.WALLET_SETTINGS;
        }

        return getLocation();
    }

    private void sendDefaultSettingsMessage() {
        telegramSenderService.send(
                "Добро пожаловать в раздел настроек!",
                ReplyKeyboardMarkup.builder()
                        .keyboard(
                                Arrays.asList(
                                        new KeyboardRow(
                                                Collections.singletonList(
                                                        new KeyboardButton("Указать кошелек")
                                                )
                                        ),
                                        createCancelButtonRow()
                                )
                        )
                        .resizeKeyboard(true)
                        .build()
        );
    }
}
