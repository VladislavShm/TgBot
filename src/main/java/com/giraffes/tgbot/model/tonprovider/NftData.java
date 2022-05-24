package com.giraffes.tgbot.model.tonprovider;

import lombok.Data;

import java.math.BigInteger;

@Data
public class NftData {
    private Integer index;
    private String address;
    private String owner;
    private BigInteger value;
}
