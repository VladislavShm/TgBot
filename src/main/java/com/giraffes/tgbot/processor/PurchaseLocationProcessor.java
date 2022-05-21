package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.ButtonName;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.property.PurchaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PurchaseLocationProcessor extends LocationProcessor {

    private final PurchaseProperties purchaseProperties;

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
        }

        return Optional.empty();
    }

    private void sendDefaultMessage(TgUser user) {
        telegramSenderService.send(
                new Text(String.format("Пожалуйста, воспользуйтесь маркетлейсом Disintar для покупки нашей NFT: %s", purchaseProperties.getLinkToMarketplace())),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
