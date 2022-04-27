package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.LocationAttribute;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.service.AuctionService;
import com.giraffes.tgbot.service.UserAuctionActivityService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createBackButtonKeyboard;

@Component
@RequiredArgsConstructor
public class AuctionsBrowseLocationProcessor extends LocationProcessor {
    private static final Pattern AUCTION_ORDER_NUMBER_PATTERN = Pattern.compile("^\\d+$");

    private final UserAuctionActivityService userAuctionActivityService;
    private final AuctionService auctionService;

    @Override
    public Location getLocation() {
        return Location.AUCTIONS_BROWSE;
    }

    @Override
    protected Location processText(TgUser user, String text, boolean redirected) {
        if (redirected || "Ок".equals(text)) {
            sendShortAuctionsInfo();
        } else if (AUCTION_ORDER_NUMBER_PATTERN.matcher(text).find()) {
            Auction auction = auctionService.findActiveByOrderNumber(Integer.valueOf(text));
            if (auction == null) {
                telegramSenderService.send(
                        "Аукцион с данным номером не был найден. " +
                                "Возможно данный аукцион только что закончился. " +
                                "Пожалуйста, попробуйте ввод еще раз.",
                        createBackButtonKeyboard()
                );

                sendShortAuctionsInfo();
            } else {
                sendAuctionInfo(auction);
                user.getLocationAttributes().put(LocationAttribute.AUCTION_ORDER_NUMBER, text);
                if (userAuctionActivityService.isParticipant(auction, user)) {
                    return Location.AUCTION_PARTICIPATION;
                } else {
                    return Location.AUCTION_REGISTRATION;
                }
            }
        } else if ("Назад".equals(text)) {
            return Location.BASE;
        }

        return getLocation();
    }

    private void sendShortAuctionsInfo() {
        List<Auction> activeAuctions = auctionService.findActive();
        List<Auction> upcomingAuctions = auctionService.findUpcoming();
        if (activeAuctions.isEmpty() && upcomingAuctions.isEmpty()) {
            sendNoActiveAndUpcomingAuctions();
            return;
        }

        StringBuilder message = new StringBuilder("Пожалуйста, укажите номер аукциона, о котором Вы хотели бы получить информацию и, возможно, принять участие");
        if (!activeAuctions.isEmpty()) {
            message.append("\n\nАктивные аукционы:\n");

            for (Auction activeAuction : activeAuctions.stream().sorted(Comparator.comparing(Auction::getOrderNumber)).collect(Collectors.toList())) {
                message.append(String.format("\n%s. %s", activeAuction.getOrderNumber(), activeAuction.getName()));
            }
        }

        if (!upcomingAuctions.isEmpty()) {
            message.append("\n\nПредстоящие аукционы:\n");

            for (Auction upcomingAuction : upcomingAuctions.stream().sorted(Comparator.comparing(Auction::getOrderNumber)).collect(Collectors.toList())) {
                message.append(String.format(
                        "\n%s. %s. %s",
                        upcomingAuction.getOrderNumber(),
                        upcomingAuction.getName(),
                        AuctionService.createStartInMessage(upcomingAuction)
                ));
            }
        }

        telegramSenderService.send(
                message.toString(),
                createBackButtonKeyboard()
        );
    }

    private void sendNoActiveAndUpcomingAuctions() {
        telegramSenderService.send(
                "В данный момент нет ни одного активного или запланированного аукциона. Ждем Вас в будущем \uD83E\uDD92\uD83D\uDD54",
                createBackButtonKeyboard()
        );
    }

    private void sendAuctionInfo(Auction auction) {
        telegramSenderService.send(
                String.format(
                        "Аукцион номер %d - %s\n\n" +
                                "Каждые %s минут начальная ставка %s TON будет уменьшаться на %s TON, пока не достигнет %s TON (аукцион заканчивается, лот снимается), либо пока кто-то не перебьёт эту ставку.\n" +
                                "После обновления максимальной ставки у участников будет %s минут, чтобы перебить её, иначе лот достанется лидеру аукциона.\n\n" +
                                "%s",
                        auction.getOrderNumber(),
                        auction.getName(),
                        auction.getPriceReductionMinutes(),
                        TonCoinUtils.toHumanReadable(auction.getStartPrice()),
                        TonCoinUtils.toHumanReadable(auction.getPriceReductionValue()),
                        TonCoinUtils.toHumanReadable(auction.getMinPrice()),
                        BigInteger.valueOf(auction.getMinutesToOutbid()),
                        auction.getDescription()
                ),
                createBackButtonKeyboard()
        );

        telegramSenderService.sendImage(
                auction.getNftImage(),
                auction.getNftImageName(),
                createBackButtonKeyboard()
        );
    }
}
