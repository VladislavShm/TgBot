package com.giraffes.tgbot.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TonLinkService {
    public String createLink(String toWallet, BigInteger amount) {
        return createLink(toWallet, amount, Collections.emptyMap());
    }

    public String createLink(String toWallet, BigInteger amount, Map<String, Object> params) {
        String link = String.format("ton://transfer/%s?amount=%s", toWallet, amount);

        if (!params.isEmpty()) {
            link += "&text=" + params.entrySet().stream().map(
                    e -> String.format("%s=%s", e.getKey(), e.getValue())
            ).collect(Collectors.joining("&"));
        }

        return link;
    }

    public Map<String, String> parseParams(String text) {
        return Arrays.stream(text.split("&"))
                .map(v -> v.split("="))
                .filter(s -> s.length == 2 && !StringUtils.isAllBlank(s[0], s[1]))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
    }
}
