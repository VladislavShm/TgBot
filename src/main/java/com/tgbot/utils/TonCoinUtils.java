package com.tgbot.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@UtilityClass
public class TonCoinUtils {
    public static String toHumanReadable(BigInteger value) {
        return new BigDecimal(value).divide(new BigDecimal("1000000000"), 2, RoundingMode.DOWN).toString();
    }

    public static BigInteger fromHumanReadable(String value) {
        return new BigInteger(new BigDecimal(value).multiply(new BigDecimal("1000000000")).setScale(0, RoundingMode.DOWN).toString());
    }
}
