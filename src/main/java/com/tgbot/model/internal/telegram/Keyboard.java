package com.tgbot.model.internal.telegram;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@NoArgsConstructor
public class Keyboard {
    private final List<List<Button>> buttons = new ArrayList<>();

    public Keyboard(Button button) {
        this.buttons.add(Collections.singletonList(button));
    }

    public Keyboard(List<Button> buttons) {
        this.buttons.add(buttons);
    }

    public Keyboard line(Collection<Button> keys) {
        buttons.add(new ArrayList<>(keys));
        return this;
    }

    public Keyboard line(Button button, Button... rest) {
        buttons.add(Stream.concat(Stream.of(button), Arrays.stream(rest)).collect(Collectors.toUnmodifiableList()));
        return this;
    }
}
