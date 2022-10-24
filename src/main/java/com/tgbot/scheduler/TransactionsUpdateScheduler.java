package com.tgbot.scheduler;

import com.tgbot.model.tonprovider.TransactionDto;
import com.tgbot.service.TonProviderService;
import com.tgbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transactions-update-scheduler.enabled", matchIfMissing = true)
public class TransactionsUpdateScheduler {
    private final TonProviderService tonProviderService;
    private final TransactionService transactionService;

    @Scheduled(fixedDelay = 5000, initialDelay = 2000)
    public void updateTransactions() {
        log.info("Updating transactions");
        Optional<Long> lastLt = transactionService.lastTransactionLt();
        List<TransactionDto> transactions = tonProviderService.getTransactions(lastLt);
        transactionService.updateTransactions(transactions);
        log.info("Transactions have been updated");
    }
}
