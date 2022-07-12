package com.giraffes.tgbot.model.ton.response;

import com.giraffes.tgbot.model.ton.Address;
import lombok.Data;

import java.math.BigInteger;

@Data
public class GetSaleDataResult {
    private final boolean isSale;
    private BigInteger fullPrice;
    private Address owner;
}
