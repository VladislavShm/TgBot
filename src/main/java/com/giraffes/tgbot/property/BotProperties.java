package com.giraffes.tgbot.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("tg-bot")
public class BotProperties {
    private String botUserName;
    private String botToken;
}
