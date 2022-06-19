package com.giraffes.tgbot.model.tonprovider;

import com.giraffes.tgbot.model.ContractType;
import com.giraffes.tgbot.model.WalletType;
import lombok.Data;

import java.math.BigInteger;

@Data
public class TransactionDto {
    private Long datetime;
    private String transactionId;
    private String sender;
    private ContractType senderType;
    private BigInteger value;
    private String text;
    private String toWallet;
    private WalletType toWalletType;
    private String hash;
}
