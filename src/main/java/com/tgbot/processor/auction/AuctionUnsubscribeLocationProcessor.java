package com.tgbot.processor.auction;

import com.tgbot.entity.Auction;
import com.tgbot.entity.Location;
import com.tgbot.entity.TgUser;
import com.tgbot.entity.UserAuctionActivity;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.service.UserAuctionActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;
import static com.tgbot.utils.TelegramUiUtils.createYesNoKeyboard;

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
    protected Optional<Location> processTextForAuction(TgUser user, String text, boolean redirected, Auction auction) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            telegramSenderService.send(
                    String.format(
                            "Вы действительно хотите прекратить участие в аукционе № %d - '%s'?",
                            auction.getOrderNumber(), auction.getName()
                    ),
                    createYesNoKeyboard()
            );
            return Optional.empty();
        }

        if ("Да".equals(text)) {
            UserAuctionActivity userAuctionActivity = userAuctionActivityService.findActivity(auction, user);
            userAuctionActivity.setActive(false);
            clearUserLocationAttributes(user);
            return Optional.of(Location.AUCTIONS_BROWSE);
        }

        if ("Нет".equals(text)) {
            return Optional.of(Location.AUCTION_PARTICIPATION);
        }

        return Optional.empty();
    }

    @Override
    protected void clearUserLocationAttributes(TgUser user) {
        user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
    }
}
