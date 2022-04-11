package com.giraffes.tgbot.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class WalletInfoDto {
    private boolean valid;
    private BigInteger balance;
}
