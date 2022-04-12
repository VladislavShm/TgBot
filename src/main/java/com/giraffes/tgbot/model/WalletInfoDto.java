package com.giraffes.tgbot.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletInfoDto {
    private boolean valid;
    private BigDecimal balance;
}
