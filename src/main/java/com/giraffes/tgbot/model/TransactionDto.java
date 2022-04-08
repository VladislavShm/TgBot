package com.giraffes.tgbot.model;

import lombok.Data;

@Data
public class TransactionDto {
    private Integer datetime;
    private String transactionId;
    private String sender;
    private String value;
    private String username;
}
