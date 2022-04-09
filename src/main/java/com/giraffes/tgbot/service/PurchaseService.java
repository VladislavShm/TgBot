package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Purchase;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.TransactionDto;
import com.giraffes.tgbot.property.PurchaseProperties;
import com.giraffes.tgbot.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseProperties purchaseProperties;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseCommunicationService purchaseCommunicationService;

    public String createLink(TgUser tgUser) {
        return String.format("ton://transfer/%s?amount=%s&text=login=%s", purchaseProperties.getWallet(), purchaseProperties.getPrice(), tgUser.getName());
    }

    @Transactional
    public void updatePurchases(List<TransactionDto> transactions) {
        Set<String> newBuyers = new HashSet<>();
        List<String> newTransactionIds = new ArrayList<>();
        Set<String> registeredTransactionIds = new HashSet<>(purchaseRepository.findAllTransactionIds());
        for (TransactionDto transaction : transactions) {
            if (registeredTransactionIds.contains(transaction.getTransactionId())) {
                continue;
            }

            LocalDateTime datetime;
            try {
                datetime = Instant.ofEpochMilli(transaction.getDatetime())
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime();
            } catch (Exception e) {
                log.error("Can't parse incoming datetime. ", e);
                continue;
            }

            Purchase purchase = new Purchase();
            purchase.setAmount(transaction.getValue());
            purchase.setDatetime(datetime);
            purchase.setBuyerWallet(transaction.getSender());
            purchase.setTransactionId(transaction.getTransactionId());
            purchase.setUsername(transaction.getUsername());
            purchase.setApproved(purchaseProperties.getPrice().equals(transaction.getValue()));
            purchaseRepository.save(purchase);

            newTransactionIds.add(purchase.getTransactionId());
            if (StringUtils.isNotBlank(transaction.getUsername())) {
                newBuyers.add(transaction.getUsername());
            }
        }

        for (String newBuyer : newBuyers) {
            try {
                purchaseCommunicationService.sendPurchaseNotification(newBuyer);
            } catch (Exception e) {
                log.error("Error while sending purchase notification to {}. ", newBuyer, e);
            }
        }


        log.info("Registered new purchases: {}", newTransactionIds);
    }

    public Integer purchasesCount(TgUser tgUser) {
        return purchaseRepository.approvedPurchasesCount(tgUser.getName());
    }
}
