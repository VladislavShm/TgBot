package com.giraffes.tgbot.config;


import com.giraffes.tgbot.property.TonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.http.HttpClient;
import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TonConfig {
    private final TonProperties tonProperties;

    @Bean
    @Qualifier("tonRestTemplate")
    public RestTemplate tonRestTemplate() {

        CloseableHttpClient httpClient = HttpClients.custom()
                .setRetryHandler((exception, executionCount, context) -> {
                    if (executionCount > 10) {
                        log.warn("Maximum retries {} reached", 10);
                        return false;
                    }

                    return true;
                })
                .build();

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));

        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(tonProperties.getUrl()));
        restTemplate.setInterceptors(Collections.singletonList(
                (request, body, execution) -> {
                    request.getHeaders().add("X-API-Key", tonProperties.getApiKey());
                    return execution.execute(request, body);
                }
        ));

        return restTemplate;
    }
}
