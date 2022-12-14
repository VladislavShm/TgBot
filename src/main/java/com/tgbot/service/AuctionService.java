package com.tgbot.service;

import com.tgbot.entity.Auction;
import com.tgbot.entity.TgUser;
import com.tgbot.entity.Transaction;
import com.tgbot.entity.UserAuctionActivity;
import com.tgbot.model.api.AuctionCreationDto;
import com.tgbot.model.tonprovider.WalletInfoDto;
import com.tgbot.repository.AuctionRepository;
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
import java.util.Optional;

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
        BigInteger minutesGone = calculateMinutesGone(auction);
        BigInteger reductionInTimes = minutesGone.divide(auction.getPriceReductionMinutes());
        return auction.getStartPrice().subtract(auction.getPriceReductionValue().multiply(reductionInTimes));
    }

    public static BigInteger calculateMinutesToNextReduction(Auction auction) {
        BigInteger minutesGone = calculateMinutesGone(auction);
        BigInteger priceReductionMinutes = auction.getPriceReductionMinutes();
        return priceReductionMinutes.subtract(minutesGone.mod(priceReductionMinutes)).subtract(BigInteger.ONE);
    }

    private static BigInteger calculateMinutesGone(Auction auction) {
        return BigInteger.valueOf(Math.abs(ChronoUnit.MINUTES.between(LocalDateTime.now(), auction.getStartDateTime())));
    }

    public static String createStartInMessage(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        return String.format(
                "???????????? ??????????: %s ???????? %s ?????????? %s ??????????",
                Math.abs(ChronoUnit.DAYS.between(now, auction.getStartDateTime())),
                Math.abs(ChronoUnit.HOURS.between(now, auction.getStartDateTime()) % 24),
                Math.abs(ChronoUnit.MINUTES.between(now, auction.getStartDateTime()) % 60)
        );
    }

    public LocalDateTime calculateAuctionFinishDateTime(Auction auction) {
        return userAuctionActivityService.findHighestBid(auction)
                .map(highestBid -> highestBid.getBidDateTime().plus(auction.getMinutesToOutbid(), ChronoUnit.MINUTES))
                .orElseGet(() -> {
                    BigInteger startPrice = auction.getStartPrice();
                    BigInteger stepsNumber = startPrice.subtract(auction.getMinPrice()).divide(auction.getPriceReductionValue());
                    BigInteger minutesForAllSteps = stepsNumber.multiply(auction.getPriceReductionMinutes());
                    return auction.getStartDateTime().plus(minutesForAllSteps.intValue(), ChronoUnit.MINUTES);
                });
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

    public BigInteger calculateMinimumAllowedBid(Auction auction) {
        Optional<UserAuctionActivity> highestBid = userAuctionActivityService.findHighestBid(auction);
        return calculateMinimumAllowedBid(auction, highestBid);
    }

    public BigInteger calculateMinimumAllowedBid(Auction auction, Optional<UserAuctionActivity> highestBid) {
        return highestBid
                .map(AuctionService::calculateNextMinimumAllowedBid)
                .orElseGet(() -> AuctionService.calculateCurrentReducedPrice(auction));
    }

    public static BigInteger calculateNextMinimumAllowedBid(UserAuctionActivity userAuctionActivity) {
        return userAuctionActivity.getBid().add(userAuctionActivity.getAuction().getMinimalStep());
    }

    public boolean isAuctionTransaction(Transaction transaction) {
        return auctionRepository.findFinishedNotPaidByHighestBidAndUserWallet(transaction.getAmount(), transaction.getSender()).isPresent();
    }

    public void processAuctionPay(Transaction transaction) {
        auctionRepository.findFinishedNotPaidByHighestBidAndUserWallet(transaction.getAmount(), transaction.getSender())
                .ifPresent(auction -> auction.setCoinsPaid(true));
    }
}
