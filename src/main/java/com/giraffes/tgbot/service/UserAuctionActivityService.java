package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.repository.UserAuctionActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuctionActivityService {
    private final UserAuctionActivityRepository userAuctionActivityRepository;

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
}
