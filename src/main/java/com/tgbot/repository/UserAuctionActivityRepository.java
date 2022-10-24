package com.tgbot.repository;

import com.tgbot.entity.Auction;
import com.tgbot.entity.TgUser;
import com.tgbot.entity.UserAuctionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuctionActivityRepository extends JpaRepository<UserAuctionActivity, Long> {

    UserAuctionActivity findByAuctionAndUser(Auction auction, TgUser user);

    UserAuctionActivity findByAuctionAndUserAndActiveIsTrue(Auction auction, TgUser user);

    @Query(
            "select uaa from UserAuctionActivity uaa join uaa.user u " +
                    "where uaa.auction = :auction and uaa.active = true and uaa.bid = " +
                    "(select max(uaa.bid) from UserAuctionActivity uaa where uaa.auction = :auction and uaa.active = true) " +
                    "and u.kicked = false"
    )
    Optional<UserAuctionActivity> findWithHighestBid(Auction auction);

    @Query(
            "select uaa from UserAuctionActivity uaa join uaa.user u " +
                    "where not uaa = :activity and uaa.auction = :auction and uaa.active = true and u.kicked = false"
    )
    List<UserAuctionActivity> findAllParticipantsExceptFor(Auction auction, UserAuctionActivity activity);
}
