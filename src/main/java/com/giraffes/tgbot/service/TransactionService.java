package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Transaction;
import com.giraffes.tgbot.model.tonprovider.TransactionDto;
import com.giraffes.tgbot.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AuctionService auctionService;
    private final PurchaseService purchaseService;
    private final TransactionRepository transactionRepository;
    private final WalletConfirmationService walletConfirmationService;

    @Transactional
    public void updateTransactions(List<TransactionDto> transactionDtos) {
        Set<String> registeredTransactionIds = transactionRepository.findAllTransactionIds();
        List<Transaction> newTransactions = transactionDtos.stream()
                .filter(dto -> !registeredTransactionIds.contains(dto.getTransactionId()))
                .map(this::createNewTransaction)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (!newTransactions.isEmpty()) {
            log.debug("Registered {} new transactions", newTransactions.size());
            processTransactions(newTransactions);
        }
    }

    private void processTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            try {
                if (purchaseService.isPurchaseTransaction(transaction)) {
                    purchaseService.registerPurchase(transaction);
                } else if (walletConfirmationService.isWalletConfirmation(transaction)) {
                    walletConfirmationService.confirmWallet(transaction);
                } else if (auctionService.isAuctionTransaction(transaction)) {
                    auctionService.processAuctionPay(transaction);
                } else {
                    log.warn("Can't find out what to do with transaction ID={}", transaction.getId());
                }
            } catch (Exception e) {
                log.error("Error while processing transaction {}. ", transaction.getId(), e);
            }
        }
    }

    private Optional<Transaction> createNewTransaction(TransactionDto dto) {
        log.info("Registering new transaction: {}", dto.getTransactionId());
        LocalDateTime datetime;
        try {
            datetime = Instant.ofEpochSecond(dto.getDatetime())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
        } catch (Exception e) {
            log.error("Can't parse incoming datetime {} for {}. Skipping transaction.", dto.getDatetime(), dto.getTransactionId(), e);
            return Optional.empty();
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getValue());
        transaction.setDatetime(datetime);
        transaction.setFromWallet(dto.getSender());
        transaction.setTransactionId(dto.getTransactionId());
        transaction.setText(dto.getText());
        transaction = transactionRepository.save(transaction);
        log.debug("Created transaction: {}", transaction);
        return Optional.of(transaction);
    }
}
