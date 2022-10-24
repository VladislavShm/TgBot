package com.tgbot.repository;

import com.tgbot.entity.Nft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface NftRepository extends JpaRepository<Nft, Long> {
    List<Nft> findAllByOwner(String owner);

    Optional<Nft> findByIndex(Integer index);

    @Query("select count(nft.id) > 0 from Nft nft where nft.rarity is null")
    boolean isAnyRarityNull();

    @Query("select max(nft.index) from Nft nft")
    Integer lastIndex();

    @Query("select count(nft.id) from Nft nft where nft.rarity >= :rarity")
    int getNumberNftRarityHigherOrEqual(BigDecimal rarity);

    @Query("select count(nft.id) from Nft nft where nft.owner = :owner")
    int findCountByOwner(String owner);

    @Query("select count(nft.id) from Nft nft where nft.owner is not null")
    Optional<Integer> totalNftNumberWithOwner();
}
