package com.giraffes.tgbot.entity;

import com.giraffes.tgbot.model.ContractType;
import com.giraffes.tgbot.model.WalletType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@ToString
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String sender;

    @Column
    @Enumerated(EnumType.STRING)
    private ContractType senderType;

    @Column
    private LocalDateTime datetime;

    @Column
    private BigInteger amount;

    @Column
    private String transactionId;

    @Column
    private String text;

    @Column
    private String toWallet;

    @Column
    @Enumerated(EnumType.STRING)
    private WalletType toWalletType;

    @Column
    private String hash;

    @Column
    private Long lt;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;
}
