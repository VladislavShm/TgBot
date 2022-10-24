package com.tgbot.service;

import com.tgbot.entity.Auction;
import com.tgbot.entity.TgUser;
import com.tgbot.entity.UserAuctionActivity;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.repository.UserAuctionActivityRepository;
import com.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

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

    public Optional<UserAuctionActivity> findHighestBid(Auction auction) {
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
                new Text(String.format(
                        "Ваша ставка была перебита!\n" +
                                "Текущая максимальная ставка: %s TON.\n" +
                                "У вас есть %s минут на то, чтобы перебить ставку.",
                        TonCoinUtils.toHumanReadable(newMaxBid),
                        prevHighestBid.getAuction().getMinutesToOutbid()
                )),
                new Keyboard(ButtonName.OkButton.OK_BUTTON),
                prevHighestBid.getUser()
        );
    }

    public List<UserAuctionActivity> findAllParticipantsExceptFor(UserAuctionActivity activity) {
        return userAuctionActivityRepository.findAllParticipantsExceptFor(activity.getAuction(), activity);
    }
}
