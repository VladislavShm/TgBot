package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserLocation;
import com.giraffes.tgbot.property.PurchaseProperties;
import com.giraffes.tgbot.service.GiftService;
import com.giraffes.tgbot.service.PurchaseService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createBaseButtons;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaseLocationProcessor implements LocationProcessor {
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d+$");

    private final PurchaseService purchaseService;
    private final TgUserService tgUserService;
    private final GiftService giftService;
    private final PurchaseProperties purchaseProperties;

    @Autowired
    private AbsSender tgSender;

    @Override
    public UserLocation getLocation() {
        return UserLocation.BASE;
    }

    @Override
    @SneakyThrows
    public UserLocation process(Update update, boolean redirected) {
        TgUser tgUser = tgUserService.getCurrentUser();
        Integer soldPresaleNFT = purchaseService.getSoldPresaleNFTQuantity() + giftService.getGiftedNFTQuantity();

        if (redirected) {
            sendBaseMessage(tgUser.getChatId(), soldPresaleNFT);
            return getLocation();
        }

        String text = update.getMessage().getText();
        String chatId = tgUser.getChatId();
        if ("Купить \uD83E\uDD92".equals(text)) {
            return UserLocation.PURCHASE;
        } else if ("Инвайт инфо \uD83D\uDC65".equals(text)) {
            sendInviteInfo(chatId, tgUser);
        } else if ("Мои жирафы".equals(text)) {
            sendMyGiraffesInfo(chatId, tgUser);
        } else if ("О нас \uD83D\uDCD6".equals(text)) {
            getGiraffeInfo(chatId, soldPresaleNFT);
        } else {
            checkInvitation(text, tgUser);
            sendBaseMessage(chatId, soldPresaleNFT);
        }

        return getLocation();
    }

    private void sendBaseMessage(String chatId, Integer soldPresaleNFT) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("Giraffes Capital \uD83E\uDD92\uD83E\uDD92\uD83E\uDD92\n\nНаш канал (https://t.me/giraffe_capital)\n\n" +
                                "Текущая стадия коллекции: <b>PRESALE</b>\nДоступно " + getAvailableNftQuantity(soldPresaleNFT) + " \uD83E\uDD92 к приобритению. ")
                        .parseMode("html")
                        .chatId(chatId)
                        .replyMarkup(createBaseButtons())
                        .build()
        );
    }

    private void getGiraffeInfo(String chatId, Integer availablePresaleNFT) throws TelegramApiException {
        String message = "Мы - первый инвестиционный DAO на блокчейне TON - <a href=\"https://telegra.ph/Giraffe-Capital---investicionnyj-DAO-na-blokchejne-TON-03-21\">GIRAFFE CAPITAL\uD83E\uDD92</a>\n" +
                "В данный момент идёт этап <b>PRESALE</b>.\nОсталось nft - " + getAvailableNftQuantity(availablePresaleNFT) + "\uD83E\uDD92\nУсловия конкурса <a href=\"https://t.me/giraffe_capital/21\">ЗДЕСЬ</a>";
        tgSender.execute(
                SendMessage.builder()
                        .text(message)
                        .parseMode("html")
                        .chatId(chatId)
                        .replyMarkup(createBaseButtons())
                        .build());
    }

    private Integer getAvailableNftQuantity(Integer soldPresaleNFT) {
        return (purchaseProperties.getPresaleQuantity() - soldPresaleNFT);
    }

    private void sendMyGiraffesInfo(String chatId, TgUser tgUser) throws TelegramApiException {
        Integer giftsCount = ObjectUtils.defaultIfNull(giftService.giftsCount(tgUser), 0);
        Integer purchasesCount = purchaseService.purchasesCount(tgUser);
        String message = String.format(
                "На данный момент Вы приобрели <i><b>%d</b></i> %s жирафов.\n\n" +
                        "В случае, если количество жирафов отличается от ожидаемого, пожалуйста, свяжитесь с нами - @GhostOfGiraffe\n" +
                        "Как правило, проведение транзакции и получение данных о Вашем переводе средств занимают некоторое время.",
                ObjectUtils.defaultIfNull(purchasesCount, 0), giftsCount > 0 ? " и выиграли <i><b>" + giftsCount + "</b></i>" : ""
        );

        tgSender.execute(
                SendMessage.builder()
                        .text(message)
                        .parseMode("html")
                        .chatId(chatId)
                        .replyMarkup(createBaseButtons())
                        .build()
        );
    }

    private void sendInviteInfo(String chatId, TgUser tgUser) throws TelegramApiException {
        tgSender.execute(
                SendMessage.builder()
                        .text("На данный момент Вы пригласили <i><b>" + tgUserService.invitedCount(tgUser) + "</b></i> человек")
                        .parseMode("html")
                        .chatId(chatId)
                        .replyMarkup(createBaseButtons())
                        .build()
        );

        String message = "Ваша персональная ссылка: \n\n" + tgUserService.createInvitationLink(tgUser);
        tgSender.execute(
                SendMessage.builder()
                        .text(message)
                        .chatId(chatId)
                        .replyMarkup(createBaseButtons())
                        .build()
        );
    }

    private void checkInvitation(String text, TgUser tgUser) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        if (!text.startsWith("/start") || text.length() <= "/start".length()) {
            return;
        }

        if (tgUser.getInvitedBy() != null) {
            log.warn("This user has already been invited: {} {}", text, tgUser);
            return;
        }

        Long inviterId = extractInviterId(text);
        if (inviterId == null) {
            log.warn("Unexpected /start parameter: {} {}", text, tgUser);
            return;
        }

        if (!tgUserService.isUserJustCreated()) {
            log.warn("User hasn't been just created: {}", tgUser);
            return;
        }

        if (inviterId.equals(tgUser.getId())) {
            log.warn("Attempt to invite yourself");
            return;
        }

        Optional<TgUser> inviter = tgUserService.findById(inviterId);
        if (!inviter.isPresent()) {
            log.info("Invalid inviter ID: {}", inviterId);
            return;
        }

        tgUser.setInvitedBy(inviter.get());
    }

    private Long extractInviterId(String text) {
        String rawInviterId = text.substring("/start".length()).trim();
        if (ID_PATTERN.matcher(rawInviterId).find()) {
            return Long.parseLong(rawInviterId);
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(rawInviterId);
            String decodedString = new String(decodedBytes);

            if (decodedString.startsWith("base64")) {
                rawInviterId = decodedString.substring("base64".length());
            }

            if (ID_PATTERN.matcher(rawInviterId).find()) {
                return Long.parseLong(rawInviterId);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

}
