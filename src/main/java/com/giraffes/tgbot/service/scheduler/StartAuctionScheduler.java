package com.giraffes.tgbot.service.scheduler;

import com.giraffes.tgbot.service.TelegramSenderService;
import com.giraffes.tgbot.service.TgUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

import static com.giraffes.tgbot.utils.TelegramUiUtils.createOkKeyboard;

@Component
@Transactional
@RequiredArgsConstructor
public class StartAuctionScheduler {
    private final TelegramSenderService telegramSenderService;
    private final TgUserService tgUserService;

    public void run(Integer auctionOrderNumber) {
        List<String> chatIds = tgUserService.queryAllChatIds();
        String message = String.format("Начался аукцион № %d. Чтобы принять в нем участие, пожалуйста, пройдите в раздел с аукционами.", auctionOrderNumber);
        for (String chatId : chatIds) {
            telegramSenderService.send(
                    message,
                    createOkKeyboard(),
                    chatId
            );
        }
    }
}
