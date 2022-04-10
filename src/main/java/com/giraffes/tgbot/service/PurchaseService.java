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
    private final TgUserService tgUserService;
    private final PurchaseProperties purchaseProperties;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseCommunicationService purchaseCommunicationService;

    public String createPurchaseMessage(TgUser tgUser) {
        return String.format(
                "На данный момент стоимость NFT для Вас составляет: %s TON.\n\n" +
                        "Для покупки NFT Вам необходимо отправить указанную сумму на кошелек: \n%s\n\n" +
                        "Чтобы получить уведомление об успешном совершении сделки, пожалуйста, укажите в описании перевода комментарий: \nid=%s\n\n" +
                        "В случае, если указать комментарий не представляется возможным, Вы можете сообщить о покупке нам напрямую - @GhostOfGiraffe\n\n\n" +
                        "Или же Вы можете воспользоваться готовой ссылкой: %s",
                Long.parseLong(purchaseProperties.getPrice()) / 1000000000,
                purchaseProperties.getWallet(),
                tgUser.getChatId(),
                createLink(tgUser)
        );
    }

    private String createLink(TgUser tgUser) {
        return String.format("ton://transfer/%s?amount=%s&text=id=%s", purchaseProperties.getWallet(), purchaseProperties.getPrice(), tgUser.getChatId());
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
                datetime = Instant.ofEpochSecond(transaction.getDatetime())
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime();
            } catch (Exception e) {
                log.error("Can't parse incoming datetime {} for {}. ", transaction.getDatetime(), transaction.getTransactionId(), e);
                continue;
            }

            String chatId = transaction.getChatId();
            if (StringUtils.isEmpty(chatId) && StringUtils.isNotEmpty(transaction.getUsername())) {
                TgUser buyer = tgUserService.findByUsername(transaction.getUsername());
                if (buyer != null) {
                    chatId = buyer.getChatId();
                }
            }

            Purchase purchase = new Purchase();
            purchase.setAmount(transaction.getValue());
            purchase.setDatetime(datetime);
            purchase.setBuyerWallet(transaction.getSender());
            purchase.setTransactionId(transaction.getTransactionId());
            purchase.setChatId(chatId);
            purchase.setApproved(purchaseProperties.getPrice().equals(transaction.getValue()));
            purchaseRepository.save(purchase);

            newTransactionIds.add(purchase.getTransactionId());
            if (StringUtils.isNotBlank(transaction.getChatId()) && purchase.isApproved()) {
                newBuyers.add(transaction.getChatId());
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
        return purchaseRepository.approvedPurchasesCount(tgUser.getChatId());
    }
}
