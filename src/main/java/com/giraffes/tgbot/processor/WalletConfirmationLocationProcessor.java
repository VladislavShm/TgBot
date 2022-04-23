package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.property.UserWalletProperties;
import com.giraffes.tgbot.service.WalletConfirmationService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createBackButtonKeyboard;

@Component
@RequiredArgsConstructor
public class WalletConfirmationLocationProcessor extends LocationProcessor {
    private final WalletConfirmationService walletConfirmationService;
    private final UserWalletProperties walletProperties;

    @Override
    public Location getLocation() {
        return Location.WALLET_CONFIRMATION;
    }

    @Override
    @SneakyThrows
    protected Location processText(TgUser tgUser, String text, boolean redirected) {
        if (tgUser.isWalletConfirmed()) {
            return Location.SETTINGS;
        }

        if (redirected || "Ок".equals(text)) {
            if (StringUtils.isBlank(tgUser.getWallet())) {
                telegramSenderService.send(
                        "Пожалуйста, сначала укажите Ваш кошелек в настройках",
                        createBackButtonKeyboard()
                );

                return Location.WALLET_SETTINGS;
            } else {
                askToConfirmWallet(tgUser);
            }
        }

        if ("Назад".equals(text)) {
            return Location.SETTINGS;
        }

        return getLocation();
    }

    private void askToConfirmWallet(TgUser tgUser) {
        telegramSenderService.send(
                String.format(
                        "Пожалуйста, подтвердите Ваш TON кошелек\n<b><code>%s</code></b>\nпереведя <b><code>%s</code></b> TON на \n<b><code>%s</code></b>",
                        tgUser.getWallet(),
                        TonCoinUtils.toHumanReadable(walletProperties.getConfirmationSum()),
                        walletProperties.getConfirmationWallet()
                ),
                createBackButtonKeyboard()
        );

        String link = walletConfirmationService.createLink();
        telegramSenderService.send(
                String.format("Вы можете воспользоваться готовой ссылкой:\n\n<a href='%s'>%s</a>", link, link),
                createBackButtonKeyboard()
        );

        telegramSenderService.send(
                "Как правило, проведение транзакции и получение данных о Вашем переводе средств занимают некоторое время. " +
                        "В случае, если подтверждение Вашего кошелька затягивается, пожалуйста, свяжитесь с нами - @GhostOfGiraffe ",
                createBackButtonKeyboard()
        );
    }
}
