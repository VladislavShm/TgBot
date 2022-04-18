package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Gift;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.CreateGiftDto;
import com.giraffes.tgbot.model.UpdateGiftDto;
import com.giraffes.tgbot.repository.GiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createBaseButtons;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GiftService {
    private final TelegramSenderService telegramSenderService;
    private final GiftRepository giftRepository;
    private final TgUserService tgUserService;

    public void createGift(CreateGiftDto giftDto) {
        Gift gift = new Gift();
        gift.setAmount(giftDto.getAmount());
        gift.setUser(
                Optional.ofNullable(tgUserService.findByChatId(giftDto.getChatId()))
                        .orElseThrow(() -> new RuntimeException("User " + giftDto.getChatId() + " not found"))
        );
        gift.setReason(giftDto.getReason());
        gift.setWallet(giftDto.getWallet());
        giftRepository.save(gift);
        sendGiftNotification(gift.getUser(), gift.getWallet());
    }

    public Integer getGiftedNFTQuantity() {
        return ObjectUtils.defaultIfNull(giftRepository.getGiftedNFTQuantity(), 0);
    }

    public void updateGift(UpdateGiftDto giftDto) {
        Gift gift = giftRepository.getById(giftDto.getGiftId());
        gift.setAmount(giftDto.getAmount());
        gift.setUser(
                Optional.ofNullable(tgUserService.findByChatId(giftDto.getChatId()))
                        .orElseThrow(() -> new RuntimeException("User " + giftDto.getChatId() + " not found"))
        );
        gift.setReason(giftDto.getReason());
        gift.setWallet(giftDto.getWallet());
        giftRepository.save(gift);
    }

    public Integer giftsCount(TgUser tgUser) {
        return ObjectUtils.defaultIfNull(giftRepository.giftsCount(tgUser), 0);
    }

    private void sendGiftNotification(TgUser tgUser, String wallet) {
        String message = "Благодарим за участие в розыгрыше от GIRAFFE CAPITAL\uD83E\uDD92\n";

        if (StringUtils.isNotBlank(wallet)) {
            message += "Ваша NFT будет отправлена на кошелек " + wallet + " сразу после окончания этапа presale.";
        } else {
            message += "Пожалуйста, сообщите нам (@GhostOfGiraffe) кошелек, на который Вы хотели бы получить Вашу NFT.\n" +
                    "Мы отправим Вашу NFT сразу после окончания этапа presale.";
        }

        tgUser.setLocation(Location.BASE);
        telegramSenderService.send(message, createBaseButtons(), tgUser.getChatId());
    }
}
