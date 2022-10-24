package com.tgbot.service;

import com.tgbot.model.internal.telegram.Button;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageToButtonTransformer {
    private static final Set<Locale> SUPPORTED_LOCALES = Set.of(Locale.ENGLISH, Locale.forLanguageTag("ru"));

    private final MessageSource messageSource;

    public <B extends Enum<B> & Button> Optional<B> determineButton(String text, Class<B> expectedButtonClass) {
        return SUPPORTED_LOCALES.stream()
                .flatMap(
                        locale ->
                                EnumUtils.getEnumList(expectedButtonClass)
                                        .stream()
                                        .filter(button -> text.equals(messageSource.getMessage(button.getCode(), null, locale)))
                )
                .findFirst();
    }
}
