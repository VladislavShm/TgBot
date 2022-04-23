package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("select transaction.transactionId from Transaction transaction")
    Set<String> findAllTransactionIds();
}
