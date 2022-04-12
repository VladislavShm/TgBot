package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import com.giraffes.tgbot.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.regex.Pattern;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createCancelButtonKeyboard;

@Component
@RequiredArgsConstructor
public class PurchaseLocationProcessor extends LocationProcessor {
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("^\\d+$");

    private final PurchaseService purchaseService;

    @Override
    public UserLocation getLocation() {
        return UserLocation.PURCHASE;
    }

    @Override
    @SneakyThrows
    public UserLocation processText(TgUser user, String text, boolean redirected) {
        if (redirected) {
            tgSender.execute(
                    SendMessage.builder()
                            .text("Пожалуйста, укажите количество \uD83E\uDD92 для покупки")
                            .chatId(user.getChatId())
                            .replyMarkup(createCancelButtonKeyboard())
                            .build()
            );

            return getLocation();
        }

        if ("Отмена".equals(text)) {
            return UserLocation.BASE;
        } else if (QUANTITY_PATTERN.matcher(text).find() && Integer.parseInt(text) > 0) {
            sendPurchaseLink(user, Integer.parseInt(text));
            return UserLocation.BASE;
        } else {
            sendInvalidInput(user);
        }

        return getLocation();
    }

    private void sendPurchaseLink(TgUser tgUser, Integer quantity) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text(purchaseService.createPurchaseMessage(tgUser, quantity))
                        .parseMode("html")
                        .chatId(tgUser.getChatId())
                        .replyMarkup(createCancelButtonKeyboard())
                        .build()
        );
    }

    private void sendInvalidInput(TgUser tgUser) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("Неверный формат.\n\nПожалуйста, укажите количество \uD83E\uDD92 для покупки")
                        .chatId(tgUser.getChatId())
                        .replyMarkup(createCancelButtonKeyboard())
                        .build()
        );
    }
}
