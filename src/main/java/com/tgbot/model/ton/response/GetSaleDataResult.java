package com.tgbot.model.ton.response;

import com.tgbot.model.ton.Address;
import lombok.Data;

import java.math.BigInteger;

@Data
public class GetSaleDataResult {
    private final boolean isSale;
    private BigInteger fullPrice;
    private Address owner;
}
