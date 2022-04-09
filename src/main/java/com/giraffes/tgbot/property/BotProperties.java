package com.giraffes.tgbot.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("tg-bot")
public class BotProperties {
    @NotBlank
    private String botUserName;

    @NotBlank
    private String botToken;
}
