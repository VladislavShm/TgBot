package com.giraffes.tgbot.processor.settings;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.ButtonName;
import com.giraffes.tgbot.model.internal.telegram.ButtonName.BackCancelButton;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.processor.LocationProcessor;
import com.giraffes.tgbot.property.UserWalletProperties;
import com.giraffes.tgbot.service.WalletConfirmationService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        if (user.isWalletConfirmed()) {
            return Optional.of(Location.SETTINGS);
        }

        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            if (StringUtils.isBlank(user.getWallet())) {
                telegramSenderService.send(
                        new Text("Пожалуйста, сначала укажите Ваш кошелек в настройках"),
                        new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                        user
                );

                return Optional.of(Location.WALLET_SETTINGS);
            } else {
                askToConfirmWallet(user);
            }
        }

        if (messageToButtonTransformer.determineButton(text, BackCancelButton.class).isPresent()) {
            return Optional.of(Location.SETTINGS);
        }

        return Optional.empty();
    }

    private void askToConfirmWallet(TgUser user) {
        telegramSenderService.send(
                new Text(String.format(
                        "Пожалуйста, подтвердите Ваш TON кошелек\n<b><code>%s</code></b>\nпереведя <b><code>%s</code></b> TON на \n<b><code>%s</code></b>",
                        user.getWallet(),
                        TonCoinUtils.toHumanReadable(walletProperties.getConfirmationSum()),
                        walletProperties.getConfirmationWallet()
                )),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );

        String link = walletConfirmationService.createLink();
        telegramSenderService.send(
                new Text(String.format("Вы можете воспользоваться готовой ссылкой:\n\n<a href='%s'>%s</a>", link, link)),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );

        telegramSenderService.send(
                new Text("Как правило, проведение транзакции и получение данных о Вашем переводе средств занимают некоторое время. " +
                        "В случае, если подтверждение Вашего кошелька затягивается, пожалуйста, свяжитесь с нами - @GhostOfGiraffe "),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
