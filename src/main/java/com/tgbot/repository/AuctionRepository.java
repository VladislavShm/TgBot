package com.tgbot.repository;

import com.tgbot.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Query("select a from Auction a where a.finishDateTime is null and a.published = true and a.startDateTime <= :now")
    List<Auction> findActive(@Param("now") LocalDateTime localDateTime);

    @Query("select a from Auction a where a.finishDateTime is null and a.published = true and a.startDateTime > :now")
    List<Auction> findUpcoming(@Param("now") LocalDateTime localDateTime);

    Auction findByOrderNumberAndPublishedIsTrueAndFinishDateTimeIsNull(Integer orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    Auction queryLockedById(Long id);

    @Query("select a from UserAuctionActivity uaa join uaa.auction a join uaa.user u " +
            "where a.finishDateTime is not null " +
            "   and a.published = true " +
            "   and a.coinsPaid = false" +
            "   and uaa.bid = :value" +
            "   and uaa.active = true" +
            "   and u.wallet = :wallet" +
            "   and u.walletConfirmed = true")
    Optional<Auction> findFinishedNotPaidByHighestBidAndUserWallet(@Param("value") BigInteger value, @Param("wallet") String wallet);
}
