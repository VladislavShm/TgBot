package com.giraffes.tgbot.entity;

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
    private String fromWallet;

    @Column
    private LocalDateTime datetime;

    @Column
    private BigInteger amount;

    @Column
    private String transactionId;

    @Column
    private String text;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;
}
