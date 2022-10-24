package com.tgbot.listener.mq;

import com.tgbot.entity.Nft;
import com.tgbot.model.NftImage;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.service.NftService;
import com.tgbot.service.PCloudProvider;
import com.tgbot.service.TelegramSenderService;
import com.tgbot.service.TgGroupService;
import com.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class NftOwnerChangedListener {
    private final TelegramSenderService telegramSenderService;
    private final PCloudProvider pCloudProvider;
    private final TgGroupService tgGroupService;
    private final NftService nftService;

    @Value("${nft-owner-changed.notification.enabled}")
    private boolean notificationEnabled;

    @RabbitListener(concurrency = "1", queues = "notification.nft-owner-changed")
    public void process(Long nftId) {
        if (true) {
            log.debug("NFT owner changed notification disabled, SKipping NFT: " + nftId);
            return;
        }

        try {
            Nft nft = nftService.getNftById(nftId);
            if (nft.getLastValue() == null) {
                log.warn("Received empty NFT value. ID: {}", nft.getId());
                return;
            }

            int nftRank = nftService.getNftRank(nft);
            long totalNftNumber = nftService.totalNftNumber();
            NftImage nftImage = pCloudProvider.imageDataByIndex(nft.getIndex());
            tgGroupService.findAllForOwnerChangedNotification()
                    .forEach(tgGroup -> telegramSenderService.sendImageToGroup(
                            new Text("notification.nft_owner_changed")
                                    .param(nft.getIndex())
                                    .param(TonCoinUtils.toHumanReadable(nft.getLastValue()))
                                    .param(nft.getRarity())
                                    .param(nftRank)
                                    .param(totalNftNumber)
                            ,
                            nftImage.getImage(),
                            nftImage.getFilename(),
                            tgGroup
                    ));
        } catch (Exception e) {
            log.error("Error while sending NFT owner changed notification. ", e);
        }
    }
}
