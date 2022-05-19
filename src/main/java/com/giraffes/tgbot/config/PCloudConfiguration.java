package com.giraffes.tgbot.config;

import com.giraffes.tgbot.property.PCloudProperties;
import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PCloudConfiguration {
    private final PCloudProperties pCloudProperties;

    @Bean
    @SneakyThrows
    public ApiClient apiClient() {
        ApiClient apiClient = PCloudSdk.newClientBuilder()
                .apiHost(pCloudProperties.getApiHost())
                .authenticator(Authenticators.newOAuthAuthenticator(pCloudProperties.getAccessToken()))
                .create();

        UserInfo userInfo = apiClient.getUserInfo().execute();
        log.info("Authorized to pCloud successfully with email: {}", userInfo.email());
        return apiClient;
    }
}
