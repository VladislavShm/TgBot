package com.giraffes.tgbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AmqpSender {
    private final RabbitTemplate rabbitTemplate;

    public void sendNftOwnerChanged(Long nftId) {
        rabbitTemplate.convertAndSend("notification.nft-owner-changed", nftId);
    }
}
