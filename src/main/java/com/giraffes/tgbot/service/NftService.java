package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Nft;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.tonprovider.NftData;
import com.giraffes.tgbot.repository.NftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NftService {
    private final NftRepository nftRepository;
    private final AmqpSender amqpSender;

    public List<Nft> getUserNFTs(TgUser tgUser) {
        return StringUtils.isNotBlank(tgUser.getWallet())
                ? nftRepository.findAllByOwner(tgUser.getWallet())
                : Collections.emptyList();
    }

    public void createOrUpdateNfts(NftData nftData) {
        nftRepository.findByIndex(nftData.getIndex())
                .ifPresentOrElse(
                        nft -> updateNftOwnerIfNecessary(nftData, nft),
                        () -> createNewNft(nftData)
                );
    }

    private void updateNftOwnerIfNecessary(NftData nftData, Nft nft) {
        if (!StringUtils.equals(nft.getOwner(), nftData.getOwner())) {
            log.debug("NFT owner has been changed {} -> {}, value {} -> {}. Index: {}", nft.getOwner(), nftData.getOwner(), nft.getLastValue(), nftData.getValue(), nft.getIndex());
            nft.setOwner(nftData.getOwner());
            nft.setLastValue(nftData.getValue());
            amqpSender.sendNftOwnerChanged(nft.getId());
        } else if (!Objects.deepEquals(nftData.getValue(), nft.getLastValue())) {
            log.debug("NFT value has been changed value: {} -> {}. Index: {}", nft.getLastValue(), nftData.getValue(), nft.getIndex());
            nft.setLastValue(nftData.getValue());
        }
    }

    private void createNewNft(NftData nftData) {
        log.debug("Creating new NFT: {}", nftData);
        Nft newNft = new Nft();
        newNft.setAddress(nftData.getAddress());
        newNft.setIndex(nftData.getIndex());
        newNft.setOwner(nftData.getOwner());
        newNft.setLastValue(nftData.getValue());
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
}
