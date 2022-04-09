package com.giraffes.tgbot.controller;

import com.giraffes.tgbot.model.CreateGiftDto;
import com.giraffes.tgbot.model.UpdateGiftDto;
import com.giraffes.tgbot.service.GiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GiftController {
    private final GiftService giftService;

    @PostMapping("/gift")
    public void createGift(@Valid @RequestBody CreateGiftDto giftDto) {
        log.info("Gift creating request: {}", giftDto);
        giftService.createGift(giftDto);
    }

    @PatchMapping("/gift")
    public void updateGift(@Valid @RequestBody UpdateGiftDto giftDto) {
        log.info("Gift updating request: {}", giftDto);
        giftService.updateGift(giftDto);
    }
}
