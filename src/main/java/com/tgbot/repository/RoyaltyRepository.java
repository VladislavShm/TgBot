package com.tgbot.repository;

import com.tgbot.entity.Royalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface RoyaltyRepository extends JpaRepository<Royalty, Long> {

    @Query(value = "SELECT sum(amount) FROM Royalty")
    BigInteger royaltySum();

    @Query(value = "SELECT sum(amount) FROM Royalty where wallet = :wallet")
    BigInteger royaltySum(String wallet);

    Optional<Royalty> findFirstByOrderByDateTimeDesc();
}
