package com.giraffes.tgbot.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class AuctionCreationDto {
    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private Integer orderNumber;

    @NotNull
    private BigInteger startPrice;

    @NotNull
    private BigInteger minPrice;

    @NotNull
    private BigInteger priceReductionValue;

    @NotNull
    private BigInteger priceReductionMinutes;

    @NotNull
    private BigInteger minimalStep;
}
