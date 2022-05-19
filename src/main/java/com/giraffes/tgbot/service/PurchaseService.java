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

    private final GiftService giftService;
    private final TgUserService tgUserService;
    private final TonLinkService tonLinkService;
    private final PurchaseProperties purchaseProperties;
    private final PurchaseRepository purchaseRepository;
    private final TelegramSenderService telegramSenderService;

    public String createPurchaseMessage(TgUser tgUser, Integer quantity) {
        BigInteger price = calculateNftPrice(tgUser);
        BigInteger priceForAll = price.multiply(new BigInteger(String.valueOf(quantity)));

        Map<String, Object> linkParams = new HashMap<>();
        linkParams.put("id", tgUser.getChatId());
        linkParams.put("n", quantity);

        return String.format(
                "На данный момент стоимость NFT для Вас составляет: %s TON.\n\n" +
                        "Для покупки %s NFT Вам необходимо отправить %s TON на кошелек: \n<CODE><b>%s</b></CODE>\n\n" +
                        "Чтобы получить уведомление об успешном совершении сделки, пожалуйста, укажите в описании перевода комментарий: <CODE><b>id=%s&n=%s</b></CODE>\n\n" +
                        "В случае, если указать комментарий не представляется возможным, Вы можете сообщить о покупке нам напрямую - @GhostOfGiraffe\n\n\n" +
                        "Или же Вы можете воспользоваться готовой ссылкой: %s",
                TonCoinUtils.toHumanReadable(price),
                quantity,
                TonCoinUtils.toHumanReadable(priceForAll),
                purchaseProperties.getWallet(),
                tgUser.getChatId(),
                quantity,
                tonLinkService.createLink(purchaseProperties.getWallet(), priceForAll, linkParams)
        );
    }

    @Deprecated
    public Integer purchasesCount() {
        return ObjectUtils.defaultIfNull(purchaseRepository.purchasesCount(), 0);
    }

    @Deprecated
    public Integer purchasesCount(TgUser tgUser) {
        return ObjectUtils.defaultIfNull(purchaseRepository.purchasesCount(tgUser), 0);
    }

    private BigInteger calculateNftPrice(TgUser tgUser) {
        BigInteger price = purchaseProperties.getBasePrice();
        Integer invitedCount = tgUserService.invitedCount(tgUser);

        if (invitedCount >= 2) {
            price = purchaseProperties.getPresalePrice();
        }

        return price;
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
                                                    BigInteger nftPrice = calculateNftPrice(buyer);
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

    private void sendPurchaseNotification(TgUser user) {
        int totalGiraffesQuantity = purchasesCount(user) + giftService.giftsCount(user);
        telegramSenderService.send(
                new Text(String.format("Спасибо за покупку! На данный момент у вас имеется %d жирафов", totalGiraffesQuantity)),
                new Keyboard(OkButton.OK_BUTTON),
                user
        );
    }
}
