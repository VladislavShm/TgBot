package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.*;
import com.giraffes.tgbot.service.AuctionService;
import com.giraffes.tgbot.service.UserAuctionActivityService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import static com.giraffes.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;
import static com.giraffes.tgbot.entity.LocationAttribute.SUGGESTED_AUCTION_BID;
import static com.giraffes.tgbot.utils.TelegramUiUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionParticipationLocationProcessor extends LocationProcessor {
    private static final Pattern BID_PATTERN = Pattern.compile("^\\d+\\.?\\d*$");

    private final UserAuctionActivityService userAuctionActivityService;
    private final AuctionService auctionService;

    @Override
    public Location getLocation() {
        return Location.AUCTION_PARTICIPATION;
    }

    @Override
    Location processText(TgUser user, String text, boolean redirected) {
        String auctionOrderNumber = user.getLocationAttributes().get(LocationAttribute.AUCTION_ORDER_NUMBER);
        if (StringUtils.isBlank(auctionOrderNumber)) {
            log.warn("User {} tried to access auction without specified auction order number. Text: {}", user, text);
            clearUserLocationAttributes(user);
            return Location.AUCTIONS_BROWSE;
        }

        if ("Назад".equals(text)) {
            clearUserLocationAttributes(user);
            return Location.AUCTIONS_BROWSE;
        }

        Auction auction = auctionService.findActiveByOrderNumber(Integer.valueOf(auctionOrderNumber));
        if (auction == null) {
            telegramSenderService.send(
                    "Похоже, что данный аукцион закончился. Пожалуйста, ожидайте результатов.",
                    createBackButtonKeyboard()
            );

            clearUserLocationAttributes(user);
            return Location.AUCTIONS_BROWSE;
        }

        if (redirected || "Ок".equals(text)) {
            sendCurrentAuctionState(auction);
            return getLocation();
        }

        if ("Да".equals(text)) {
            processBidApproved(user, auction.getId());
            sendCurrentAuctionState(auction);
            return getLocation();
        }

        if ("Нет".equals(text)) {
            sendCurrentAuctionState(auction);
            user.getLocationAttributes().remove(SUGGESTED_AUCTION_BID);
            return getLocation();
        }

        if (BID_PATTERN.matcher(text).find()) {
            processBidSuggest(user, text, auction);
        } else {
            sendInvalidInput();
        }

        return getLocation();
    }

    private void sendInvalidInput() {
        telegramSenderService.send(
                "Неверный формат.\nОтправьте количество TON, которые Вы хотите поставить.",
                createCancelButtonKeyboard()
        );
    }

    private void processBidSuggest(TgUser user, String text, Auction auction) {
        BigInteger auctionBid = TonCoinUtils.fromHumanReadable(text);
        if (auctionBid.compareTo(BigInteger.ZERO) <= 0) {
            telegramSenderService.send(
                    "Пожалуйста, укажите значение больше 0",
                    createBackButtonKeyboard()
            );

            return;
        }

        BigInteger minimumAllowBid = auctionService.calculateMinimumAllowBid(auction);
        if (auctionBid.compareTo(minimumAllowBid) < 0) {
            sendInvalidTonAmount(minimumAllowBid);
            return;
        }

        if (!auctionService.isUserHasEnoughCoins(user, auctionBid)) {
            sendNotEnoughCoins();
            return;
        }

        user.getLocationAttributes().put(SUGGESTED_AUCTION_BID, text);
        telegramSenderService.send(
                String.format("Вы действительно хотите сделать ставку в размере %s TON?", text),
                createYesNoKeyboard()
        );
    }

    private void sendNotEnoughCoins() {
        telegramSenderService.send(
                "К сожалению, на Вашем кошельке недостаточно средств для совершения данной ставки.",
                createBackButtonKeyboard()
        );
    }

    private void processBidApproved(TgUser user, Long auctionId) {
        Auction auction = auctionService.findByIdAndLock(auctionId);
        BigInteger minimumAllowBid = auctionService.calculateMinimumAllowBid(auction);
        BigInteger auctionBid = TonCoinUtils.fromHumanReadable(user.getLocationAttributes().get(SUGGESTED_AUCTION_BID));
        if (auctionBid.compareTo(minimumAllowBid) < 0) {
            sendInvalidTonAmount(minimumAllowBid);
            return;
        }

        if (!auctionService.isUserHasEnoughCoins(user, auctionBid)) {
            sendNotEnoughCoins();
            return;
        }

        UserAuctionActivity activeUserAuctionActivity = userAuctionActivityService.findActivity(auction, user);
        activeUserAuctionActivity.setBid(auctionBid);

        telegramSenderService.send("Ваша ставка принята!", createBackButtonKeyboard());

        user.getLocationAttributes().remove(SUGGESTED_AUCTION_BID);
    }

    private void sendInvalidTonAmount(BigInteger minimumAllowBid) {
        telegramSenderService.send(
                String.format(
                        "Минимальная допустимая ставка %s TON. Пожалуйста, укажите количество TON большее или равное указанной сумме.",
                        TonCoinUtils.toHumanReadable(minimumAllowBid)
                ),
                createBackButtonKeyboard()
        );
    }

    private void sendCurrentAuctionState(Auction auction) {
        String message;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getStartDateTime())) {
            UserAuctionActivity highestBid = userAuctionActivityService.findHighestBid(auction);
            if (highestBid != null) {
                long minutesLeft = Math.abs(60 - ChronoUnit.MINUTES.between(now, highestBid.getUpdateDateTime()));
                message = String.format(
                        "Текущая максимальная сделаная ставка: %s TON\n" +
                                "У Вас осталось <i><b>%s</b></i> минут чтобы перебить ставку.\n" +
                                "Отправьте количество TON, которые Вы хотите поставить.",
                        TonCoinUtils.toHumanReadable(highestBid.getBid()), minutesLeft
                );
            } else {
                message = String.format(
                        "Текущая минимальная допустимая ставка: %s TON.\n" +
                                "Отправьте количество ТОН, которые Вы хотите поставить.",
                        TonCoinUtils.toHumanReadable(AuctionService.calculateCurrentReducedPrice(auction))
                );
            }
        } else {
            message = String.format(
                    "Вы зарегистрированы в качестве участника в данном аукционе.\n" +
                            "Пожалуйста, ожидайте начала данного аукциона, чтобы принять в нем участие.\n%s",
                    AuctionService.createStartInMessage(auction)
            );
        }

        telegramSenderService.send(
                message,
                createBackButtonKeyboard()
        );
    }

    private void clearUserLocationAttributes(TgUser user) {
        user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
        user.getLocationAttributes().remove(SUGGESTED_AUCTION_BID);
    }
}
