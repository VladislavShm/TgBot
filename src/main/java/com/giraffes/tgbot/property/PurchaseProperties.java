package com.giraffes.tgbot.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("purchase")
public class PurchaseProperties {
    @NotBlank
    private String wallet;

    @NotNull
    private Long basePrice;

    @NotNull
    private Long presalePrice;
}
