package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @Query("select purchase.transactionId from Purchase purchase")
    List<String> findAllTransactionIds();

    @Query("select sum(purchase.number) from Purchase purchase where purchase.chatId = :chatId and purchase.approved = true")
    Integer approvedPurchasesCount(@Param("chatId") String chatId);
}
