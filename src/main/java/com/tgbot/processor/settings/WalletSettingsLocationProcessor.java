package com.tgbot.processor.settings;

import com.tgbot.entity.Location;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.model.tonprovider.WalletInfoDto;
import com.tgbot.processor.LocationProcessor;
import com.tgbot.service.TonProviderService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            askToSpecifyWallet(user);
            return Optional.empty();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Optional.of(Location.SETTINGS);
        }

        WalletInfoDto walletInfo = tonProviderService.getWalletInfo(text);
        if (walletInfo.isValid()) {
            user.setWallet(text);
            user.setWalletConfirmed(false);

            telegramSenderService.send(
                    new Text(String.format(
                            "Кошелек обновлен! Новое значение:\n<b><code>%s</code></b>\n",
                            text
                    )),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
            );

            return Optional.of(Location.WALLET_CONFIRMATION);
        } else {
            telegramSenderService.send(
                    new Text("Кошелек с данным адресом не найден. Пожалуйста, проверьте правильность введенных данных."),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
            );
        }

        return Optional.empty();
    }

    private void askToSpecifyWallet(TgUser user) {
        telegramSenderService.send(
                new Text("Пожалуйста, укажите Ваш TON кошелек" +
                        (StringUtils.isNotBlank(user.getWallet())
                                ? "\nТекущий кошелек: <b><code>" + user.getWallet() + "</code></b>"
                                : "")),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
