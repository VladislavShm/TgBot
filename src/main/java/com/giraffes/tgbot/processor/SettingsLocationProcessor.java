package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createBackButtonRow;

@Component
@RequiredArgsConstructor
public class SettingsLocationProcessor extends LocationProcessor {
    @Override
    public Location getLocation() {
        return Location.SETTINGS;
    }

    @Override
    @SneakyThrows
    protected Location processText(TgUser user, String text, boolean redirected) {
        if (redirected || "Ок".equals(text)) {
            sendDefaultSettingsMessage(user);
        } else if ("Отмена".equals(text) || "Назад".equals(text)) {
            return Location.BASE;
        } else if ("Указать кошелек".equals(text) || "Изменить кошелек".equals(text)) {
            return Location.WALLET_SETTINGS;
        } else if ("Подтвердить кошелек".equals(text)) {
            return Location.WALLET_CONFIRMATION;
        }

        return getLocation();
    }

    private void sendDefaultSettingsMessage(TgUser user) {
        List<KeyboardButton> customKeyboards =
                user.isWalletConfirmed()
                        ? List.of
                        (
                                new KeyboardButton("Изменить кошелек")
                        )
                        : List.of
                        (
                                new KeyboardButton(StringUtils.isEmpty(user.getWallet()) ? "Указать кошелек" : "Изменить кошелек"),
                                new KeyboardButton("Подтвердить кошелек")
                        );

        telegramSenderService.send(
                "Добро пожаловать в раздел настроек!",
                ReplyKeyboardMarkup.builder()
                        .keyboard(
                                List.of(
                                        new KeyboardRow(customKeyboards),
                                        createBackButtonRow()
                                )
                        )
                        .resizeKeyboard(true)
                        .build()
        );
    }
}
