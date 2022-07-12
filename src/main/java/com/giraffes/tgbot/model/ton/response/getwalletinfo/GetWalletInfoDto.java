package com.giraffes.tgbot.model.ton.response.getwalletinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigInteger;

@Data
public class GetWalletInfoDto {
    private boolean wallet;

    private BigInteger balance;

    @JsonProperty("account_state")
    private String accountState;
}
