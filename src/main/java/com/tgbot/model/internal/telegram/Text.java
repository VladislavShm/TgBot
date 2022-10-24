package com.tgbot.model.internal.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class Text {
    private final List<String> params;
    private final String message;

    public Text(String message, Object... params) {
        this.message = message;
        this.params = Arrays.stream(params).map(Objects::toString).collect(Collectors.toList());
    }

    public Text param(Object param) {
        params.add(param.toString());
        return this;
    }

    public Text param(Locale locale) {
        params.add(locale.getDisplayLanguage());
        return this;
    }
}
