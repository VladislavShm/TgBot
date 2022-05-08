package com.giraffes.tgbot.service;

import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.persistence.ParticipantInvites;
import com.giraffes.tgbot.property.BotProperties;
import com.giraffes.tgbot.repository.TgUserRepository;
import com.giraffes.tgbot.utils.TelegramUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgUserService {
    private static final ThreadLocal<TgUser> CURRENT_USER = new ThreadLocal<>();
    private final TgUserRepository tgUserRepository;
    private final BotProperties botProperties;

    public static TgUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public String createInvitationLink(TgUser tgUser) {
        String uniqueCode = createInvitationUniqueCode(tgUser);
        return "https://t.me/" + botProperties.getBotUserName() + "?start=" + uniqueCode;
    }

    public String createInvitationUniqueCode(TgUser tgUser) {
        return new String(Base64.getEncoder().encode(("base64" + tgUser.getId()).getBytes(StandardCharsets.UTF_8)));
    }

    public TgUser authenticateUser(Update update) {
        User user = TelegramUtils.extractUser(update);
        String chatId = TelegramUtils.determineChatId(update);
        return tgUserRepository.findByChatId(chatId)
                .or(() -> Optional.of(createUser(chatId)))
                .stream()
                .peek(tgUser -> {
                    tgUser.setName(user.getUserName());
                    tgUser.setFirstName(user.getFirstName());
                    tgUser.setLastName(user.getLastName());
                })
                .peek(tgUser ->
                        Optional.ofNullable(tgUser.getLocale())
                                .or(() ->
                                        Optional.ofNullable(user.getLanguageCode())
                                                .map(Locale::forLanguageTag)
                                )
                                .ifPresent(LocaleContextHolder::setLocale)
                )
                .peek(CURRENT_USER::set)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User wasn't neither found nor created for: " + chatId));
    }

    private TgUser createUser(String chatId) {
        log.info("Creating a new user: {}", chatId);

        TgUser tgUser = new TgUser();
        tgUser.setChatId(chatId);
        tgUser.setLocation(Location.BASE);
        tgUser.setJustCreated(true);
        tgUser = tgUserRepository.save(tgUser);
        log.info("New user was created: {}", tgUser);

        return tgUser;
    }

    public void onUserBecomeKicked(TgUser user) {
        user.setKicked(true);
        log.debug("User with chat ID = {} became kicked", user.getChatId());
    }

    public void onUserBecomeMember(TgUser user) {
        user.setKicked(false);
        log.debug("User with chat ID = {} became member", user.getChatId());
    }

    public Optional<TgUser> findByUsername(String username) {
        return this.tgUserRepository.findByName(username);
    }

    public String topParticipants() {
        List<ParticipantInvites> topParticipants = tgUserRepository.top10Participants();
        return topParticipants.stream().map(x -> x.getName() + " - " + x.getInvites()).collect(Collectors.joining("\n"));
    }

    public Optional<TgUser> findByChatId(String chatId) {
        return this.tgUserRepository.findByChatId(chatId);
    }

    public List<TgUser> findAllByWalletNotConfirmed(String wallet) {
        return this.tgUserRepository.findAllByWalletAndWalletConfirmedIsFalse(wallet);
    }

    public Integer invitedCount(TgUser tgUser) {
        return tgUserRepository.invitedCount(tgUser);
    }

    public Optional<TgUser> findById(Long inviterId) {
        return tgUserRepository.findById(inviterId);
    }

    public List<TgUser> findAllUsers() {
        return tgUserRepository.findAll();
    }
}
