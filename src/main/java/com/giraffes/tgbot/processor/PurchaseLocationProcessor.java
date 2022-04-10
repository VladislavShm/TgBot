package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import com.giraffes.tgbot.service.PurchaseService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.regex.Pattern;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createCancelButton;

@Component
@RequiredArgsConstructor
public class PurchaseLocationProcessor implements LocationProcessor {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

    private final PurchaseService purchaseService;
    private final TgUserService tgUserService;

    @Autowired
    private AbsSender tgSender;

    @Override
    public UserLocation getLocation() {
        return UserLocation.PURCHASE;
    }

    @Override
    @SneakyThrows
    public UserLocation process(Update update, boolean redirected) {
        TgUser tgUser = tgUserService.getCurrentUser();
        if (redirected) {
            tgUser.setLocation(getLocation());

            tgSender.execute(
                    SendMessage.builder()
                            .text("Пожалуйста, укажите количество \uD83E\uDD92 которое вы хотели бы купить")
                            .chatId(tgUser.getChatId())
                            .replyMarkup(createCancelButton())
                            .build()
            );

            return getLocation();
        }

        String text = update.getMessage().getText();
        if ("Отмена".equals(text)) {
            return UserLocation.BASE;
        } else if (NUMBER_PATTERN.matcher(text).find() && Integer.parseInt(text) > 0) {
            sendPurchaseLink(tgUser, Integer.parseInt(text));
            return UserLocation.BASE;
        } else {
            sendInvalidInput(tgUser);
        }

        return getLocation();
    }

    private void sendPurchaseLink(TgUser tgUser, Integer number) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text(purchaseService.createPurchaseMessage(tgUser, number))
                        .chatId(tgUser.getChatId())
                        .replyMarkup(createCancelButton())
                        .build()
        );
    }

    private void sendInvalidInput(TgUser tgUser) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("Неверный формат.\n\nПожалуйста, укажите количество \uD83E\uDD92 которое вы хотели бы купить")
                        .chatId(tgUser.getChatId())
                        .replyMarkup(createCancelButton())
                        .build()
        );
    }
}
