package com.tgbot.processor;

import com.tgbot.entity.Location;
import com.tgbot.entity.Nft;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.property.PurchaseProperties;
import com.tgbot.service.NftImageAsyncSenderService;
import com.tgbot.service.NftService;
import com.tgbot.service.PurchaseService;
import com.tgbot.service.RoyaltyService;
import com.tgbot.service.TgUserService;
import com.tgbot.utils.TonCoinUtils;
import com.tgbot.model.internal.telegram.ButtonName;
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

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaseLocationProcessor extends LocationProcessor {
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d+$");

    private final NftImageAsyncSenderService nftImageAsyncSenderService;
    private final PurchaseProperties purchaseProperties;
    private final PurchaseService purchaseService;
    private final RoyaltyService royaltyService;
    private final TgUserService tgUserService;
    private final NftService nftService;

    @Override
    public Location getLocation() {
        return Location.BASE;
    }

    @Override
    @SneakyThrows
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        Optional<ButtonName.OkButton> commonButton = messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class);
        if (redirected || commonButton.isPresent()) {
            sendBaseMessage(user);
            return Optional.empty();
        }

        return messageToButtonTransformer.determineButton(text, ButtonName.BaseLocationButton.class)
                .map(button -> processButtonClickEvent(user, button))
                .or(() -> {
                    checkInvitation(text, user);
                    sendBaseMessage(user);
                    return Optional.empty();
                });
    }

    private Location processButtonClickEvent(TgUser user, ButtonName.BaseLocationButton button) {
        switch (button) {
            case BUY_BUTTON:
                return Location.PURCHASE;
            case INVITE_INFO_BUTTON:
                sendInviteInfo(user);
                return getLocation();
            case MY_NFT_BUTTON:
                sendMyNftInfo(user);
                return getLocation();
            case ROYALTY_BUTTON:
                sendRoyaltyInfo(user);
                return getLocation();
            case AUCTION_BUTTON:
                return Location.AUCTIONS_BROWSE;
            case ABOUT_COLLECTION_BUTTON:
                return Location.ABOUT_COLLECTION;
            case SETTINGS_BUTTON:
                return Location.SETTINGS;
            default:
                return getLocation();
        }
    }

    private void sendBaseMessage(TgUser user) {
        if (purchaseProperties.isEnabled()) {
            Integer purchaseNftLeft = purchaseService.purchaseNftLeft();
            telegramSenderService.send(
                    new Text("base_location.base_message_presale", purchaseNftLeft),
                    createBaseButtons(),
                    user
            );
        } else {
            telegramSenderService.send(
                    new Text("base_location.base_message"),
                    createBaseButtons(),
                    user
            );
        }
    }

    @SneakyThrows
    private void sendMyNftInfo(TgUser user) {
        List<Nft> nfts = nftService.getUserNFTs(user);

        Integer purchaseCount = purchaseService.purchaseCount(user);
        String message = String.format(
                "some message",
                nfts.size() + purchaseCount,
                purchaseCount
        );

        telegramSenderService.send(
                new Text(message),
                createBaseButtons(),
                user
        );

        if (!nfts.isEmpty()) {
            Map<Integer, List<Nft>> nftByIndex = nfts.stream().collect(groupingBy(Nft::getIndex));

            long totalNftNumber = nftService.totalNftNumber();
            nftImageAsyncSenderService.sendAsync(nftByIndex.keySet(), (nftImage -> {
                Integer index = nftImage.getIndex();
                Nft nft = nftByIndex.get(index).stream().findFirst().orElseThrow();
                int nftRank = nftService.getNftRank(nft);
                BigDecimal rarity = nft.getRarity();
                Optional.ofNullable(nft.getLastValue())
                        .map(TonCoinUtils::toHumanReadable)
                        .map(lastPrice ->
                                new Text("my_nft.nft_caption")
                                        .param(index)
                                        .param(lastPrice)
                                        .param(rarity)
                                        .param(nftRank)
                                        .param(totalNftNumber)
                        )
                        .or(() -> Optional.of(
                                new Text("my_nft.nft_caption_without_price")
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
            }));
        }
    }

    private void sendInviteInfo(TgUser user) {
        telegramSenderService.send(
                new Text("???? ???????????? ???????????? ???? ???????????????????? <i><b>" + tgUserService.invitedCount(user) + "</b></i> ??????????????\n" +
                        "??????-10 ????????????????????>???????????? ????????????????: </a>\n" +
                        tgUserService.topParticipants()),
                createBaseButtons(),
                user
        );

        telegramSenderService.send(
                new Text("???????? ???????????????????????? ????????????: \n\n" + tgUserService.createInvitationLink(user)),
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
                        ButtonName.BaseLocationButton.BUY_BUTTON,
                        ButtonName.BaseLocationButton.INVITE_INFO_BUTTON,
                        ButtonName.BaseLocationButton.ABOUT_COLLECTION_BUTTON
                )
                .line(
                        ButtonName.BaseLocationButton.MY_NFT_BUTTON,
                        ButtonName.BaseLocationButton.ROYALTY_BUTTON
                )
                .line(
                        ButtonName.BaseLocationButton.AUCTION_BUTTON,
                        ButtonName.BaseLocationButton.SETTINGS_BUTTON
                );
    }

    private void sendRoyaltyInfo(TgUser user) {
        telegramSenderService.send(royaltyService.createInfoMessage(user), createBaseButtons(), user);
    }
}
