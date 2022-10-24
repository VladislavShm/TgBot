package com.tgbot.scheduler;

import com.tgbot.entity.Nft;
import com.tgbot.model.NftMetadata;
import com.tgbot.service.NftService;
import com.tgbot.service.PCloudProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class NftRaritySynchronizationScheduler {
    private final PCloudProvider pCloudProvider;
    private final NftService nftService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 1000)
    public void synchronizingNftRarities() {
        log.info("Synchronizing NFTs rarities");

        if (!nftService.isAnyRarityNull()) {
            log.info("All NFT rarities are already synchronized");
            return;
        }

        Integer lastIndex = nftService.getLastIndex();
        List<Integer> indexes = IntStream.rangeClosed(0, lastIndex).boxed().collect(Collectors.toList());
        Map<Integer, NftMetadata> metadataByIndex = pCloudProvider.metadataByIndexes(indexes);

        Map<String, Map<String, Long>> frequencies =
                metadataByIndex
                        .values()
                        .stream()
                        .map(NftMetadata::getAttributes)
                        .flatMap(Collection::stream)
                        .collect(
                                groupingBy(
                                        NftMetadata.Attribute::getTraitType,
                                        groupingBy(
                                                NftMetadata.Attribute::getValue,
                                                counting()
                                        )
                                )
                        );

        Map<Integer, Nft> nftByIndex =
                nftService
                        .findAll()
                        .stream()
                        .collect(Collectors.toMap(Nft::getIndex, nft -> nft));

        BigDecimal nftNumber = BigDecimal.valueOf(indexes.size());
        metadataByIndex.forEach((index, metadata) -> {

            BigDecimal rarity = metadata.getAttributes().stream()
                    .map(attribute -> frequencies.get(attribute.getTraitType()).get(attribute.getValue()))
                    .map(BigDecimal::valueOf)
                    .map(frequency -> nftNumber.divide(frequency, 6, RoundingMode.CEILING))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            nftByIndex.get(index).setRarity(rarity.setScale(3, RoundingMode.CEILING));
        });

        log.info("NFTs rarities have been updated");
    }
}
