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
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

    private final TgUserService tgUserService;
    private final PurchaseProperties purchaseProperties;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseCommunicationService purchaseCommunicationService;

    public String createPurchaseMessage(TgUser tgUser, Integer quantity) {
        long price = calculateNftPrice(tgUser);

        return String.format(
                "На данный момент стоимость NFT для Вас составляет: %s TON.\n\n" +
                        "Для покупки %s NFT Вам необходимо отправить %s TON на кошелек: \n<CODE><b>%s</b></CODE>\n\n" +
                        "Чтобы получить уведомление об успешном совершении сделки, пожалуйста, укажите в описании перевода комментарий: <CODE><b>id=%s&n=%s</b></CODE>\n\n" +
                        "В случае, если указать комментарий не представляется возможным, Вы можете сообщить о покупке нам напрямую - @GhostOfGiraffe\n\n\n" +
                        "Или же Вы можете воспользоваться готовой ссылкой: %s",
                price,
                quantity,
                quantity * price,
                purchaseProperties.getWallet(),
                tgUser.getChatId(),
                quantity,
                createLink(tgUser, quantity, price)
        );
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

            TgUser buyer = null;
            String chatId = transaction.getChatId();
            if (StringUtils.isEmpty(chatId) && StringUtils.isNotEmpty(transaction.getUsername())) {
                buyer = tgUserService.findByUsername(transaction.getUsername());
                if (buyer != null) {
                    chatId = buyer.getChatId();
                }
            }

            BigInteger receivedNumber = !StringUtils.isBlank(transaction.getNumber()) && NUMBER_PATTERN.matcher(transaction.getNumber()).find()
                    ? new BigInteger(transaction.getNumber())
                    : null;

            BigInteger value = new BigInteger(transaction.getValue());

            Integer number = null;
            boolean approved = false;
            if (receivedNumber != null && buyer != null) {
                BigInteger price = new BigInteger(String.valueOf(calculateNftPrice(buyer) * 1000000000));
                BigInteger expectedAmount = receivedNumber.multiply(price);
                if (expectedAmount.equals(value)) {
                    number = receivedNumber.intValue();
                    approved = true;
                }
            }

            Purchase purchase = new Purchase();
            purchase.setAmount(transaction.getValue());
            purchase.setDatetime(datetime);
            purchase.setBuyerWallet(transaction.getSender());
            purchase.setTransactionId(transaction.getTransactionId());
            purchase.setChatId(chatId);
            purchase.setNumber(number);
            purchase.setApproved(approved);
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

    public Integer getSoldPresaleNFTQuantity() {
        return purchaseRepository.getSoldPresaleNFTQuantity();
    }

    public Integer purchasesCount(TgUser tgUser) {
        return purchaseRepository.approvedPurchasesCount(tgUser.getChatId());
    }

    private long calculateNftPrice(TgUser tgUser) {
        long humanReadablePrice = purchaseProperties.getBasePrice();
        Integer invitedCount = tgUserService.invitedCount(tgUser);

        if (invitedCount >= 2) {
            humanReadablePrice = purchaseProperties.getPresalePrice();
        }

        return humanReadablePrice;
    }

    private String createLink(TgUser tgUser, Integer quantity, long price) {
        return String.format("ton://transfer/%s?amount=%s&text=id=%s&n=%s", purchaseProperties.getWallet(), quantity * price * 1000000000, tgUser.getChatId(), quantity);
    }
}
