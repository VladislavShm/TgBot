package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Gift;
import com.giraffes.tgbot.entity.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {
    @Query("select count(g.id) from Gift g where g.user = :user")
    Integer giftsCount(@Param("user")TgUser tgUser);

    @Query("select coalesce(sum(number),0) from Gift")
    Integer getGiftedNFTQuantity();
}
