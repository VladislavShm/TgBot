package com.tgbot.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NftImage {
    private final Integer index;
    private final byte[] image;
    private final String filename;
}
