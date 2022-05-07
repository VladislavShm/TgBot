package com.giraffes.tgbot.processor.settings;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.ButtonName;
import com.giraffes.tgbot.model.internal.telegram.ButtonName.BackCancelButton;
import com.giraffes.tgbot.model.internal.telegram.ButtonName.LanguageChangeLocationButton;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.processor.LocationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LanguageChangeLocationProcessor extends LocationProcessor {
    @Override
    public Location getLocation() {
        return Location.LANGUAGE_CHANGE;
    }

    @Override
    @SneakyThrows
    protected Location processText(TgUser tgUser, String text, boolean redirected) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            sendBaseMessage(tgUser);
            return getLocation();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Location.SETTINGS;
        }

        return messageToButtonTransformer.determineButton(text, LanguageChangeLocationButton.class)
                .map(button -> {
                    switch (button) {
                        case EN_BUTTON:
                            updateLanguage(tgUser, Locale.ENGLISH);
                            return getLocation();
                        case RU_BUTTON:
                            updateLanguage(tgUser, Locale.forLanguageTag("ru"));
                            return getLocation();
                        default:
                            sendBaseMessage(tgUser);
                            return getLocation();
                    }
                })
                .orElseGet(() -> {
                    sendBaseMessage(tgUser);
                    return getLocation();
                });
    }

    private void updateLanguage(TgUser tgUser, Locale locale) {
        tgUser.setLocale(locale);
        LocaleContextHolder.setLocale(locale);
        sendLanguageWasChanged(tgUser);
    }

    private void sendBaseMessage(TgUser tgUser) {
        telegramSenderService.send(
                new Text("language_change.base_message")
                        .param(ObjectUtils.defaultIfNull(tgUser.getLocale(), LocaleContextHolder.getLocale())),
                createButtons(),
                tgUser
        );
    }

    private void sendLanguageWasChanged(TgUser tgUser) {
        telegramSenderService.send(
                new Text("language_change.language_changed").param(tgUser.getLocale()),
                createButtons(),
                tgUser
        );
    }

    private Keyboard createButtons() {
        return new Keyboard()
                .line(LanguageChangeLocationButton.EN_BUTTON, LanguageChangeLocationButton.RU_BUTTON)
                .line(BackCancelButton.BACK_BUTTON);
    }
}
