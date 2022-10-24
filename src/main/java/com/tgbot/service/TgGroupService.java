package com.tgbot.service;

import com.tgbot.entity.TgGroup;
import com.tgbot.repository.TgGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TgGroupService {
    private final TgGroupRepository tgGroupRepository;

    public void registerOrUpdateGroup(Update update) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        Chat chat = myChatMember.getChat();
        tgGroupRepository.findByChatId(chat.getId())
                .or(() -> {
                    TgGroup tgGroup = new TgGroup();
                    tgGroup.setChatId(chat.getId());
                    return Optional.of(tgGroup);
                })
                .stream()
                .peek(tgGroup -> tgGroup.setChatTitle(chat.getTitle()))
                .peek(tgGroup -> tgGroup.setStatus(myChatMember.getNewChatMember().getStatus()))
                .peek(tgGroup -> tgGroup.setLocale(Locale.forLanguageTag("ru")))
                .forEach(tgGroupRepository::save);
    }

    public List<TgGroup> findAllForOwnerChangedNotification() {
        return tgGroupRepository.findAllByNftGettingNotificationEnabledIsTrue();
    }
}
