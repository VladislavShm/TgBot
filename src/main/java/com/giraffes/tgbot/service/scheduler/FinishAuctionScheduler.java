package com.giraffes.tgbot.service.scheduler;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.property.PurchaseProperties;
import com.giraffes.tgbot.service.*;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static com.giraffes.tgbot.model.internal.telegram.ButtonName.OkButton;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class FinishAuctionScheduler {
    private final UserAuctionActivityService userAuctionActivityService;
    private final TelegramSenderService telegramSenderService;
    private final PurchaseProperties purchaseProperties;
    private final TonLinkService tonLinkService;

    @Lazy
    @Autowired
    private AuctionService auctionService;

    @Lazy
    @Autowired
    private AuctionSchedulerService auctionSchedulerService;

    public void run(Long auctionId) {
        log.info("Finishing auction: {}", auctionId);
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionService.findByIdAndLock(auctionId);
        LocalDateTime actualFinishDateTime = auctionService.calculateAuctionFinishDateTime(auction);

        if (actualFinishDateTime.isAfter(now)) {
            log.warn("Something went wrong! Reschedule auction {} finishing for a new time: {}", auctionId, actualFinishDateTime);
            auctionSchedulerService.scheduleAuctionFinish(auction);
            return;
        }

        try {
            userAuctionActivityService
                    .findHighestBid(auction)
                    .ifPresentOrElse(
                            this::finishAuctionHavingWinner,
                            () -> log.debug("Winner wasn't found for auction with ID={}", auction.getId())
                    );
        } catch (Exception e) {
            log.error("Error while executing onAuctionFinish action. ", e);
        }

        auction.setFinishDateTime(now);
    }

    private void finishAuctionHavingWinner(UserAuctionActivity highestBid) {
        sendNotificationToWinner(highestBid);

        Auction auction = highestBid.getAuction();
        List<UserAuctionActivity> participants = userAuctionActivityService.findAllParticipantsExceptFor(highestBid);
        for (UserAuctionActivity participant : participants) {
            telegramSenderService.send(
                    new Text(String.format(
                            "Аукцион № %s - %s завершен! Спасибо за участие в аукционе! Данный лот был выкуплен за %s TON. ",
                            auction.getOrderNumber(), auction.getName(), TonCoinUtils.toHumanReadable(highestBid.getBid())
                    )),
                    new Keyboard(OkButton.OK_BUTTON),
                    participant.getUser()
            );
        }
    }

    private void sendNotificationToWinner(UserAuctionActivity highestBid) {
        Auction auction = highestBid.getAuction();
        TgUser user = highestBid.getUser();
        telegramSenderService.send(
                new Text(String.format("Поздравляем! Вы победили в аукционе № %s - %s!", auction.getOrderNumber(), auction.getName())),
                new Keyboard(OkButton.OK_BUTTON),
                user
        );

        String link = tonLinkService.createLink(purchaseProperties.getWallet(), highestBid.getBid());
        telegramSenderService.send(
                new Text(String.format(
                        "Для того, чтобы получить выигранный лот, Вам необходимо сделать перевод, с указанного в настройках кошелька, на сумму, равную %s TON на кошелек\n<b><code>%s</code></b>\nИли же Вы можете воспользоваться готовой ссылкой\n\n<a href='%s'>%s</a>",
                        TonCoinUtils.toHumanReadable(highestBid.getBid()),
                        purchaseProperties.getWallet(),
                        link,
                        link
                )),
                new Keyboard(OkButton.OK_BUTTON),
                user
        );
    }
}
