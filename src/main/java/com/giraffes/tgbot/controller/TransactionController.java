package com.giraffes.tgbot.controller;

import com.giraffes.tgbot.model.TransactionDto;
import com.giraffes.tgbot.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final PurchaseService purchaseService;

    @PostMapping("/transactions")
    public void transactions(@RequestBody List<TransactionDto> transactions) {
        purchaseService.updatePurchases(transactions);
    }
}
