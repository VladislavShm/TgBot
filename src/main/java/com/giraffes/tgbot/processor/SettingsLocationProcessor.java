package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Collections;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createCancelButtonRow;

@Component
public class SettingsLocationProcessor extends LocationProcessor {
    @Override
    public UserLocation getLocation() {
        return UserLocation.SETTINGS;
    }

    @Override
    @SneakyThrows
    public UserLocation processText(TgUser user, String text, boolean redirected) {
        if (redirected) {
            sendDefaultSettingsMessage(user);
        } else if ("Отмена".equals(text)) {
            return UserLocation.BASE;
        } else if ("Указать кошелек".equals(text)) {
            return UserLocation.WALLET_SETTINGS;
        } else {
            sendDefaultSettingsMessage(user);
        }

        return getLocation();
    }

    private void sendDefaultSettingsMessage(TgUser user) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("Добро пожаловать в раздел настроек!")
                        .chatId(user.getChatId())
                        .replyMarkup(
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
                        )
                        .build()
        );
    }
}
