package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuctionActivityRepository extends JpaRepository<UserAuctionActivity, Long> {

    UserAuctionActivity findByAuctionAndUser(Auction auction, TgUser user);

    UserAuctionActivity findByAuctionAndUserAndActiveIsTrue(Auction auction, TgUser user);

    @Query(
            "select uaa from UserAuctionActivity uaa " +
                    "where uaa.auction = :auction and uaa.active = true and uaa.bid = " +
                    "(select max(uaa.bid) from UserAuctionActivity uaa where uaa.auction = :auction and uaa.active = true)"
    )
    UserAuctionActivity findWithHighestBid(Auction auction);
}
