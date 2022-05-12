package com.giraffes.tgbot.service;

import com.giraffes.tgbot.model.tonprovider.TransactionDto;
import com.giraffes.tgbot.model.tonprovider.WalletInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TonProviderService {
    private final RestTemplate restTemplate;

    public WalletInfoDto getWalletInfo(String wallet) {
        return restTemplate.getForObject("/wallet-info/" + wallet, WalletInfoDto.class);
    }

    public List<TransactionDto> getTransactions() {
        return Arrays.stream(Objects.requireNonNull(restTemplate.getForObject("/transactions", TransactionDto[].class)))
                .collect(Collectors.toList());
    }
}
