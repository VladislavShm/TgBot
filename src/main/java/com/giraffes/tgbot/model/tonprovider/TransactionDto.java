package com.giraffes.tgbot.model.tonprovider;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TransactionDto {
    private Long datetime;
    private String transactionId;
    private String sender;
    private BigInteger value;
    private String text;
}
