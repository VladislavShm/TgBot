package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
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
    public Location getLocation() {
        return Location.PURCHASE;
    }

    @Override
    @SneakyThrows
    protected Location processText(TgUser user, String text, boolean redirected) {
        if (redirected) {
            askToSpecifyQuantityOfNft();
        } else if ("Отмена".equals(text)) {
            return Location.BASE;
        } else if (QUANTITY_PATTERN.matcher(text).find() && Integer.parseInt(text) > 0) {
            sendPurchaseLink(user, Integer.parseInt(text));
            return Location.BASE;
        } else if ("Ок".equals(text)) {
            askToSpecifyQuantityOfNft();
        } else {
            sendInvalidInput();
        }

        return getLocation();
    }

    private void askToSpecifyQuantityOfNft() {
        telegramSenderService.send(
                "Пожалуйста, укажите количество \uD83E\uDD92 для покупки",
                createCancelButtonKeyboard()
        );
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
