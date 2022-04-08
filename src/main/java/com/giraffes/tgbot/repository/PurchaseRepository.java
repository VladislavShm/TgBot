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

    @Query("select count(purchase.id) from Purchase purchase where purchase.username = :username and purchase.approved = true")
    Integer approvedPurchasesCount(@Param("username") String username);
}
