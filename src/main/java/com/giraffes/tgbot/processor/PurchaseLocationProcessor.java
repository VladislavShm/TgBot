package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import com.giraffes.tgbot.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

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
            telegramSenderService.send(
                    "Пожалуйста, укажите количество \uD83E\uDD92 для покупки",
                    createCancelButtonKeyboard()
            );

            return getLocation();
        }

        if ("Отмена".equals(text)) {
            return UserLocation.BASE;
        } else if (QUANTITY_PATTERN.matcher(text).find() && Integer.parseInt(text) > 0) {
            sendPurchaseLink(user, Integer.parseInt(text));
            return UserLocation.BASE;
        } else {
            sendInvalidInput();
        }

        return getLocation();
    }

    private void sendPurchaseLink(TgUser tgUser, Integer quantity) {
        telegramSenderService.send(
                purchaseService.createPurchaseMessage(tgUser, quantity),
                createCancelButtonKeyboard()
        );
    }

    private void sendInvalidInput() {
        telegramSenderService.send(
                "Неверный формат.\n\nПожалуйста, укажите количество \uD83E\uDD92 для покупки",
                createCancelButtonKeyboard()
        );
    }
}
