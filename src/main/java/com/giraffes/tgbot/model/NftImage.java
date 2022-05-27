package com.giraffes.tgbot.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NftImage {
    private final byte[] image;
    private final String filename;
}
