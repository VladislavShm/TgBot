package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.ButtonName;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.property.PurchaseProperties;
import com.giraffes.tgbot.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PurchaseLocationProcessor extends LocationProcessor {
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("^\\d+$");

    private final PurchaseProperties purchaseProperties;
    private final PurchaseService purchaseService;

    @Override
    public Location getLocation() {
        return Location.PURCHASE;
    }

    @Override
    @SneakyThrows
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            sendDefaultMessage(user);
            return Optional.empty();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Optional.of(Location.BASE);
        } else if (StringUtils.isBlank(purchaseProperties.getLinkToMarketplace())) {
            if (QUANTITY_PATTERN.matcher(text).find() && Integer.parseInt(text) > 0) {
                sendPurchaseLink(user, Integer.parseInt(text));
            } else {
                sendInvalidInput(user);
            }

            return Optional.empty();
        }

        return Optional.empty();
    }

    private void sendDefaultMessage(TgUser user) {
        if (StringUtils.isNotBlank(purchaseProperties.getLinkToMarketplace())) {
            telegramSenderService.send(
                    new Text(String.format("Пожалуйста, воспользуйтесь маркетлейсом Disintar для покупки нашей NFT: %s", purchaseProperties.getLinkToMarketplace())),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
            );
        } else {
            telegramSenderService.send(
                    new Text("Пожалуйста, укажите количество \uD83E\uDD92 для покупки"),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
            );
        }
    }

    private void sendPurchaseLink(TgUser user, Integer quantity) {
        telegramSenderService.send(
                new Text(purchaseService.createPurchaseMessage(user, quantity)),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }

    private void sendInvalidInput(TgUser user) {
        telegramSenderService.send(
                new Text("Неверный формат.\n\nПожалуйста, укажите количество \uD83E\uDD92 для покупки"),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
