package com.tgbot.scheduler;

import com.tgbot.entity.Auction;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.service.AuctionService;
import com.tgbot.service.TelegramSenderService;
import com.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import static com.tgbot.model.internal.telegram.ButtonName.OkButton;

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
