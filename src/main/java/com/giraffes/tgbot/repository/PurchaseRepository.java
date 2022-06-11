package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Purchase;
import com.giraffes.tgbot.entity.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @Query("select sum(purchase.quantity) from Purchase purchase where purchase.user = :user")
    Integer purchasesCount(@Param("user") TgUser user);

    @Query("select sum(purchase.quantity) from Purchase purchase")
    Integer totalPurchasesCount();
}
