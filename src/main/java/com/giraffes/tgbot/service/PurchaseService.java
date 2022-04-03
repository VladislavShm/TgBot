package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.property.PurchaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseProperties purchaseProperties;

    public String createLink(TgUser tgUser) {
        return String.format("ton://transfer/%s?amount=%s&text=login=%s", purchaseProperties.getWallet(), purchaseProperties.getPrice(), tgUser.getName());
    }
}
