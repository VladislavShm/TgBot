package com.giraffes.tgbot.scheduler;

import com.giraffes.tgbot.model.tonprovider.NftData;
import com.giraffes.tgbot.service.NftService;
import com.giraffes.tgbot.service.TonProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "nft-data-synchronization-scheduler.enabled", matchIfMissing = true)
public class NftDataSynchronizationScheduler {
    private final NftService nftService;
    private final TonProviderService tonProviderService;

    @Scheduled(fixedDelay = 60000, initialDelay = 1000)
    public void synchronizingNftData() {
        log.info("Synchronizing NFT data");
        updateNftsStartingFrom(ObjectUtils.defaultIfNull(nftService.maxIndex() + 1, 0));
        log.info("NFTs have been synchronized");
    }

    private void updateNftsStartingFrom(Integer index) {
        Optional.of(tonProviderService.getNftData(index))
                .filter(list -> !list.isEmpty())
                .stream()
                .peek(nftService::updateNfts)
                .flatMap(Collection::stream)
                .map(NftData::getIndex)
                .max(Integer::compareTo)
                .map(lastIndex -> lastIndex + 1)
                .ifPresent(this::updateNftsStartingFrom);
    }
}
