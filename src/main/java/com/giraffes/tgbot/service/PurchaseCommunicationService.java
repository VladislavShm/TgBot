package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseCommunicationService {
    private final PurchaseRepository purchaseRepository;
    private final TgUserService tgUserService;
    private final AbsSender tgSender;

    @SneakyThrows
    public void sendPurchaseNotification(String username) {
        TgUser tgUser = tgUserService.findByUsername(username);
        if (tgUser == null) {
            log.warn("User {} wasn't found for sending the notification", username);
            return;
        }

        tgSender.execute(
                SendMessage.builder()
                        .text("Спасибо за покупку! На данный момент у вас имеется " + purchaseRepository.approvedPurchasesCount(username) + " жирафов")
                        .parseMode("html")
                        .chatId(tgUser.getChatId())
                        .replyMarkup(createButtons())
                        .build()
        );
    }

    private ReplyKeyboardMarkup createButtons() {
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(
                        new KeyboardRow(
                                Arrays.asList(
                                        new KeyboardButton("Купить"),
                                        new KeyboardButton("Инвайт инфо"),
                                        new KeyboardButton("Мои жирафы")
                                )
                        )
                )
                .resizeKeyboard(true)
                .build();
    }
}
