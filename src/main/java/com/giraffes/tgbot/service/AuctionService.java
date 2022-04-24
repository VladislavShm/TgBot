package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.model.AuctionCreationDto;
import com.giraffes.tgbot.model.WalletInfoDto;
import com.giraffes.tgbot.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {
    private final UserAuctionActivityService userAuctionActivityService;
    private final AuctionSchedulerService auctionSchedulerService;
    private final TonProviderService tonProviderService;
    private final AuctionRepository auctionRepository;

    public Auction create(AuctionCreationDto auctionCreationDto) {
        Auction auction = new Auction();
        auction.setStartDateTime(auctionCreationDto.getStartDateTime());
        auction.setName(auctionCreationDto.getName());
        auction.setDescription(auctionCreationDto.getDescription());
        auction.setOrderNumber(auctionCreationDto.getOrderNumber());
        auction.setMinPrice(auctionCreationDto.getMinPrice());
        auction.setStartPrice(auctionCreationDto.getStartPrice());
        auction.setPriceReductionMinutes(auctionCreationDto.getPriceReductionMinutes());
        auction.setPriceReductionValue(auctionCreationDto.getPriceReductionValue());
        auction.setMinimalStep(auctionCreationDto.getMinimalStep());
        auction.setMinutesToOutbid(auctionCreationDto.getMinutesToOutbid());
        return auctionRepository.save(auction);
    }

    @SneakyThrows
    public void addNftImage(MultipartFile file, Long id) {
        Auction auction = findByIdOrFail(id);
        auction.setNftImage(IOUtils.toByteArray(file.getInputStream()));
        auction.setNftImageName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
    }

    public void activate(Long id) {
        Auction auction = findByIdOrFail(id);
        auction.setPublished(true);

        auctionSchedulerService.scheduleAuctionStart(auction);
        auctionSchedulerService.scheduleAuctionFinish(auction);
    }

    public List<Auction> findActive() {
        LocalDateTime now = LocalDateTime.now();
        return auctionRepository.findActive(now);
    }

    public List<Auction> findUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return auctionRepository.findUpcoming(now);
    }

    private Auction findByIdOrFail(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Auction with ID=%d is not found", id)));
    }

    public Auction findActiveByOrderNumber(Integer orderNumber) {
        return auctionRepository.findByOrderNumberAndPublishedIsTrueAndFinishDateTimeIsNull(orderNumber);
    }

    public Auction findByIdAndLock(Long id) {
        return auctionRepository.queryLockedById(id);
    }

    public static BigInteger calculateCurrentReducedPrice(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        BigInteger minutesGone = BigInteger.valueOf(Math.abs(ChronoUnit.MINUTES.between(now, auction.getStartDateTime())));
        BigInteger reductionInTimes = minutesGone.divide(auction.getPriceReductionMinutes());
        return auction.getStartPrice().subtract(auction.getPriceReductionValue().multiply(reductionInTimes));
    }

    public static String createStartInMessage(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        return String.format(
                "Начало через: %s дней %s часов %s минут",
                Math.abs(ChronoUnit.DAYS.between(now, auction.getStartDateTime())),
                Math.abs(ChronoUnit.HOURS.between(now, auction.getStartDateTime()) % 24),
                Math.abs(ChronoUnit.MINUTES.between(now, auction.getStartDateTime()) % 60)
        );
    }

    public LocalDateTime calculateAuctionFinishDateTime(Auction auction) {
        UserAuctionActivity highestBid = userAuctionActivityService.findHighestBid(auction);
        if (highestBid != null) {
            return highestBid.getBidDateTime().plus(auction.getMinutesToOutbid(), ChronoUnit.MINUTES);
        }

        BigInteger startPrice = auction.getStartPrice();
        BigInteger stepsNumber = startPrice.subtract(auction.getMinPrice()).divide(auction.getPriceReductionValue());
        BigInteger minutesForAllSteps = stepsNumber.multiply(auction.getPriceReductionMinutes());
        return auction.getStartDateTime().plus(minutesForAllSteps.intValue(), ChronoUnit.MINUTES);
    }

    public boolean isUserHasEnoughCoins(TgUser user, BigInteger auctionBid) {
        WalletInfoDto walletInfo = tonProviderService.getWalletInfo(user.getWallet());
        if (!walletInfo.isValid()) {
            // I'm not sure that this option is possible. Only in case if there is something wrong with blockchain.
            log.warn("An unexpected invalid status was received for the user's wallet: {}", user);
            return false;
        }

        return auctionBid.compareTo(walletInfo.getBalance()) <= 0;
    }

    public BigInteger calculateMinimumAllowBid(Auction auction) {
        UserAuctionActivity highestBid = userAuctionActivityService.findHighestBid(auction);
        return calculateMinimumAllowBid(auction, highestBid);
    }

    public BigInteger calculateMinimumAllowBid(Auction auction, UserAuctionActivity highestBid) {
        if (highestBid != null) {
            return highestBid.getBid().add(auction.getMinimalStep());
        }

        return AuctionService.calculateCurrentReducedPrice(auction);
    }

    public void onAuctionFinish(Auction auction) {
        // TODO
        log.info("Auction has been finished: {}", auction);
    }
}
