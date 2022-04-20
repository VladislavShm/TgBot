package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.property.PurchaseProperties;
import com.giraffes.tgbot.service.GiftService;
import com.giraffes.tgbot.service.PurchaseService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createBaseButtons;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaseLocationProcessor extends LocationProcessor {
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d+$");

    private final PurchaseProperties purchaseProperties;
    private final PurchaseService purchaseService;
    private final TgUserService tgUserService;
    private final GiftService giftService;

    @Override
    public Location getLocation() {
        return Location.BASE;
    }

    @Override
    @SneakyThrows
    protected Location processText(TgUser user, String text, boolean redirected) {
        Integer availableNftQuantity = getAvailableNftQuantity();

        if (redirected || "Ок".equals(text)) {
            sendBaseMessage(availableNftQuantity);
            return getLocation();
        }

        if ("Купить \uD83E\uDD92".equals(text)) {
            return Location.PURCHASE;
        } else if ("Инвайт инфо \uD83D\uDC65".equals(text)) {
            sendInviteInfo(user);
        } else if ("Мои жирафы \uD83E\uDD92".equals(text)) {
            sendMyGiraffesInfo(user);
        } else if ("Аукцион ⚖️".equals(text)) {
            return Location.AUCTIONS_BROWSE;
        } if ("О нас \uD83D\uDCD6".equals(text)) {
            sendGiraffeInfo(availableNftQuantity);
        } else if ("Настройки ⚙️".equals(text)) {
            return Location.SETTINGS;
        } else {
            checkInvitation(text, user);
            sendBaseMessage(availableNftQuantity);
        }

        return getLocation();
    }

    private int getAvailableNftQuantity() {
        return purchaseProperties.getPresaleQuantity() - (purchaseService.getSoldPresaleNFTQuantity() + giftService.getGiftedNFTQuantity());
    }

    private void sendBaseMessage(Integer availableNftQuantity) {
        telegramSenderService.send(
                "Giraffes Capital \uD83E\uDD92\uD83E\uDD92\uD83E\uDD92\n\nНаш канал (https://t.me/giraffe_capital)\n\n" +
                        "Текущая стадия коллекции: <b>PRESALE</b>\nДоступно " + availableNftQuantity + " \uD83E\uDD92 к приобритению. ",
                createBaseButtons()
        );
    }

    private void sendGiraffeInfo(Integer availableNftQuantity) {
        String message = "Мы - первый инвестиционный DAO на блокчейне TON - " +
                "<a href=\"https://telegra.ph/Giraffe-Capital---investicionnyj-DAO-na-blokchejne-TON-03-21\">GIRAFFE CAPITAL\uD83E\uDD92</a>\n" +
                "В данный момент идёт этап <b>PRESALE</b>.\nОсталось nft - " +
                availableNftQuantity +
                "\uD83E\uDD92\nУсловия конкурса <a href=\"https://t.me/giraffe_capital/21\">ЗДЕСЬ</a>";

        telegramSenderService.send(message, createBaseButtons());
    }

    private void sendMyGiraffesInfo(TgUser user) {
        Integer giftsCount = ObjectUtils.defaultIfNull(giftService.giftsCount(user), 0);
        Integer purchasesCount = purchaseService.approvedPurchasesCount(user);
        String message = String.format(
                "На данный момент Вы приобрели <i><b>%d</b></i> %s жирафов.\n\n" +
                        "В случае, если количество жирафов отличается от ожидаемого, пожалуйста, свяжитесь с нами - @GhostOfGiraffe\n" +
                        "Как правило, проведение транзакции и получение данных о Вашем переводе средств занимают некоторое время.",
                ObjectUtils.defaultIfNull(purchasesCount, 0), giftsCount > 0 ? " и выиграли <i><b>" + giftsCount + "</b></i>" : ""
        );

        telegramSenderService.send(
                message,
                createBaseButtons()
        );
    }

    private void sendInviteInfo(TgUser tgUser) {
        telegramSenderService.send(
                "На данный момент Вы пригласили <i><b>" + tgUserService.invitedCount(tgUser) + "</b></i> человек",
                createBaseButtons()
        );

        telegramSenderService.send(
                "Ваша персональная ссылка: \n\n" + tgUserService.createInvitationLink(tgUser),
                createBaseButtons()
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

        if (!TgUserService.isUserJustCreated()) {
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
