package com.tgbot.processor.settings;

import com.tgbot.entity.Location;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.Button;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.ButtonName.SettingsLocationButton;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.processor.LocationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SettingsLocationProcessor extends LocationProcessor {
    @Override
    public Location getLocation() {
        return Location.SETTINGS;
    }

    @Override
    @SneakyThrows
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            sendDefaultSettingsMessage(user);
            return Optional.empty();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Optional.of(Location.BASE);
        }

        return messageToButtonTransformer
                .determineButton(text, SettingsLocationButton.class)
                .flatMap((button) -> {
                    switch (button) {
                        case SPECIFY_WALLET_BUTTON:
                        case CHANGE_WALLET_BUTTON:
                            return Optional.of(Location.WALLET_SETTINGS);
                        case CONFIRM_WALLET_BUTTON:
                            return Optional.of(Location.WALLET_CONFIRMATION);
                        case CHANGE_LANGUAGE_BUTTON:
                            return Optional.of(Location.LANGUAGE_CHANGE);
                        default:
                            return Optional.empty();
                    }
                })
                .or(() -> {
                    sendDefaultSettingsMessage(user);
                    return Optional.empty();
                });
    }

    private void sendDefaultSettingsMessage(TgUser user) {
        List<Button> customKeyboards = new ArrayList<>();
        if (StringUtils.isBlank(user.getWallet())) {
            customKeyboards.add(SettingsLocationButton.SPECIFY_WALLET_BUTTON);
        } else {
            customKeyboards.add(SettingsLocationButton.CHANGE_WALLET_BUTTON);
        }

        if (!user.isWalletConfirmed()) {
            customKeyboards.add(SettingsLocationButton.CONFIRM_WALLET_BUTTON);
        }

        customKeyboards.add(SettingsLocationButton.CHANGE_LANGUAGE_BUTTON);

        telegramSenderService.send(
                new Text("settings_location.base_message"),
                new Keyboard()
                        .line(customKeyboards)
                        .line(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
