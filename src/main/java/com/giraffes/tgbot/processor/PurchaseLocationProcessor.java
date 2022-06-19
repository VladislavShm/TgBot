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
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PurchaseLocationProcessor extends LocationProcessor {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

    private final PurchaseProperties purchaseProperties;
    private final PurchaseService purchaseService;

    @Override
    public Location getLocation() {
        return Location.PURCHASE;
    }

    @Override
    @SneakyThrows
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        Integer purchaseNftLeft = purchaseService.purchaseNftLeft();
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            sendDefaultMessage(user, purchaseNftLeft);
            return Optional.empty();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Optional.of(Location.BASE);
        }

        if (purchaseNftLeft <= 0) {
            telegramSenderService.send(
                    new Text("purchase.presale_sold", purchaseProperties.getLinkToMarketplace()),
                    new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                    user
            );

            return Optional.empty();
        }

        if (NUMBER_PATTERN.matcher(text).find()) {
            Integer quantity = Integer.valueOf(text);
            if (purchaseNftLeft < quantity) {
                telegramSenderService.send(
                        new Text("purchase.available_only", purchaseNftLeft),
                        new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                        user
                );
            } else {
                telegramSenderService.send(
                        purchaseService.createPurchaseMessage(user, quantity),
                        new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                        user
                );
            }
        }

        return Optional.empty();
    }

    private void sendDefaultMessage(TgUser user, Integer purchaseNftLeft) {
        telegramSenderService.send(
                new Text("purchase.base_message", purchaseNftLeft, purchaseProperties.getLinkToMarketplace()),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
