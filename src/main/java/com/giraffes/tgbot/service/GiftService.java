package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Gift;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.api.CreateGiftDto;
import com.giraffes.tgbot.model.api.UpdateGiftDto;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.repository.GiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.giraffes.tgbot.model.internal.telegram.ButtonName.OkButton;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GiftService {
    private final TelegramSenderService telegramSenderService;
    private final GiftRepository giftRepository;
    private final TgUserService tgUserService;

    public void createGift(CreateGiftDto giftDto) {
        tgUserService.findByChatId(giftDto.getChatId())
                .ifPresentOrElse(
                        (user) -> {
                            Gift gift = new Gift();
                            gift.setAmount(giftDto.getAmount());
                            gift.setUser(user);
                            gift.setReason(giftDto.getReason());
                            gift.setWallet(giftDto.getWallet());
                            giftRepository.save(gift);
                            sendGiftNotification(gift.getUser(), gift.getWallet());
                        },
                        () -> {
                            throw new RuntimeException("User " + giftDto.getChatId() + " not found");
                        });
    }

    public Integer getGiftedNFTQuantity() {
        return ObjectUtils.defaultIfNull(giftRepository.getGiftedNFTQuantity(), 0);
    }

    public void updateGift(UpdateGiftDto giftDto) {
        tgUserService.findByChatId(giftDto.getChatId())
                .ifPresentOrElse(
                        (user) -> {
                            Gift gift = giftRepository.getById(giftDto.getGiftId());
                            gift.setAmount(giftDto.getAmount());
                            gift.setUser(user);
                            gift.setReason(giftDto.getReason());
                            gift.setWallet(giftDto.getWallet());
                            giftRepository.save(gift);
                        },
                        () -> {
                            throw new RuntimeException("User " + giftDto.getChatId() + " not found");
                        });
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

        telegramSenderService.send(new Text(message), new Keyboard(OkButton.OK_BUTTON), tgUser);
    }
}
