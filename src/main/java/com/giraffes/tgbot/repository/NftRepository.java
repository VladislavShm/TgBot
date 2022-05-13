package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Nft;
import com.giraffes.tgbot.entity.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NftRepository extends JpaRepository<Nft, Long> {
    @Query("select nft.link from Nft nft where nft.user = :user")
    List<String> getUserNFTLinks(@Param("user")TgUser tgUser);
}
