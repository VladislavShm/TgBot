package com.giraffes.tgbot.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Configuration
@ConfigurationProperties("p-cloud")
public class PCloudProperties {
    @NotBlank
    private String apiHost;

    @NotBlank
    private String accessToken;
}
