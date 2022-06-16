package com.giraffes.tgbot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TonLinkService {
    public String paramsToString(Map<String, Object> params) {
        return params.entrySet()
                .stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("&"));
    }

    public String createLink(String toWallet, BigInteger amount) {
        return createLink(toWallet, amount, Collections.emptyMap());
    }

    public String createLink(String toWallet, BigInteger amount, Map<String, Object> params) {
        return createLink(toWallet, amount, paramsToString(params));
    }

    public String createLink(String toWallet, BigInteger amount, String params) {
        String link = String.format("ton://transfer/%s?amount=%s", toWallet, amount);

        if (StringUtils.isNotEmpty(params)) {
            link += "&text=" + URLEncoder.encode(params, StandardCharsets.UTF_8);
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
