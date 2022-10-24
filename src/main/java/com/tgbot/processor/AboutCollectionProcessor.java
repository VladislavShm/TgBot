package com.tgbot.processor;

import com.tgbot.entity.Location;
import com.tgbot.entity.Nft;
import com.tgbot.entity.TgUser;
import com.tgbot.model.NftImage;
import com.tgbot.model.internal.telegram.ButtonName;
import com.tgbot.model.internal.telegram.Keyboard;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.service.NftService;
import com.tgbot.service.PCloudProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AboutCollectionProcessor extends LocationProcessor {
    private static final Pattern NFT_INDEX_PATTERN = Pattern.compile("^\\d+$");
    private final PCloudProvider pCloudProvider;
    private final NftService nftService;

    @Override
    public Location getLocation() {
        return Location.ABOUT_COLLECTION;
    }

    @Override
    protected Optional<Location> processText(TgUser user, String text, boolean redirected) {
        Optional<ButtonName.OkButton> okButton = messageToButtonTransformer.determineButton(text, ButtonName.OkButton.class);
        if (redirected || okButton.isPresent()) {
            sendBaseMessage(user);
            return Optional.empty();
        }

        Optional<ButtonName.BackCancelButton> backButton = messageToButtonTransformer.determineButton(text, ButtonName.BackCancelButton.class);
        if (backButton.isPresent()) {
            return Optional.of(Location.BASE);
        }

        if (NFT_INDEX_PATTERN.matcher(text).find()) {
            Integer lastIndex = nftService.getLastIndex();
            int index = Integer.parseInt(text);
            if (lastIndex < index) {
                telegramSenderService.send(new Text("about_collection.invalid_range").param(lastIndex), new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON), user);
            } else {
                Nft nft = nftService.findByIndex(index).orElseThrow();
                int nftRank = nftService.getNftRank(nft);
                long totalNftNumber = nftService.totalNftNumber();
                NftImage nftImage = pCloudProvider.imageDataByIndex(index);

                telegramSenderService.sendImage(
                        new Text("about_collection.nft_info")
                                .param(index)
                                .param(nft.getRarity())
                                .param(nftRank)
                                .param(totalNftNumber),
                        nftImage.getImage(),
                        nftImage.getFilename(),
                        new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON),
                        user
                );
            }
        }

        return Optional.empty();
    }

    private void sendBaseMessage(TgUser user) {
        telegramSenderService.send(new Text("about_collection.base_message"), new Keyboard(ButtonName.BackCancelButton.BACK_BUTTON), user);
    }
}
