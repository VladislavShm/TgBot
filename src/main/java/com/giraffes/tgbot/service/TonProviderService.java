package com.giraffes.tgbot.service;

import com.giraffes.tgbot.model.WalletInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TonProviderService {
    private final RestTemplate restTemplate;

    public WalletInfoDto getWalletInfo(String wallet) {
        return restTemplate.getForObject("/wallet-info/" + wallet, WalletInfoDto.class);
    }
}
