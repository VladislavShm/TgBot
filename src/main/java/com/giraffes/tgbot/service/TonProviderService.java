package com.giraffes.tgbot.service;

import com.giraffes.tgbot.model.tonprovider.TransactionDto;
import com.giraffes.tgbot.model.tonprovider.WalletInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TonProviderService {
    @Autowired
    @Qualifier("tonProviderRestTemplate")
    private RestTemplate restTemplate;

    public WalletInfoDto getWalletInfo(String wallet) {
        return restTemplate.getForObject("/wallet-info/" + wallet, WalletInfoDto.class);
    }

    public List<TransactionDto> getTransactions(Optional<Long> lastLt) {
        String url = lastLt.map(aLong -> "/transactions?lastLt=" + aLong).orElse("/transactions");
        return Arrays.stream(Objects.requireNonNull(restTemplate.getForObject(url, TransactionDto[].class)))
                .collect(Collectors.toList());
    }
}
