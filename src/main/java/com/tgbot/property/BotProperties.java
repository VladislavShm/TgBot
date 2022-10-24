package com.tgbot.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Configuration
@ConfigurationProperties("tg-bot")
public class BotProperties {
    @NotBlank
    private String botUserName;

    @NotBlank
    private String botToken;
}
