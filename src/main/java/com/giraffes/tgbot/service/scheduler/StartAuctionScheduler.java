package com.giraffes.tgbot.service.scheduler;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.service.AuctionService;
import com.giraffes.tgbot.service.TelegramSenderService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import static com.giraffes.tgbot.model.internal.telegram.ButtonName.OkButton;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class StartAuctionScheduler {
    private final TelegramSenderService telegramSenderService;
    private final TgUserService tgUserService;

    @Lazy
    @Autowired
    private AuctionService auctionService;

    public void run(Integer auctionOrderNumber) {
        log.info("Starting auction: {}", auctionOrderNumber);
        Auction auction = auctionService.findActiveByOrderNumber(auctionOrderNumber);
        Text message = new Text(String.format(
                "Начался аукцион № %d - %s. Чтобы принять в нем участие, пожалуйста, пройдите в раздел с аукционами.",
                auctionOrderNumber, auction.getName()
        ));

        Keyboard keyboard = new Keyboard(OkButton.OK_BUTTON);
        for (TgUser user : tgUserService.findAllUsers()) {
            telegramSenderService.send(message, keyboard, user);
        }
    }
}
