package com.giraffes.tgbot.utils;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public class CollectorsUtils {
    public static <T> Collector<T, ?, Optional<T>> zeroOrOne() {
        return Collectors.reducing((a, b) -> {
            throw new IllegalStateException("More than one value was returned");
        });
    }

    public static <T> Collector<T, ?, T> onlyOne() {
        return Collectors.collectingAndThen(zeroOrOne(), Optional::get);
    }
}
