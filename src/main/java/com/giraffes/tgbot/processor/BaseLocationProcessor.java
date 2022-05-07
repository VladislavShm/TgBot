package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
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

import static com.giraffes.tgbot.model.internal.telegram.ButtonName.BaseLocationButton;
import static com.giraffes.tgbot.model.internal.telegram.ButtonName.OkButton;

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
        Optional<OkButton> commonButton = messageToButtonTransformer.determineButton(text, OkButton.class);
        if (redirected || commonButton.isPresent()) {
            sendBaseMessage(availableNftQuantity, user);
            return getLocation();
        }

        return messageToButtonTransformer.determineButton(text, BaseLocationButton.class)
                .map(button -> processButtonClickEvent(user, button, availableNftQuantity))
                .orElseGet(() -> {
                    checkInvitation(text, user);
                    sendBaseMessage(availableNftQuantity, user);
                    return getLocation();
                });
    }

    private Location processButtonClickEvent(TgUser user, BaseLocationButton button, Integer availableNftQuantity) {
        switch (button) {
            case BUY_BUTTON:
                return Location.PURCHASE;
            case INVITE_INFO_BUTTON:
                sendInviteInfo(user);
                return getLocation();
            case MY_GIRAFFES_BUTTON:
                sendMyGiraffesInfo(user);
                return getLocation();
            case AUCTION_BUTTON:
                return Location.AUCTIONS_BROWSE;
            case ABOUT_US_BUTTON:
                sendGiraffeInfo(availableNftQuantity, user);
                return getLocation();
            case SETTINGS_BUTTON:
                return Location.SETTINGS;
            default:
                return getLocation();
        }
    }

    private int getAvailableNftQuantity() {
        return purchaseProperties.getPresaleQuantity() - (purchaseService.purchasesCount() + giftService.getGiftedNFTQuantity());
    }

    private void sendBaseMessage(Integer availableNftQuantity, TgUser user) {
        telegramSenderService.send(
                new Text("Giraffes Capital \uD83E\uDD92\uD83E\uDD92\uD83E\uDD92\n\nНаш канал (https://t.me/giraffe_capital)\n\n" +
                        "Текущая стадия коллекции: <b>PRESALE</b>\nДоступно " + availableNftQuantity + " \uD83E\uDD92 к приобритению."),
                createBaseButtons(),
                user
        );
    }

    private void sendGiraffeInfo(Integer availableNftQuantity, TgUser user) {
        String message = "Мы - первый инвестиционный DAO на блокчейне TON - " +
                "<a href=\"https://telegra.ph/Giraffe-Capital---investicionnyj-DAO-na-blokchejne-TON-03-21\">GIRAFFE CAPITAL\uD83E\uDD92</a>\n" +
                "В данный момент идёт этап <b>PRESALE</b>.\nОсталось nft - " +
                availableNftQuantity +
                "\uD83E\uDD92\nУсловия конкурса <a href=\"https://t.me/giraffe_capital/21\">ЗДЕСЬ</a>";

        telegramSenderService.send(new Text(message), createBaseButtons(), user);
    }

    private void sendMyGiraffesInfo(TgUser user) {
        Integer giftsCount = ObjectUtils.defaultIfNull(giftService.giftsCount(user), 0);
        Integer purchasesCount = purchaseService.purchasesCount(user);
        String message = String.format(
                "На данный момент Вы приобрели <i><b>%d</b></i> %s жирафов.\n\n" +
                        "В случае, если количество жирафов отличается от ожидаемого, пожалуйста, свяжитесь с нами - @GhostOfGiraffe\n" +
                        "Как правило, проведение транзакции и получение данных о Вашем переводе средств занимают некоторое время.",
                ObjectUtils.defaultIfNull(purchasesCount, 0), giftsCount > 0 ? " и выиграли <i><b>" + giftsCount + "</b></i>" : ""
        );

        telegramSenderService.send(
                new Text(message),
                createBaseButtons(),
                user
        );
    }

    private void sendInviteInfo(TgUser user) {
        telegramSenderService.send(
                new Text("На данный момент Вы пригласили <i><b>" + tgUserService.invitedCount(user) + "</b></i> человек\n" +
                        "ТОП-10 участников <a href=\"https://t.me/giraffe_capital/21\">нашего конкурса: </a>\n" +
                        tgUserService.topParticipants()),
                createBaseButtons(),
                user
        );

        telegramSenderService.send(
                new Text("Ваша персональная ссылка: \n\n" + tgUserService.createInvitationLink(user)),
                createBaseButtons(),
                user
        );
    }

    private void checkInvitation(String text, TgUser user) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        if (!text.startsWith("/start") || text.length() <= "/start".length()) {
            return;
        }

        if (user.getInvitedBy() != null) {
            log.warn("This user has already been invited: {} {}", text, user);
            return;
        }

        Long inviterId = extractInviterId(text);
        if (inviterId == null) {
            log.warn("Unexpected /start parameter: {} {}", text, user);
            return;
        }

        if (!user.isJustCreated()) {
            log.warn("User hasn't been just created: {}", user);
            return;
        }

        if (inviterId.equals(user.getId())) {
            log.warn("Attempt to invite yourself");
            return;
        }

        Optional<TgUser> inviter = tgUserService.findById(inviterId);
        if (inviter.isEmpty()) {
            log.info("Invalid inviter ID: {}", inviterId);
            return;
        }

        user.setInvitedBy(inviter.get());
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

    private Keyboard createBaseButtons() {
        return new Keyboard()
                .line(
                        BaseLocationButton.BUY_BUTTON,
                        BaseLocationButton.INVITE_INFO_BUTTON,
                        BaseLocationButton.MY_GIRAFFES_BUTTON
                )
                .line(
                        BaseLocationButton.AUCTION_BUTTON,
                        BaseLocationButton.ABOUT_US_BUTTON,
                        BaseLocationButton.SETTINGS_BUTTON
                );
    }
}
