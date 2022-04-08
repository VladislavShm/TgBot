package com.giraffes.tgbot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String buyerWallet;

    @Column
    private Integer datetime;

    @Column
    private String amount;

    @Column
    private String transactionId;

    @Column
    private String username;

    @Column
    private boolean approved;
}
