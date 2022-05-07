package com.giraffes.tgbot.model.internal.telegram;

import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@ToString
@RequiredArgsConstructor
public class Text {
    private final List<String> params = new ArrayList<>();
    private final String message;

    public Text param(Object param) {
        params.add(param.toString());
        return this;
    }

    public Text param(Locale locale) {
        params.add(locale.getDisplayLanguage());
        return this;
    }

    public Text param(BigInteger param) {
        params.add(TonCoinUtils.toHumanReadable(param));
        return this;
    }
}
