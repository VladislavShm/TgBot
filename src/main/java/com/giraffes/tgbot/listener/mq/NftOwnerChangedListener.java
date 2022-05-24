package com.giraffes.tgbot.listener.mq;

import com.giraffes.tgbot.entity.Nft;
import com.giraffes.tgbot.entity.TgGroup;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.service.NftService;
import com.giraffes.tgbot.service.PCloudProvider;
import com.giraffes.tgbot.service.TelegramSenderService;
import com.giraffes.tgbot.service.TgGroupService;
import com.giraffes.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collections;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class NftOwnerChangedListener {
    private final TelegramSenderService telegramSenderService;
    private final PCloudProvider pCloudProvider;
    private final TgGroupService tgGroupService;
    private final NftService nftService;

    @RabbitListener(concurrency = "1", queues = "notification.nft-owner-changed")
    public void process(Long nftId) {
        try {
            Nft nft = nftService.getNftById(nftId);
            if (nft.getLastValue() == null) {
                log.warn("Received empty NFT value. ID: {}", nft.getId());
                return;
            }

            pCloudProvider.imageDataByIndexes(Collections.singleton(nft.getIndex()))
                    .stream()
                    .findFirst()
                    .ifPresentOrElse(
                            (imageData) -> {
                                for (TgGroup tgGroup : tgGroupService.findAllForOwnerChangedNotification()) {
                                    try {
                                        telegramSenderService.sendImageToGroup(
                                                new Text("notification.nft_owner_changed")
                                                        .param(nft.getIndex() + 1)
                                                        .param(TonCoinUtils.toHumanReadable(nft.getLastValue())),
                                                IOUtils.toByteArray(imageData.getInputStream()),
                                                imageData.getFilename(),
                                                tgGroup
                                        );
                                    } catch (Exception e) {
                                        ExceptionUtils.rethrow(e);
                                    }
                                }
                            },
                            () -> log.error("NFT with index {} was found on cloud", nft.getIndex())
                    );
        } catch (Exception e) {
            log.error("Error while sending NFT owner changed notification. ", e);
        }
    }
}
