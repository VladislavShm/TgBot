package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.service.scheduler.FinishAuctionScheduler;
import com.giraffes.tgbot.service.scheduler.StartAuctionScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSchedulerService implements ApplicationListener<ApplicationStartedEvent> {
    private final Map<Long, ScheduledFuture<?>> START_AUC_SCHEDULERS = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> FINISH_AUC_SCHEDULERS = new ConcurrentHashMap<>();

    private final FinishAuctionScheduler finishAuctionScheduler;
    private final StartAuctionScheduler startAuctionScheduler;
    private final TaskScheduler taskScheduler;

    @Lazy
    @Autowired
    private AuctionService auctionService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.info("Schedule auctions events on application start up");

        List<Auction> active = auctionService.findActive();
        List<Auction> upcoming = auctionService.findUpcoming();

        for (Auction auction : upcoming) {
            scheduleAuctionStart(auction);
        }

        for (Auction auction : Stream.of(upcoming, active).flatMap(Collection::stream).collect(Collectors.toList())) {
            scheduleAuctionFinish(auction);
        }

        log.info("Auctions events scheduled");
    }

    public void scheduleAuctionStart(Auction auction) {
        Optional.ofNullable(START_AUC_SCHEDULERS.put(
                auction.getId(),
                taskScheduler.schedule(
                        () -> startAuctionScheduler.run(auction.getId()),
                        auction.getStartDateTime().toInstant(ZoneOffset.UTC)
                )
        )).ifPresent(scheduler -> scheduler.cancel(false));
        log.info("Auction {} start event has been scheduled at: {}", auction.getId(), auction.getStartDateTime());
    }

    public void scheduleAuctionFinish(Auction auction) {
        LocalDateTime dateTime = auctionService.calculateAuctionFinishDateTime(auction);
        Optional.ofNullable(FINISH_AUC_SCHEDULERS.put(
                auction.getId(),
                taskScheduler.schedule(
                        () -> finishAuctionScheduler.run(auction.getId()),
                        dateTime.toInstant(ZoneOffset.UTC)
                )
        )).ifPresent(scheduler -> scheduler.cancel(false));
        log.info("Auction {} finish event has been scheduled at: {}", auction.getId(), dateTime);
    }
}
