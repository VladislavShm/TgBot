package com.giraffes.tgbot.processor.auction;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.service.UserAuctionActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.giraffes.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;
import static com.giraffes.tgbot.utils.TelegramUiUtils.createBackButtonKeyboard;
import static com.giraffes.tgbot.utils.TelegramUiUtils.createYesNoKeyboard;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRegistrationProcessor extends AuctionLocationProcessor {
    private final UserAuctionActivityService userAuctionActivityService;

    @Override
    public Location getLocation() {
        return Location.AUCTION_REGISTRATION;
    }

    @Override
    protected Location processTextForAuction(TgUser user, String text, boolean redirected, Auction auction) {
        if (redirected || "Ок".equals(text)) {
            sendConfirmToParticipateInAuction();
            return getLocation();
        }

        if ("Да".equals(text)) {
            registerParticipantIfNeeded(user, auction);
            return Location.AUCTION_PARTICIPATION;
        }

        if ("Нет".equals(text)) {
            clearUserLocationAttributes(user);
            return Location.AUCTIONS_BROWSE;
        }

        return getLocation();
    }

    private void registerParticipantIfNeeded(TgUser user, Auction auction) {
        UserAuctionActivity userAuctionActivity = userAuctionActivityService.findActivity(auction, user);
        if (userAuctionActivity == null) {
            userAuctionActivityService.registerParticipant(auction, user);
            telegramSenderService.send(
                    "Вы зарегистрированы в качестве участника!",
                    createBackButtonKeyboard()
            );
        } else if (!userAuctionActivity.isActive()) {
            telegramSenderService.send(
                    "Рады видеть Вас снова в качестве участника данного аукциона!",
                    createBackButtonKeyboard()
            );

            userAuctionActivity.setActive(true);
        }
    }

    private void sendConfirmToParticipateInAuction() {
        String message = "Хотите ли Вы принять участие в данном аукционе?";

        telegramSenderService.send(
                message,
                createYesNoKeyboard()
        );
    }

    @Override
    protected void clearUserLocationAttributes(TgUser user) {
        user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
    }
}
