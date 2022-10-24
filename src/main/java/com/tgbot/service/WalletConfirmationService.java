package com.tgbot.service;

import com.tgbot.entity.TgUser;
import com.tgbot.entity.Transaction;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.property.UserWalletProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletConfirmationService {
    private final TelegramSenderService telegramSenderService;
    private final UserWalletProperties userWalletProperties;
    private final TonLinkService tonLinkService;
    private final TgUserService tgUserService;

    public boolean isWalletConfirmation(Transaction transaction) {
        return Objects.equals(transaction.getAmount(), userWalletProperties.getConfirmationSum()) &&
                !tgUserService.findAllByWalletNotConfirmed(transaction.getSender()).isEmpty();
    }

    public void confirmWallet(Transaction transaction) {
        for (TgUser user : tgUserService.findAllByWalletNotConfirmed(transaction.getSender())) {
            try {
                user.setWalletConfirmed(true);
                sendNotification(user);
            } catch (Exception e) {
                log.error("Error while confirming user wallet: {}. ", user, e);
            }
        }
    }

    public String createLink() {
        return tonLinkService.createLink(userWalletProperties.getConfirmationWallet(), userWalletProperties.getConfirmationSum());
    }

    private void sendNotification(TgUser user) {
        telegramSenderService.send(
                new Text(String.format("Ваш кошелек <b><code>%s</code></b> был подтвержден", user.getWallet())),
                new Keyboard(ButtonName.OkButton.OK_BUTTON),
                user
        );
    }
}
