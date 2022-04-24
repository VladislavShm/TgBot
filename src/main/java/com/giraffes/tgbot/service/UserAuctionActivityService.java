package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.repository.UserAuctionActivityRepository;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createOkKeyboard;

@Service
@RequiredArgsConstructor
public class UserAuctionActivityService {
    private final UserAuctionActivityRepository userAuctionActivityRepository;
    private final TelegramSenderService telegramSenderService;

    public UserAuctionActivity findActivity(Auction auction, TgUser user) {
        return userAuctionActivityRepository.findByAuctionAndUser(auction, user);
    }

    public boolean isParticipant(Auction auction, TgUser user) {
        return userAuctionActivityRepository.findByAuctionAndUserAndActiveIsTrue(auction, user) != null;
    }

    public UserAuctionActivity findHighestBid(Auction auction) {
        return userAuctionActivityRepository.findWithHighestBid(auction);
    }

    public void registerParticipant(Auction auction, TgUser user) {
        UserAuctionActivity userAuctionActivity = new UserAuctionActivity();
        userAuctionActivity.setAuction(auction);
        userAuctionActivity.setUser(user);
        userAuctionActivity.setActive(true);
        userAuctionActivityRepository.save(userAuctionActivity);
    }

    public void notifyOutbid(UserAuctionActivity prevHighestBid, BigInteger newMaxBid) {
        telegramSenderService.send(
                String.format(
                        "Ваша ставка была перебита!\n" +
                                "Текущая максимальная ставка: %s TON.\n" +
                                "У вас есть %s минут на то, чтобы перебить ставку.",
                        TonCoinUtils.toHumanReadable(newMaxBid),
                        prevHighestBid.getAuction().getMinutesToOutbid()
                ),
                createOkKeyboard(),
                prevHighestBid.getUser()
        );
    }
}
