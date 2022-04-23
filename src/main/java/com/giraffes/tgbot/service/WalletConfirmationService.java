package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.Transaction;
import com.giraffes.tgbot.property.UserWalletProperties;
import com.giraffes.tgbot.utils.TelegramUiUtils;
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
                !tgUserService.findAllByWalletNotConfirmed(transaction.getFromWallet()).isEmpty();
    }

    public void confirmWallet(Transaction transaction) {
        for (TgUser user : tgUserService.findAllByWalletNotConfirmed(transaction.getFromWallet())) {
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
                String.format("Ваш кошелек <b><code>%s</code></b> был подтвержден", user.getWallet()),
                TelegramUiUtils.createOkKeyboard(),
                user.getChatId()
        );
    }
}
