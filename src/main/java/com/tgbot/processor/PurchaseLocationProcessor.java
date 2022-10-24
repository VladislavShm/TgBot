package com.tgbot.processor;

import com.tgbot.entity.Location;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.property.PurchaseProperties;
import com.tgbot.service.PurchaseService;
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
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            if (purchaseProperties.isEnabled()) {
                sendBasePurchaseMessage(user);
            } else {
                sendPresaleFinished(user);
            }
            return Optional.empty();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Optional.of(Location.BASE);
        }

        if (!purchaseProperties.isEnabled()) {
            sendPresaleFinished(user);
            return Optional.empty();
        }

        Integer purchaseNftLeft = purchaseService.purchaseNftLeft();
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

    private void sendBasePurchaseMessage(TgUser user) {
        Integer purchaseNftLeft = purchaseService.purchaseNftLeft();
        telegramSenderService.send(
                new Text("purchase.base_message", purchaseNftLeft, purchaseProperties.getLinkToMarketplace()),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }

    private void sendPresaleFinished(TgUser user) {
        telegramSenderService.send(
                new Text("purchase.presale_finished"),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
