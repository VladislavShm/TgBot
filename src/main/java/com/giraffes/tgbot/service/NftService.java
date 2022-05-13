package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.repository.NftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class NftService {
    private final NftRepository nftRepository;

    public List<String> getUserNFTLinks(TgUser tgUser) {
        return nftRepository.getUserNFTLinks(tgUser);
    }
}
