package com.giraffes.tgbot.scheduler;

import com.giraffes.tgbot.entity.Nft;
import com.giraffes.tgbot.model.ton.response.GetCollectionDataResult;
import com.giraffes.tgbot.model.ton.response.GetNftAddressByIndexResult;
import com.giraffes.tgbot.model.ton.response.GetNftDataResult;
import com.giraffes.tgbot.model.ton.response.GetSaleDataResult;
import com.giraffes.tgbot.model.ton.response.gettransactions.GetTransactionsResponseDto;
import com.giraffes.tgbot.model.ton.response.getwalletinfo.GetWalletInfoDto;
import com.giraffes.tgbot.property.NftCollectionProperties;
import com.giraffes.tgbot.service.AmqpSender;
import com.giraffes.tgbot.service.NftService;
import com.giraffes.tgbot.service.ton.TonService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "nft-data-synchronization-scheduler.enabled", matchIfMissing = true)
public class NftDataSynchronizationScheduler {
    private final NftCollectionProperties nftCollectionProperties;
    private final TonService tonService;
    private final NftService nftService;
    private final AmqpSender amqpSender;

    @Transactional
    @Scheduled(fixedDelay = 120000, initialDelay = 1000)
    public void synchronizingNftData() {
        log.info("Synchronizing NFT data");

        int totalNftNumber = (int) nftService.totalNftNumber();
        GetCollectionDataResult collectionData = tonService.getCollectionData(nftCollectionProperties.getAddress());
        for (int i = totalNftNumber; i < collectionData.getNextNftIndex(); i++) {
            GetNftAddressByIndexResult nftAddressByIndex = tonService.getNftAddressByIndex(nftCollectionProperties.getAddress(), i);
            nftService.createNewNft(i, nftAddressByIndex.getAddress().toString());
        }

        nftService.findAll()
                .stream()
                .sorted(Comparator.comparing(Nft::getIndex))
                .forEachOrdered(this::updateNftData);

        log.info("NFTs have been synchronized");
    }

    private void updateNftData(Nft nft) {
        try {
            log.info("Processing NFT: " + nft.getIndex());
            GetNftDataResult getNftDataResult = tonService.getNftData(nft.getAddress());

            String currentNftOwner = nft.getOwner();
            String newOwner = getNftDataResult.getOwner().toString(true, true, true, false);
            if (Objects.equals(newOwner, currentNftOwner)) {
                return;
            }

            if (shouldUpdatePrice(currentNftOwner, newOwner, nft.getIndex())) {
                updateSalePrice(nft, newOwner);
            }
        } catch (Exception e) {
            log.error("Error while updating NFT {}.", nft.getIndex(), e);
        }
    }

    private boolean shouldUpdatePrice(String currentNftOwner, String newOwner, int nftIndex) {
        GetWalletInfoDto newOwnerWalletInfo = tonService.getWalletInfo(newOwner);
        if (newOwnerWalletInfo.isWallet()) { // Check if current owner is wallet
            return true;
        } else { // Otherwise, most likely it is a sale contract
            GetSaleDataResult saleData = tonService.getSaleData(newOwner);
            if (saleData.isSale()) { // If it is a sale contract then determine a real owner
                if (saleData.getOwner() != null) {
                    String nftSellerAddress = saleData.getOwner().toString(true, true, true, false);
                    if (Objects.equals(currentNftOwner, nftSellerAddress)) {
                        return false;
                    }

                    return tonService.getWalletInfo(nftSellerAddress).isWallet();
                } else {
                    log.warn("Sale data owner is empty for NFT: {}", nftIndex);
                }
            } else {
                log.warn("Don't know what to do with NFT: {}", nftIndex);
            }
        }

        return false;
    }

    private void updateSalePrice(Nft nft, String newOwner) {
        getSaleFullPrice(nft, newOwner)
                .ifPresentOrElse(
                        newSalePrice -> {
                            updateNftOwnerAndLastValue(nft, newOwner, newSalePrice);
                            amqpSender.sendNftOwnerChanged(nft.getId());
                        },
                        () -> {
                            updateNftOwnerAndLastValue(nft, newOwner, null);
                            log.warn("Can't extract sale price for NFT {}", nft.getIndex());
                        }
                );
    }

    private void updateNftOwnerAndLastValue(Nft nft, String newOwner, BigInteger salePrice) {
        String newValue = Optional.ofNullable(salePrice).map(TonCoinUtils::toHumanReadable).orElse(null);
        String oldValue = Optional.ofNullable(nft.getLastValue()).map(TonCoinUtils::toHumanReadable).orElse(null);
        log.debug("Updating NFT {} owner {} -> {} and last value {} -> {}", nft.getIndex(), nft.getOwner(), newOwner, oldValue, newValue);

        nft.setOwner(newOwner);
        nft.setLastValue(salePrice);
    }

    private Optional<BigInteger> getSaleFullPrice(Nft nft, String owner) {
        String nftAddress = nft.getAddress();
        return tonService.getTransactions(nftAddress)
                .stream()
                .sorted((o1, o2) -> Long.compare(o2.getUtime(), o1.getUtime()))
                .filter(transaction -> isSaleContractCandidate(owner, nftAddress, transaction))
                .map(transaction -> transaction.getInMsg().getSource())
                .flatMap(potentialSale -> {
                    GetSaleDataResult saleData = tonService.getSaleData(potentialSale);
                    if (saleData.isSale()) {
                        return tonService.getTransactions(potentialSale)
                                .stream()
                                .sorted((o1, o2) -> Long.compare(o2.getUtime(), o1.getUtime()))
                                .filter(transaction -> isSaleSettled(transaction, owner, nftAddress))
                                .map(transaction -> transaction.getInMsg().getValue());
                    } else {
                        log.info("Skipping {} since it is not sale", potentialSale);
                        return Stream.empty();
                    }
                })
                .findFirst();
    }

    private boolean isSaleContractCandidate(String owner, String nftAddress, GetTransactionsResponseDto transaction) {
        if (transaction.getInMsg().getDestination().equals(nftAddress)) {
            for (GetTransactionsResponseDto.OutMsg msg : transaction.getOutMsgs()) {
                if (Objects.equals(msg.getSource(), nftAddress) && Objects.equals(msg.getDestination(), owner)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isSaleSettled(GetTransactionsResponseDto transaction, String owner, String nftAddress) {
        // Looking for a transaction which was sent by the owner
        if (transaction.getInMsg().getSource().equals(owner)) {
            // Usually we have 4 out msgs (2 royalties, 1 msg to contract, 1 msg to the previous owner), but let's expect 2 for a while...
            if (transaction.getOutMsgs().size() > 2) {
                // At least 1 msgs was sent to an NFT address
                if (transaction.getOutMsgs().stream().anyMatch(outMsg -> outMsg.getDestination().equals(nftAddress))) {
                    // Assume that the minimum price is 3 TON
                    return transaction.getInMsg().getValue().compareTo(TonCoinUtils.fromHumanReadable("3")) > 0;
                }
            }
        }

        return false;
    }
}
