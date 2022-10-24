package com.tgbot.config;


import com.tgbot.property.TonProviderProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@RequiredArgsConstructor
public class TonProviderConfig {
    private final TonProviderProperties tonProviderProperties;

    @Bean
    @Qualifier("tonProviderRestTemplate")
    public RestTemplate tonProviderRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(tonProviderProperties.getUrl()));
        return restTemplate;
    }
}
