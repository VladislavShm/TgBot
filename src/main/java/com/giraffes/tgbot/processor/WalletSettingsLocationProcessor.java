package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.model.WalletInfoDto;
import com.giraffes.tgbot.service.TonProviderService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createCancelButtonKeyboard;

@Component
@RequiredArgsConstructor
public class WalletSettingsLocationProcessor extends LocationProcessor {
    private final TonProviderService tonProviderService;

    @Override
    public Location getLocation() {
        return Location.WALLET_SETTINGS;
    }

    @Override
    @SneakyThrows
    protected Location processText(TgUser tgUser, String text, boolean redirected) {
        if (redirected || "Ок".equals(text)) {
            askToSpecifyWallet(tgUser);
        } else if ("Отмена".equals(text)) {
            return Location.SETTINGS;
        } else {
            WalletInfoDto walletInfo = tonProviderService.getWalletInfo(text);
            if (walletInfo.isValid()) {
                tgUser.setWallet(text);

                telegramSenderService.send(
                        "Кошелек обновлен! Новое значение:\n<b><code>" + text + "</code></b>",
                        createCancelButtonKeyboard()
                );

                return Location.SETTINGS;
            } else {
                telegramSenderService.send(
                        "Кошелек с данным адресом не найден. Пожалуйста, проверьте правильность введенных данных.",
                        createCancelButtonKeyboard()
                );
            }
        }

        return getLocation();
    }

    private void askToSpecifyWallet(TgUser tgUser) {
        telegramSenderService.send(
                "Пожалуйста, укажите Ваш TON кошелек" +
                        (StringUtils.isNotBlank(tgUser.getWallet())
                                ? "\nТекущий кошелек: <b><code>" + tgUser.getWallet() + "</code></b>"
                                : ""),
                createCancelButtonKeyboard()
        );
    }
}
