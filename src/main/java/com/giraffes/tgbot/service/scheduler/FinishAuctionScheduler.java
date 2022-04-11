package com.giraffes.tgbot.service.scheduler;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.service.AuctionSchedulerService;
import com.giraffes.tgbot.service.AuctionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
@Transactional
public class FinishAuctionScheduler {
    @Lazy
    @Autowired
    private AuctionService auctionService;

    @Lazy
    @Autowired
    private AuctionSchedulerService auctionSchedulerService;

    public void run(Long auctionId) {
        Auction auction = auctionService.findByIdAndLock(auctionId);
        LocalDateTime actualFinishDateTime = auctionService.calculateAuctionFinishDateTime(auction);
        if (actualFinishDateTime.isAfter(LocalDateTime.now())) {
            log.warn("Something went wrong! Reschedule auction {} finishing for a new time: {}", auctionId, actualFinishDateTime);
            auctionSchedulerService.scheduleAuctionFinish(auction);
        } else {
            auction.setFinishDateTime(LocalDateTime.now());
            auctionService.onAuctionFinish(auction);
        }
    }
}
