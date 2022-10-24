package com.tgbot.model.ton;

import lombok.Data;

@Data
public class CellDeserializationResult {
    private final Cell cell;
    private final int[] residue;
}
