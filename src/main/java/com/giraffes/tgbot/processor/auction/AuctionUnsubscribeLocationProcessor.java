package com.giraffes.tgbot.processor.auction;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.service.UserAuctionActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.giraffes.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;
import static com.giraffes.tgbot.utils.TelegramUiUtils.createYesNoKeyboard;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionUnsubscribeLocationProcessor extends AuctionLocationProcessor {
    private final UserAuctionActivityService userAuctionActivityService;

    @Override
    public Location getLocation() {
        return Location.AUCTION_UNSUBSCRIBE;
    }

    @Override
    protected Location processTextForAuction(TgUser user, String text, boolean redirected, Auction auction) {
        if (redirected || "Ок".equals(text)) {
            telegramSenderService.send(
                    String.format(
                            "Вы действительно хотите прекратить участие в аукционе № %d - '%s'?",
                            auction.getOrderNumber(), auction.getName()
                    ),
                    createYesNoKeyboard()
            );
            return getLocation();
        }

        if ("Да".equals(text)) {
            UserAuctionActivity userAuctionActivity = userAuctionActivityService.findActivity(auction, user);
            userAuctionActivity.setActive(false);
            clearUserLocationAttributes(user);
            return Location.AUCTIONS_BROWSE;
        }

        if ("Нет".equals(text)) {
            return Location.AUCTION_PARTICIPATION;
        }

        return getLocation();
    }

    @Override
    protected void clearUserLocationAttributes(TgUser user) {
        user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
    }
}
