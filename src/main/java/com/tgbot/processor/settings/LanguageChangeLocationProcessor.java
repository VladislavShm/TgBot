package com.tgbot.processor.settings;

import com.tgbot.entity.Location;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.ButtonName.BackCancelButton;
import com.tgbot.model.internal.telegram.ButtonName.LanguageChangeLocationButton;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.processor.LocationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LanguageChangeLocationProcessor extends LocationProcessor {
    @Override
    public Location getLocation() {
        return Location.LANGUAGE_CHANGE;
    }

    @Override
    @SneakyThrows
    protected Optional<Location> processText(TgUser tgUser, String text, boolean redirected) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            sendBaseMessage(tgUser);
            return Optional.empty();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Optional.of(Location.SETTINGS);
        }

        messageToButtonTransformer.determineButton(text, LanguageChangeLocationButton.class)
                .ifPresentOrElse(
                        button -> {
                            switch (button) {
                                case EN_BUTTON:
                                    updateLanguage(tgUser, Locale.ENGLISH);
                                    break;
                                case RU_BUTTON:
                                    updateLanguage(tgUser, Locale.forLanguageTag("ru"));
                                    break;
                                default:
                                    sendBaseMessage(tgUser);
                                    break;
                            }
                        },
                        () -> sendBaseMessage(tgUser));

        return Optional.empty();
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
