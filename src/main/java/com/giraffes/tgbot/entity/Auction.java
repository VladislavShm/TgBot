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
@Table(name = "auction")
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column
    private LocalDateTime finishDateTime;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean published;

    @Lob
    @ToString.Exclude
    @Basic(fetch = FetchType.LAZY)
    private byte[] nftImage;

    @Column
    private String nftImageName;

    @Column(nullable = false)
    private Integer orderNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigInteger startPrice;

    @Column(nullable = false)
    private BigInteger minPrice;

    @Column(nullable = false)
    private BigInteger priceReductionValue;

    @Column(nullable = false)
    private BigInteger priceReductionMinutes;

    @Column(nullable = false)
    private BigInteger minimalStep;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;
}
