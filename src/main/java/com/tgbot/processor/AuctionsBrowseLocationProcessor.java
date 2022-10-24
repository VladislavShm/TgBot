package com.tgbot.processor;

import com.tgbot.entity.Auction;
import com.tgbot.entity.Location;
import com.tgbot.entity.LocationAttribute;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.service.AuctionService;
import com.tgbot.service.UserAuctionActivityService;
import com.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        if (redirected || messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class).isPresent()) {
            sendShortAuctionsInfo(user);
            return Optional.empty();
        }

        if (messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class).isPresent()) {
            return Optional.of(Location.BASE);
        }

        if (AUCTION_ORDER_NUMBER_PATTERN.matcher(text).find()) {
            Auction auction = auctionService.findActiveByOrderNumber(Integer.valueOf(text));
            if (auction == null) {
                telegramSenderService.send(
                        new Text("Аукцион с данным номером не был найден. " +
                                "Возможно данный аукцион только что закончился. " +
                                "Пожалуйста, попробуйте ввод еще раз."),
                        new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                        user
                );

                sendShortAuctionsInfo(user);
            } else {
                sendAuctionInfo(auction, user);
                user.getLocationAttributes().put(LocationAttribute.AUCTION_ORDER_NUMBER, text);
                if (userAuctionActivityService.isParticipant(auction, user)) {
                    return Optional.of(Location.AUCTION_PARTICIPATION);
                } else {
                    return Optional.of(Location.AUCTION_REGISTRATION);
                }
            }

            return Optional.empty();
        }

        return Optional.empty();
    }

    private void sendShortAuctionsInfo(TgUser user) {
        List<Auction> activeAuctions = auctionService.findActive();
        List<Auction> upcomingAuctions = auctionService.findUpcoming();
        if (activeAuctions.isEmpty() && upcomingAuctions.isEmpty()) {
            sendNoActiveAndUpcomingAuctions(user);
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
                new Text(message.toString()),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }

    private void sendNoActiveAndUpcomingAuctions(TgUser user) {
        telegramSenderService.send(
                new Text("В данный момент нет ни одного активного или запланированного аукциона. Ждем Вас в будущем \uD83E\uDD92\uD83D\uDD54"),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }

    private void sendAuctionInfo(Auction auction, TgUser user) {
        telegramSenderService.send(
                new Text(String.format(
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
                )),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );

        telegramSenderService.sendImage(
                auction.getNftImage(),
                auction.getNftImageName(),
                new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                user
        );
    }
}
