package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Purchase;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.Transaction;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.property.PurchaseProperties;
import com.giraffes.tgbot.repository.PurchaseRepository;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.giraffes.tgbot.model.internal.telegram.ButtonName.OkButton;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

    private final NftService nftService;
    private final TgUserService tgUserService;
    private final TonLinkService tonLinkService;
    private final PurchaseProperties purchaseProperties;
    private final PurchaseRepository purchaseRepository;
    private final TelegramSenderService telegramSenderService;

    public Text createPurchaseMessage(TgUser tgUser, Integer quantity) {
        BigInteger price = purchaseProperties.getPrice();
        BigInteger priceForAll = price.multiply(new BigInteger(String.valueOf(quantity)));

        Map<String, Object> linkParams = new HashMap<>();
        linkParams.put("id", tgUser.getChatId());
        linkParams.put("n", quantity);

        String params = tonLinkService.paramsToString(linkParams);

        return new Text("purchase.info")
                .param(TonCoinUtils.toHumanReadable(price))
                .param(quantity)
                .param(TonCoinUtils.toHumanReadable(priceForAll))
                .param(purchaseProperties.getWallet())
                .param(params)
                .param(tonLinkService.createLink(purchaseProperties.getWallet(), priceForAll, params));
    }

    public boolean isPurchaseTransaction(Transaction transaction) {
        return Optional.ofNullable(transaction.getText())
                .map(tonLinkService::parseParams)
                .flatMap(params ->
                        Optional.ofNullable(params.get("id"))
                                .flatMap(tgUserService::findByChatId)
                                .or(() ->
                                        Optional.ofNullable(params.get("login"))
                                                .flatMap(tgUserService::findByUsername)
                                )
                                .flatMap(buyer ->
                                        Optional.ofNullable(params.get("n"))
                                                .map(NUMBER_PATTERN::matcher)
                                                .filter(Matcher::find)
                                                .map(Matcher::group)
                                                .map(BigInteger::new)
                                                .map(quantity -> {
                                                    BigInteger nftPrice = purchaseProperties.getPrice();
                                                    BigInteger expectedAmount = quantity.multiply(nftPrice);
                                                    return expectedAmount.equals(transaction.getAmount());
                                                }))
                )
                .orElse(false);
    }

    public void registerPurchase(Transaction transaction) {
        Map<String, String> params = tonLinkService.parseParams(transaction.getText());

        TgUser user = Optional.ofNullable(params.get("id"))
                .flatMap(tgUserService::findByChatId)
                .or(() -> Optional.ofNullable(params.get("login"))
                        .flatMap(tgUserService::findByUsername))
                .orElseThrow();

        Purchase purchase = new Purchase();
        purchase.setQuantity(Integer.valueOf(params.get("n")));
        purchase.setUser(user);
        purchase.setTransaction(transaction);
        purchaseRepository.save(purchase);

        sendPurchaseNotification(user);
    }

    public Integer purchaseCount(TgUser user) {
        return ObjectUtils.defaultIfNull(purchaseRepository.purchasesCount(user), 0);
    }

    private void sendPurchaseNotification(TgUser user) {
        Integer userNftCount = nftService.findUserNftCount(user);
        Integer purchaseCount = purchaseCount(user);

        telegramSenderService.send(
                new Text("notification.purchase", userNftCount + purchaseCount, purchaseCount),
                new Keyboard(OkButton.OK_BUTTON),
                user
        );
    }

    public Integer totalPurchasesCount() {
        return ObjectUtils.defaultIfNull(purchaseRepository.totalPurchasesCount(), 0);
    }
}
