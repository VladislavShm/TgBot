package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createBaseButtons;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCommunicationService {
    private final TelegramSenderService telegramSenderService;

    @SneakyThrows
    public void sendGiftNotification(TgUser tgUser, String wallet) {
        String message = "Благодарим за участие в розыгрыше от GIRAFFE CAPITAL\uD83E\uDD92\n";

        if (StringUtils.isNotBlank(wallet)) {
            message += "Ваша NFT будет отправлена на кошелек " + wallet + " сразу после окончания этапа presale.";
        } else {
            message += "Пожалуйста, сообщите нам (@GhostOfGiraffe) кошелек, на который Вы хотели бы получить Вашу NFT.\n" +
                    "Мы отправим Вашу NFT сразу после окончания этапа presale.";
        }

        tgUser.setLocation(UserLocation.BASE);
        telegramSenderService.send(message, createBaseButtons(), tgUser.getChatId());
    }
}
