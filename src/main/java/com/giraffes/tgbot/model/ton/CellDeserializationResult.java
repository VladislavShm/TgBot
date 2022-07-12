package com.giraffes.tgbot.model.ton;

import com.giraffes.tgbot.model.ton.Cell;
import lombok.Data;

@Data
public class CellDeserializationResult {
    private final Cell cell;
    private final int[] residue;
}
