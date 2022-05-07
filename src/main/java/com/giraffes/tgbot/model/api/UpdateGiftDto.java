package com.giraffes.tgbot.model.api;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateGiftDto {
    @NotNull
    private Long giftId;

    @NotBlank
    private String chatId;

    @NotBlank
    private String wallet;

    @NotNull
    private Integer amount;

    @NotBlank
    private String reason;
}
