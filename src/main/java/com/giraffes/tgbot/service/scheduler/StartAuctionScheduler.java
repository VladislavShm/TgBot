package com.giraffes.tgbot.service.scheduler;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.service.AuctionService;
import com.giraffes.tgbot.service.TelegramSenderService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createOkKeyboard;

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
        List<String> chatIds = tgUserService.queryAllChatIds();
        Auction auction = auctionService.findActiveByOrderNumber(auctionOrderNumber);
        String message = String.format(
                "Начался аукцион № %d - %s. Чтобы принять в нем участие, пожалуйста, пройдите в раздел с аукционами.",
                auctionOrderNumber, auction.getName()
        );

        for (String chatId : chatIds) {
            telegramSenderService.send(
                    message,
                    createOkKeyboard(),
                    chatId
            );
        }
    }
}
