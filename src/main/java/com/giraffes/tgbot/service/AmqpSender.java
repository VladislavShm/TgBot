package com.giraffes.tgbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmqpSender {
    private final RabbitTemplate rabbitTemplate;

    public void sendNftOwnerChanged(Long nftId) {
        log.info("Sending notification regarding changing NFT owner changing");
        rabbitTemplate.convertAndSend("notification.nft-owner-changed", nftId);
    }
}
