package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Nft;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.tonprovider.NftData;
import com.giraffes.tgbot.repository.NftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NftService {
    private final NftRepository nftRepository;

    public List<Nft> getUserNFTs(TgUser tgUser) {
        return nftRepository.findAllByOwner(tgUser.getWallet());
    }

    public void createOrUpdateNfts(List<NftData> nftDatas) {
        nftDatas.forEach(
                nftData ->
                        nftRepository.findByIndex(nftData.getIndex())
                                .ifPresentOrElse(
                                        nft -> updateNftOwnerIfNecessary(nftData, nft),
                                        () -> createNewNft(nftData)
                                )
        );
    }

    private void updateNftOwnerIfNecessary(NftData nftData, Nft nft) {
        if (!StringUtils.equals(nft.getOwner(), nftData.getOwner())) {
            log.debug("NFT owner has been changed {} -> {}. Index: {}", nft.getOwner(), nftData.getOwner(), nft.getIndex());
            nft.setOwner(nftData.getOwner());
        }
    }

    private void createNewNft(NftData nftData) {
        log.debug("Creating new NFT: {}", nftData);
        Nft newNft = new Nft();
        newNft.setAddress(nftData.getAddress());
        newNft.setIndex(nftData.getIndex());
        newNft.setOwner(nftData.getOwner());
        nftRepository.save(newNft);
    }
}
