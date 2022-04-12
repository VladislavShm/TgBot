package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import com.giraffes.tgbot.model.WalletInfoDto;
import com.giraffes.tgbot.service.TonProviderService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createCancelButtonKeyboard;

@Component
@RequiredArgsConstructor
public class WalletSettingsLocationProcessor extends LocationProcessor {
    private final TonProviderService tonProviderService;

    @Override
    public UserLocation getLocation() {
        return UserLocation.WALLET_SETTINGS;
    }

    @Override
    @SneakyThrows
    public UserLocation processText(TgUser tgUser, String text, boolean redirected) {
        if (redirected) {
            tgSender.execute(
                    SendMessage.builder()
                            .text(
                                    "Пожалуйста, укажите Ваш TON кошелек" +
                                            (StringUtils.isNotBlank(tgUser.getWallet())
                                                    ? "\nТекущий кошелек: <b><code>" + tgUser.getWallet() + "</code></b>"
                                                    : ""))
                            .parseMode("html")
                            .chatId(tgUser.getChatId())
                            .replyMarkup(createCancelButtonKeyboard())
                            .build()
            );
        } else if ("Отмена".equals(text)) {
            return UserLocation.SETTINGS;
        } else {
            WalletInfoDto walletInfo = tonProviderService.getWalletInfo(text);
            if (walletInfo.isValid()) {
                tgUser.setWallet(text);
                tgSender.execute(
                        SendMessage.builder()
                                .text("Кошелек обновлен! Новое значение:\n<b><code>" + text + "</code></b>")
                                .parseMode("html")
                                .chatId(tgUser.getChatId())
                                .replyMarkup(createCancelButtonKeyboard())
                                .build()
                );

                return UserLocation.SETTINGS;
            } else {
                tgSender.execute(
                        SendMessage.builder()
                                .text("Кошелек с данным адресом не найден. Пожалуйста, проверьте правильность введенных данных.")
                                .parseMode("html")
                                .chatId(tgUser.getChatId())
                                .replyMarkup(createCancelButtonKeyboard())
                                .build()
                );
            }
        }

        return getLocation();
    }
}
