package com.giraffes.tgbot.screen;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.repository.TgUserRepository;
import com.giraffes.tgbot.service.PurchaseService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitialScreen implements ScreenProcessor {
    private final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");
    private final TgUserRepository tgUserRepository;
    private final PurchaseService purchaseService;
    private final TgUserService tgUserService;

    @Override
    public boolean shouldProcessIncomingMessage(Message message, String text) {
        return true;
    }

    @Override
    public BotApiMethod<?> processIncomingMessage(String text, String chatId, Message message) {
//        return SendMessage.builder()
//                .text("456")
//                .chatId(chatId)
//                .replyMarkup(ReplyKeyboardMarkup.builder().keyboardRow(new KeyboardRow(Arrays.asList(
//                        new KeyboardButton("Пригласить"),
//                        new KeyboardButton("Купить"),
//                        new KeyboardButton("Инвайт инфо")
//                ))).build()).build();

        TgUser tgUser = tgUserService.getCurrentUser();

        checkInvitation(text, tgUser);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Giraffes Capital\nНа данный момент Вы пригласили `" + tgUserRepository.invitedCount(tgUser) + "` человек")
                .replyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboard(Collections.singletonList(Arrays.asList(
                                                InlineKeyboardButton.builder()
                                                        .switchInlineQuery("Ваша персональная ссылка: \n\n\nhttps://t.me/Giraffe_capital_bot?start=" + tgUser.getId())
                                                        .text("Пригласить")
                                                        .build(),
                                                InlineKeyboardButton.builder()
                                                        .url(purchaseService.createLink(tgUser))
                                                        .text("Купить")
                                                        .build()
                                        ))
                                )
                                .build())
                .build();
    }

    private void checkInvitation(String text, TgUser tgUser) {
        if (!text.startsWith("/start") || text.length() <= "/start".length()) {
            return;
        }

        if (tgUser.getInvitedBy() != null) {
            log.warn("This user has already been invited: {} {}", text, tgUser);
            return;
        }

        String inviterId = text.substring("/start".length()).trim();
        if (!NUMBER_PATTERN.matcher(inviterId).find()) {
            log.warn("Unexpected /start parameter: {} {}", text, tgUser);
            return;
        }

        if (!tgUserService.isUserJustCreated()) {
            log.warn("User hasn't been just created: {}", tgUser);
            return;
        }

        Optional<TgUser> inviter = tgUserRepository.findById(Long.valueOf(inviterId));
        if (!inviter.isPresent()) {
            log.info("Invalid inviter ID: {}", inviterId);
            return;
        }

        tgUser.setInvitedBy(inviter.get());
    }

    @Override
    public boolean shouldProcessIncomingAction(String data) {
        return false;
    }

    @Override
    public BotApiMethod<? extends Serializable> processIncomingAction(Update update, String chatId, String data) {
        return null;
    }
}
