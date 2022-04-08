package com.giraffes.tgbot.screen;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.repository.TgUserRepository;
import com.giraffes.tgbot.service.PurchaseService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitialScreen implements ScreenProcessor {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");
    private final TgUserRepository tgUserRepository;
    private final PurchaseService purchaseService;
    private final TgUserService tgUserService;

    @Autowired
    private AbsSender tgSender;

    @Override
    public boolean shouldProcessIncomingMessage(Message message, String text) {
        return true;
    }

    @Override
    @SneakyThrows
    public BotApiMethod<?> processIncomingMessage(String text, String chatId, Message message) {
        TgUser tgUser = tgUserService.getCurrentUser();

        if ("Купить".equals(text)) {
            sendPurchaseLink(chatId, tgUser);
            return null;
        } else if ("Инвайт инфо".equals(text)) {
            sendInviteInfo(chatId, tgUser);
            return null;
        } else if ("Мои жирафы".equals(text)) {
            sendMyGiraffesInfo(chatId, tgUser);
            return null;
        }

        checkInvitation(text, tgUser);

        return SendMessage.builder()
                .text("Giraffes Capital \uD83E\uDD92\uD83E\uDD92\uD83E\uDD92\n\nНаш канал (https://t.me/giraffe_capital)")
                .chatId(chatId)
                .replyMarkup(createButtons())
                .build();
    }

    private void sendMyGiraffesInfo(String chatId, TgUser tgUser) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("На данный момент Вы приобрели <i><b>" + purchaseService.purchasesCount(tgUser) + "</b></i> жирафов.\n\n" +
                                "В случае, если количество жирафов отличается от ожидаемого, пожалуйста, свяжитесь с нами.\n" +
                                "Как правило, проведение транзакции и олучение данных о Вашем переводе средств занимают некоторое время.")
                        .parseMode("html")
                        .chatId(chatId)
                        .replyMarkup(createButtons())
                        .build()
        );
    }

    private void sendInviteInfo(String chatId, TgUser tgUser) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("На данный момент Вы пригласили <i><b>" + tgUserRepository.invitedCount(tgUser) + "</b></i> человек")
                        .parseMode("html")
                        .chatId(chatId)
                        .replyMarkup(createButtons())
                        .build()
        );

        tgSender.execute(
                SendMessage.builder()
                        .text("Ваша персональная ссылка: \n\n\nhttps://t.me/Giraffe_capital_bot?start=" + tgUser.getId())
                        .chatId(chatId)
                        .replyMarkup(createButtons())
                        .build()
        );
    }

    private void sendPurchaseLink(String chatId, TgUser tgUser) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("Ссылка для покупки NFT: " + purchaseService.createLink(tgUser))
                        .chatId(chatId)
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
