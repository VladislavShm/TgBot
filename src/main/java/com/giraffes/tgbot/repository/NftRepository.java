package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.Nft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NftRepository extends JpaRepository<Nft, Long> {
    List<Nft> findAllByOwner(String owner);

    Optional<Nft> findByIndex(Integer index);
}
