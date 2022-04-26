package com.giraffes.tgbot.processor.auction;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.entity.UserAuctionActivity;
import com.giraffes.tgbot.service.AuctionSchedulerService;
import com.giraffes.tgbot.service.AuctionService;
import com.giraffes.tgbot.service.UserAuctionActivityService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.giraffes.tgbot.entity.LocationAttribute.AUCTION_ORDER_NUMBER;
import static com.giraffes.tgbot.entity.LocationAttribute.SUGGESTED_AUCTION_BID;
import static com.giraffes.tgbot.utils.TelegramUiUtils.createBackButtonRow;
import static com.giraffes.tgbot.utils.TelegramUiUtils.createYesNoKeyboard;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionParticipationLocationProcessor extends AuctionLocationProcessor {
    private static final String REDIRECT_TO_WALLET_SETTINGS_BTN = "Перейти в раздел для указания кошелька ⚙️";
    private static final String REDIRECT_TO_WALLET_CONFIRM_BTN = "Перейти в раздел для подтверждения кошелька ⚙️";
    private static final String UPDATE_BTN = "Обновить \uD83D\uDD04";

    private static final Pattern BID_PATTERN = Pattern.compile("^\\d+\\.?\\d*$");

    private final UserAuctionActivityService userAuctionActivityService;
    private final AuctionSchedulerService auctionSchedulerService;
    private final AuctionService auctionService;

    @Override
    public Location getLocation() {
        return Location.AUCTION_PARTICIPATION;
    }

    @Override
    protected Location processTextForAuction(TgUser user, String text, boolean redirected, Auction auction) {
        if (REDIRECT_TO_WALLET_SETTINGS_BTN.equals(text) && StringUtils.isBlank(user.getWallet())) {
            return Location.WALLET_SETTINGS;
        }

        if (StringUtils.isBlank(user.getWallet())) {
            sendAskConfigureWallet();
            return getLocation();
        }

        if (REDIRECT_TO_WALLET_CONFIRM_BTN.equals(text) && !user.isWalletConfirmed()) {
            return Location.WALLET_CONFIRMATION;
        }

        if (!user.isWalletConfirmed()) {
            sendAskConfirmWallet();
            return getLocation();
        }

        if (redirected || "Ок".equals(text)) {
            sendCurrentAuctionState(auction, user);
            return getLocation();
        }

        if ("Назад".equals(text)) {
            clearUserLocationAttributes(user);
            return Location.AUCTIONS_BROWSE;
        }

        if ("Да".equals(text)) {
            processBidApproved(user, auction.getId());
            sendCurrentAuctionState(auction, user);
            return getLocation();
        }

        if ("Нет".equals(text)) {
            sendCurrentAuctionState(auction, user);
            user.getLocationAttributes().remove(SUGGESTED_AUCTION_BID);
            return getLocation();
        }

        if ("Прекратить участие".equals(text)) {
            return Location.AUCTION_UNSUBSCRIBE;
        }

        if (UPDATE_BTN.equals(text)) {
            sendCurrentAuctionState(auction, user);
            return getLocation();
        }

        if (BID_PATTERN.matcher(text).find()) {
            processBidSuggest(user, text, auction);
        } else {
            sendInvalidInput();
        }

        return getLocation();
    }

    private void sendAskConfigureWallet() {
        telegramSenderService.send(
                "Пожалуйста, укажите Ваш кошелек в настройках для участия в аукционе",
                ReplyKeyboardMarkup.builder()
                        .keyboard(
                                Arrays.asList(
                                        new KeyboardRow(
                                                Collections.singletonList(
                                                        new KeyboardButton(REDIRECT_TO_WALLET_SETTINGS_BTN)
                                                )
                                        ),
                                        createBackButtonRow())
                        )
                        .resizeKeyboard(true)
                        .build()
        );
    }

    private void sendAskConfirmWallet() {
        telegramSenderService.send(
                "Пожалуйста, подтвердите Ваш кошелек в настройках для участия в аукционе",
                ReplyKeyboardMarkup.builder()
                        .keyboard(
                                Arrays.asList(
                                        new KeyboardRow(
                                                Collections.singletonList(
                                                        new KeyboardButton(REDIRECT_TO_WALLET_CONFIRM_BTN)
                                                )
                                        ),
                                        createBackButtonRow())
                        )
                        .resizeKeyboard(true)
                        .build()
        );
    }

    private void sendInvalidInput() {
        telegramSenderService.send(
                "Неверный формат.\nОтправьте количество TON, которые Вы хотите поставить.",
                createBaseButtons()
        );
    }

    private void processBidSuggest(TgUser user, String text, Auction auction) {
        BigInteger auctionBid = TonCoinUtils.fromHumanReadable(text);
        if (auctionBid.compareTo(BigInteger.ZERO) <= 0) {
            telegramSenderService.send(
                    "Пожалуйста, укажите значение больше 0",
                    createBaseButtons()
            );

            return;
        }

        BigInteger minimumAllowBid = auctionService.calculateMinimumAllowedBid(auction);
        if (auctionBid.compareTo(minimumAllowBid) < 0) {
            sendInvalidTonAmount(minimumAllowBid);
            return;
        }

        if (!auctionService.isUserHasEnoughCoins(user, auctionBid)) {
            sendNotEnoughCoins();
            return;
        }

        user.getLocationAttributes().put(SUGGESTED_AUCTION_BID, text);
        telegramSenderService.send(
                String.format("Вы действительно хотите сделать ставку в размере %s TON?", text),
                createYesNoKeyboard()
        );
    }

    private void sendNotEnoughCoins() {
        telegramSenderService.send(
                "К сожалению, на Вашем кошельке недостаточно средств для совершения данной ставки.",
                createBaseButtons()
        );
    }

    private void processBidApproved(TgUser user, Long auctionId) {
        Auction auction = auctionService.findByIdAndLock(auctionId);
        Optional<UserAuctionActivity> currentHighestBid = userAuctionActivityService.findHighestBid(auction);
        BigInteger minimumAllowBid = auctionService.calculateMinimumAllowedBid(auction, currentHighestBid);
        BigInteger newAuctionBid = TonCoinUtils.fromHumanReadable(user.getLocationAttributes().get(SUGGESTED_AUCTION_BID));
        if (newAuctionBid.compareTo(minimumAllowBid) < 0) {
            sendInvalidTonAmount(minimumAllowBid);
            return;
        }

        if (!auctionService.isUserHasEnoughCoins(user, newAuctionBid)) {
            sendNotEnoughCoins();
            return;
        }

        UserAuctionActivity activeUserAuctionActivity = userAuctionActivityService.findActivity(auction, user);
        activeUserAuctionActivity.setBid(newAuctionBid);
        activeUserAuctionActivity.setBidDateTime(LocalDateTime.now());

        user.getLocationAttributes().remove(SUGGESTED_AUCTION_BID);

        auctionSchedulerService.scheduleAuctionFinish(auction);

        currentHighestBid
                .filter(bid -> !Objects.equals(user, currentHighestBid.get().getUser()))
                .ifPresent((bid) -> userAuctionActivityService.notifyOutbid(bid, newAuctionBid));

        telegramSenderService.send("Ваша ставка принята!", createBaseButtons(), user);
    }

    private void sendInvalidTonAmount(BigInteger minimumAllowBid) {
        telegramSenderService.send(
                String.format(
                        "Минимальная допустимая ставка %s TON. Пожалуйста, укажите количество TON большее или равное указанной сумме.",
                        TonCoinUtils.toHumanReadable(minimumAllowBid)
                ),
                createBaseButtons()
        );
    }

    private void sendCurrentAuctionState(Auction auction, TgUser user) {
        telegramSenderService.send(createCurrentAuctionStateMessage(auction, user), createBaseButtons(), user);
    }

    private String createCurrentAuctionStateMessage(Auction auction, TgUser user) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getStartDateTime())) {
            Optional<UserAuctionActivity> highestBidOptional = userAuctionActivityService.findHighestBid(auction);
            return highestBidOptional
                    .map((highestBid) -> createAuctionStateMessageHavingBid(user, highestBid))
                    .orElseGet(() -> createAuctionStateMessageWithoutBid(auction));
        } else {
            return String.format(
                    "Вы зарегистрированы в качестве участника в данном аукционе.\n" +
                            "Пожалуйста, ожидайте начала данного аукциона, чтобы принять в нем участие.\n%s",
                    AuctionService.createStartInMessage(auction)
            );
        }
    }

    private String createAuctionStateMessageWithoutBid(Auction auction) {
        BigInteger minimumAllowBid = AuctionService.calculateCurrentReducedPrice(auction);
        return String.format(
                "Текущая минимальная допустимая ставка: %s TON.\n" +
                        "Следующее уменьшение стоимости произойдет через: %s минут\n" +
                        "Отправьте количество TON, которые Вы хотите поставить.",
                TonCoinUtils.toHumanReadable(minimumAllowBid),
                AuctionService.calculateMinutesToNextReduction(auction)
        );
    }

    private String createAuctionStateMessageHavingBid(TgUser user, UserAuctionActivity highestBid) {
        BigInteger nextMinimumAllowedBid = AuctionService.calculateNextMinimumAllowedBid(highestBid);
        long minutesLeft = calculateMinutesLeft(highestBid);
        if (Objects.equals(user, highestBid.getUser())) {
            return String.format(
                    "На данный момент Вы лидерируете в данном аукционе!\n" +
                            "Ваша максимальная сделанная ставка: %s TON\n" +
                            "Следующая минимальная допустимая ставка: %s TON\n" +
                            "Если в течение <i><b>%s</b></i> минут Ваша ставка не будет перебита, то NFT Ваша!\n" +
                            "Если Вы хотите повысить свою ставку, то отправьте количество TON, которое хотели бы поставить.",
                    TonCoinUtils.toHumanReadable(highestBid.getBid()),
                    TonCoinUtils.toHumanReadable(nextMinimumAllowedBid),
                    minutesLeft
            );
        } else {
            return String.format(
                    "Текущая максимальная сделанная ставка: %s TON\n" +
                            "Минимальная допустимая ставка: %s TON\n" +
                            "У Вас осталось <i><b>%s</b></i> минут чтобы перебить ставку лидера.\n" +
                            "Отправьте количество TON, которые Вы хотите поставить.",
                    TonCoinUtils.toHumanReadable(highestBid.getBid()),
                    TonCoinUtils.toHumanReadable(nextMinimumAllowedBid),
                    minutesLeft
            );
        }
    }

    private long calculateMinutesLeft(UserAuctionActivity highestBid) {
        return Math.abs(highestBid.getAuction().getMinutesToOutbid() - Math.abs(ChronoUnit.MINUTES.between(LocalDateTime.now(), highestBid.getBidDateTime())));
    }

    private ReplyKeyboardMarkup createBaseButtons() {
        return ReplyKeyboardMarkup.builder()
                .keyboard(Arrays.asList(
                        new KeyboardRow(
                                Collections.singletonList(
                                        new KeyboardButton(UPDATE_BTN)
                                )
                        ),
                        createBackButtonRow())
                )
                .resizeKeyboard(true)
                .build();
//        TODO I don't think that we really need this feature now
//        return ReplyKeyboardMarkup.builder()
//                .keyboard(Arrays.asList(
//                        new KeyboardRow(
//                                Collections.singletonList(
//                                        new KeyboardButton("Прекратить участие")
//                                )
//                        ),
//                        createBackButtonRow()))
//                .resizeKeyboard(true)
//                .build();
    }

    @Override
    protected void clearUserLocationAttributes(TgUser user) {
        user.getLocationAttributes().remove(AUCTION_ORDER_NUMBER);
        user.getLocationAttributes().remove(SUGGESTED_AUCTION_BID);
    }
}
