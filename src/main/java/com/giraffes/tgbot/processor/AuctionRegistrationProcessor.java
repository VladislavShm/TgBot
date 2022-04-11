package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.*;
import com.giraffes.tgbot.service.AuctionService;
import com.giraffes.tgbot.service.UserAuctionActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.giraffes.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;
import static com.giraffes.tgbot.utils.TelegramUiUtils.createBackButtonKeyboard;
import static com.giraffes.tgbot.utils.TelegramUiUtils.createYesNoKeyboard;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRegistrationProcessor extends LocationProcessor {
    private final UserAuctionActivityService userAuctionActivityService;
    private final AuctionService auctionService;

    @Override
    public Location getLocation() {
        return Location.AUCTION_REGISTRATION;
    }

    @Override
    Location processText(TgUser user, String text, boolean redirected) {
        String auctionOrderNumber = user.getLocationAttributes().get(LocationAttribute.AUCTION_ORDER_NUMBER);
        if (StringUtils.isBlank(auctionOrderNumber)) {
            log.warn("User {} tried to access auction without specified auction order number. Text: {}", user, text);
            return Location.AUCTIONS_BROWSE;
        }

        Auction auction = auctionService.findActiveByOrderNumber(Integer.valueOf(auctionOrderNumber));
        if (auction == null) {
            telegramSenderService.send(
                    "Похоже, что данный аукцион закончился. Возможно Вам в следующий раз повезет больше.",
                    createBackButtonKeyboard()
            );

            user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
            return Location.AUCTIONS_BROWSE;
        }

        if (redirected || "Ок".equals(text)) {
            sendConfirmToParticipateInAuction();
            return getLocation();
        }

        if ("Да".equals(text)) {
            registerParticipantIfNeeded(user, auction);
            return Location.AUCTION_PARTICIPATION;
        } else if ("Нет".equals(text)) {
            user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
            return Location.AUCTIONS_BROWSE;
        }

        return getLocation();
    }

    private void registerParticipantIfNeeded(TgUser user, Auction auction) {
        UserAuctionActivity activeUserAuctionActivity = userAuctionActivityService.findActivity(auction, user);
        if (activeUserAuctionActivity == null) {
            userAuctionActivityService.registerParticipant(auction, user);
            telegramSenderService.send(
                    "Вы зарегистрированы в качестве участника!",
                    createBackButtonKeyboard()
            );
        }
    }

    private void sendConfirmToParticipateInAuction() {
        String message = "Хотите ли Вы принять участие в данном аукционе?";

        telegramSenderService.send(
                message,
                createYesNoKeyboard()
        );
    }
}
