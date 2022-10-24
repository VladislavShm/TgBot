package com.tgbot.model.ton;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cell {
    private int isExotic;
    private BitString bits = new BitString(1023);
    private List<Cell> refs = new ArrayList<>();
    private List<Integer> refsAsInt = new ArrayList<>();
}
