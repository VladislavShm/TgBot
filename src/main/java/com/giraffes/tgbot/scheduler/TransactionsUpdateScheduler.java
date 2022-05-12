package com.giraffes.tgbot.scheduler;

import com.giraffes.tgbot.model.tonprovider.TransactionDto;
import com.giraffes.tgbot.service.TonProviderService;
import com.giraffes.tgbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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
        List<TransactionDto> transactions = tonProviderService.getTransactions();
        transactionService.updateTransactions(transactions);
        log.info("Transactions have been updated");
    }
}
