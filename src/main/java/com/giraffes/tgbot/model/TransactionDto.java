package com.giraffes.tgbot.model;

import lombok.Data;

@Data
public class TransactionDto {
    private Long datetime;
    private String transactionId;
    private String sender;
    private String value;
    private String number;
    private String chatId;
    private String username;
}
