package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Nft;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.tonprovider.NftData;
import com.giraffes.tgbot.repository.NftRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NftService {
    private final NftRepository nftRepository;

    public List<Nft> getUserNFTs(TgUser tgUser) {
        return nftRepository.findAllByOwner(tgUser.getWallet());
    }

    public void updateNfts(List<NftData> nftDatas) {
        for (NftData nftData : nftDatas) {
            Nft nft = new Nft();
            nft.setAddress(nftData.getAddress());
            nft.setIndex(nftData.getIndex());
            nft.setOwner(nftData.getOwner());
            nftRepository.save(nft);
        }
    }

    public Integer maxIndex() {
        return nftRepository.maxIndex();
    }
}
