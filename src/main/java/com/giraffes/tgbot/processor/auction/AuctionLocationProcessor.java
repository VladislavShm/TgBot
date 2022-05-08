package com.giraffes.tgbot.processor.auction;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.LocationAttribute;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.ButtonName;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.processor.LocationProcessor;
import com.giraffes.tgbot.service.AuctionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.giraffes.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;

@Slf4j
public abstract class AuctionLocationProcessor extends LocationProcessor {
    @Autowired
    private AuctionService auctionService;

    @Override
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        String auctionOrderNumber = user.getLocationAttributes().get(LocationAttribute.AUCTION_ORDER_NUMBER);
        if (StringUtils.isBlank(auctionOrderNumber)) {
            log.warn("User {} tried to access auction without specified auction order number. Text: {}", user, text);
            return Optional.of(Location.AUCTIONS_BROWSE);
        }

        Auction auction = auctionService.findActiveByOrderNumber(Integer.valueOf(auctionOrderNumber));
        if (auction == null) {
            telegramSenderService.send(
                    new Text("Похоже, что данный аукцион закончился. Пожалуйста, ожидайте результатов."),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
            );

            user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
            return Optional.of(Location.AUCTIONS_BROWSE);
        }

        return processTextForAuction(user, text, redirected, auction);
    }

    protected abstract Optional<Location> processTextForAuction(TgUser user, String text, boolean redirected, Auction auction);

    protected abstract void clearUserLocationAttributes(TgUser user);
}
