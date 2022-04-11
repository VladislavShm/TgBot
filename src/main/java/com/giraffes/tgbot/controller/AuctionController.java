package com.giraffes.tgbot.controller;

import com.giraffes.tgbot.entity.Auction;
import com.giraffes.tgbot.model.AuctionCreationDto;
import com.giraffes.tgbot.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @PostMapping("/auction")
    public Long create(@Valid @RequestBody AuctionCreationDto auctionCreationDto) {
        log.info("Received auction creation request: {}", auctionCreationDto);
        Auction auction = auctionService.create(auctionCreationDto);
        log.info("Auction creation request has been successfully processed");
        return auction.getId();
    }

    @PutMapping("/auction/{id}/nft-image")
    public void uploadNftImage(@RequestParam("file") MultipartFile file, @PathVariable String id) {
        log.info("Received set image request for: {}", id);
        auctionService.addNftImage(file, Long.valueOf(id));
        log.info("Set image request has been successfully processed");
    }

    @PutMapping("/auction/{id}/activate")
    public void activateAuction(@PathVariable String id) {
        log.info("Received activation request for: {}", id);
        auctionService.activate(Long.valueOf(id));
        log.info("Activation request has been successfully processed");
    }
}
