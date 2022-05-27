package com.giraffes.tgbot.processor;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.Nft;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.NftImage;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.service.NftService;
import com.giraffes.tgbot.service.PCloudProvider;
import com.giraffes.tgbot.service.TgUserService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.giraffes.tgbot.model.internal.telegram.ButtonName.BaseLocationButton;
import static com.giraffes.tgbot.model.internal.telegram.ButtonName.OkButton;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaseLocationProcessor extends LocationProcessor {
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d+$");

    private final PCloudProvider pCloudProvider;
    private final TgUserService tgUserService;
    private final NftService nftService;

    @Override
    public Location getLocation() {
        return Location.BASE;
    }

    @Override
    @SneakyThrows
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        Optional<OkButton> commonButton = messageToButtonTransformer.determineButton(text, OkButton.class);
        if (redirected || commonButton.isPresent()) {
            sendBaseMessage(user);
            return Optional.empty();
        }

        return messageToButtonTransformer.determineButton(text, BaseLocationButton.class)
                .map(button -> processButtonClickEvent(user, button))
                .or(() -> {
                    checkInvitation(text, user);
                    sendBaseMessage(user);
                    return Optional.empty();
                });
    }

    private Location processButtonClickEvent(TgUser user, BaseLocationButton button) {
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
                sendGiraffeInfo(user);
                return getLocation();
            case SETTINGS_BUTTON:
                return Location.SETTINGS;
            default:
                return getLocation();
        }
    }

    private void sendBaseMessage(TgUser user) {
        telegramSenderService.send(
                new Text("Giraffes Capital \uD83E\uDD92\uD83E\uDD92\uD83E\uDD92\n\nНаш канал (https://t.me/giraffe_capital)"),
                createBaseButtons(),
                user
        );
    }

    private void sendGiraffeInfo(TgUser user) {
        String message = "Мы - первый инвестиционный DAO на блокчейне TON - " +
                "<a href=\"https://telegra.ph/Giraffe-Capital---investicionnyj-DAO-na-blokchejne-TON-03-21\">GIRAFFE CAPITAL\uD83E\uDD92</a>\n" +
                "\uD83E\uDD92\nУсловия конкурса <a href=\"https://t.me/giraffe_capital/21\">ЗДЕСЬ</a>";

        telegramSenderService.send(new Text(message), createBaseButtons(), user);
    }

    @SneakyThrows
    private void sendMyGiraffesInfo(TgUser user) {
        List<Nft> nfts = nftService.getUserNFTs(user);
        Map<Integer, List<Nft>> nftByIndex = nfts.stream().collect(groupingBy(Nft::getIndex));
        Map<Integer, NftImage> nftImageList = pCloudProvider.imageDataByIndexes(nftByIndex.keySet());

        String message = String.format(
                "На данный момент у Вас имеется <i><b>%d</b></i> жирафов.\n\n" +
                        "В случае, если количество жирафов отличается от ожидаемого, пожалуйста, свяжитесь с нами - @GhostOfGiraffe",
                nfts.size()
        );

        telegramSenderService.send(
                new Text(message),
                createBaseButtons(),
                user
        );

        long totalNftNumber = nftService.totalNftNumber();
        nftImageList.forEach((index, nftImage) -> {
            Nft nft = nftByIndex.get(index).stream().findFirst().orElseThrow();
            int nftRank = nftService.getNftRank(nft);
            BigDecimal rarity = nft.getRarity();
            Optional.ofNullable(nft.getLastValue())
                    .map(TonCoinUtils::toHumanReadable)
                    .map(lastPrice ->
                            new Text("my_giraffes.nft_caption")
                                    .param(index)
                                    .param(lastPrice)
                                    .param(rarity)
                                    .param(nftRank)
                                    .param(totalNftNumber)
                    )
                    .or(() -> Optional.of(
                            new Text("my_giraffes.nft_caption_without_price")
                                    .param(index)
                                    .param(rarity)
                                    .param(nftRank)
                                    .param(totalNftNumber)
                    ))
                    .ifPresent(text ->
                            telegramSenderService.sendImage(
                                    text,
                                    nftImage.getImage(),
                                    nftImage.getFilename(),
                                    createBaseButtons(),
                                    user
                            )
                    );

        });

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

        if (!user.isJustCreated()) {
            log.warn("User hasn't been just created: {}", user);
            return;
        }

        extractInviterId(text)
                .filter((inviterId) -> {
                    if (inviterId.equals(user.getId())) {
                        log.warn("Attempt to invite yourself");
                        return false;
                    }

                    return true;
                })
                .ifPresentOrElse(
                        (inviterId) ->
                                tgUserService.findById(inviterId)
                                        .ifPresentOrElse(
                                                user::setInvitedBy,
                                                () -> log.info("Invalid inviter ID: {}", inviterId)
                                        ),
                        () ->
                                log.warn("Unexpected /start parameter: {} {}", text, user)
                );
    }

    private Optional<Long> extractInviterId(String text) {
        String rawInviterId = text.substring("/start".length()).trim();
        if (ID_PATTERN.matcher(rawInviterId).find()) {
            return Optional.of(Long.parseLong(rawInviterId));
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(rawInviterId);
            String decodedString = new String(decodedBytes);

            if (decodedString.startsWith("base64")) {
                rawInviterId = decodedString.substring("base64".length());
            }

            if (ID_PATTERN.matcher(rawInviterId).find()) {
                return Optional.of(Long.parseLong(rawInviterId));
            }
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.empty();
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
