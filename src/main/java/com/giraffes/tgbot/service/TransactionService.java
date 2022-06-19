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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Map<String, Transaction> transactionById = transactionRepository.findAll()
                .stream().collect(Collectors.toMap(Transaction::getTransactionId, t -> t));

        List<Transaction> newTransactions = new ArrayList<>();
        for (TransactionDto dto : transactionDtos) {
            Optional.ofNullable(transactionById.get(dto.getTransactionId()))
                    .ifPresentOrElse(
                            (transaction) -> updateTransactionTransaction(dto, transaction),
                            () -> newTransactions.add(createNewTransaction(dto))
                    );
        }

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

    private Transaction createNewTransaction(TransactionDto dto) {
        log.info("Registering new transaction: {}", dto.getTransactionId());
        LocalDateTime datetime = Instant.ofEpochSecond(dto.getDatetime())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getValue());
        transaction.setDatetime(datetime);
        transaction.setSender(dto.getSender());
        transaction.setTransactionId(dto.getTransactionId());
        transaction.setText(dto.getText());
        transaction.setToWallet(dto.getToWallet());
        transaction.setToWalletType(dto.getToWalletType());
        transaction.setHash(dto.getHash());
        transaction.setSenderType(dto.getSenderType());
        transaction = transactionRepository.save(transaction);
        log.debug("Created transaction: {}", transaction);
        return transaction;
    }

    private void updateTransactionTransaction(TransactionDto dto, Transaction transaction) {
        // Update only fields that may be empty
        transaction.setToWallet(dto.getToWallet());
        transaction.setToWalletType(dto.getToWalletType());
        transaction.setHash(dto.getHash());
        transaction.setSenderType(dto.getSenderType());
    }
}
