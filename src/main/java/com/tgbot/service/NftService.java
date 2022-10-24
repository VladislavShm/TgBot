package com.tgbot.service;

import com.tgbot.entity.Nft;
import com.tgbot.entity.TgUser;
import com.tgbot.repository.NftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NftService {
    private final NftRepository nftRepository;

    public List<Nft> getUserNFTs(TgUser tgUser) {
        return StringUtils.isNotBlank(tgUser.getWallet())
                ? nftRepository.findAllByOwner(tgUser.getWallet())
                : Collections.emptyList();
    }

    public void createNewNft(int index, String nftAddress) {
        log.debug("Creating new NFT: {}, {}", index, nftAddress);
        Nft newNft = new Nft();
        newNft.setAddress(nftAddress);
        newNft.setIndex(index);
        nftRepository.save(newNft);
    }

    public Nft getNftById(Long nftId) {
        return nftRepository.getById(nftId);
    }

    public int getNftRank(Nft nft) {
        return nftRepository.getNumberNftRarityHigherOrEqual(nft.getRarity());
    }

    public boolean isAnyRarityNull() {
        return nftRepository.isAnyRarityNull();
    }

    public Integer getLastIndex() {
        return nftRepository.lastIndex();
    }

    public List<Nft> findAll() {
        return nftRepository.findAll();
    }

    public long totalNftNumber() {
        return nftRepository.count();
    }

    public Optional<Nft> findByIndex(Integer index) {
        return nftRepository.findByIndex(index);
    }

    public Integer findUserNftCount(TgUser tgUser) {
        return ObjectUtils.defaultIfNull(nftRepository.findCountByOwner(tgUser.getWallet()), 0);
    }

    public Integer totalNftNumberWithOwner() {
        return nftRepository.totalNftNumberWithOwner().orElse(0);
    }
}
