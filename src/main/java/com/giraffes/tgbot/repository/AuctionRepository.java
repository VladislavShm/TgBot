package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;

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
}
