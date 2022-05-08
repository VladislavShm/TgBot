package com.giraffes.tgbot.processor.auction;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.model.internal.telegram.ButtonName;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.service.UserAuctionActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.giraffes.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;
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
    protected Optional<Location> processTextForAuction(TgUser user, String text, boolean redirected, Auction auction) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            sendConfirmToParticipateInAuction();
            return Optional.empty();
        }

        if ("Да".equals(text)) {
            registerParticipantIfNeeded(user, auction);
            return Optional.of(Location.AUCTION_PARTICIPATION);
        }

        if ("Нет".equals(text)) {
            clearUserLocationAttributes(user);
            return Optional.of(Location.AUCTIONS_BROWSE);
        }

        return Optional.empty();
    }

    private void registerParticipantIfNeeded(TgUser user, Auction auction) {
        UserAuctionActivity userAuctionActivity = userAuctionActivityService.findActivity(auction, user);
        if (userAuctionActivity == null) {
            userAuctionActivityService.registerParticipant(auction, user);
            telegramSenderService.send(
                    new Text("Вы зарегистрированы в качестве участника!"),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
            );
        } else if (!userAuctionActivity.isActive()) {
            telegramSenderService.send(
                    new Text("Рады видеть Вас снова в качестве участника данного аукциона!"),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
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
