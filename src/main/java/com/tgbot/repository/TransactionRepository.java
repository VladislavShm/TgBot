package com.tgbot.repository;

import com.tgbot.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findFirstByOrderByDatetimeDesc();

    @Query("select sum(amount) from Transaction " +
            "where senderType = 'NFT_SALE' and id not in (" +
            "    select tr1.id from Transaction tr1 inner join Transaction tr2 " +
            "       on tr1.sender = tr2.sender and not tr1.id = tr2.id" +
            "       and tr2.senderType = 'NFT_SALE' and tr1.amount > tr2.amount" +
            "       where tr1.senderType = 'NFT_SALE')")
    Optional<BigInteger> sumOfRoyalties();
}
