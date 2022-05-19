package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Nft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NftRepository extends JpaRepository<Nft, Long> {
    List<Nft> findAllByOwner(String owner);

    @Query("select max(nft.index) from Nft nft")
    Integer maxIndex();
}
