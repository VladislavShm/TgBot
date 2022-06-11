package com.giraffes.tgbot.processor;


import com.giraffes.tgbot.TgBotApplication;
import com.giraffes.tgbot.entity.Location;
import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.internal.telegram.Keyboard;
import com.giraffes.tgbot.model.internal.telegram.Text;
import com.giraffes.tgbot.repository.TgUserRepository;
import com.giraffes.tgbot.service.IncomingUpdateProcessor;
import com.giraffes.tgbot.service.TelegramSenderService;
import com.giraffes.tgbot.service.TgUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(classes = TgBotApplication.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
public class BaseLocationProcessorTest {
    @Autowired
    private IncomingUpdateProcessor incomingUpdateProcessor;

    @Autowired
    private TgUserService tgUserService;

    @Autowired
    private TgUserRepository tgUserRepository;

    @MockBean
    private TelegramSenderService telegramSenderService;

//    @Test
    void startMessageTest() {
        Mockito.doNothing().when(telegramSenderService).send(anyString(), any(ReplyKeyboard.class));

        long chatId = 1L;

        Update update = createUpdate(chatId, "/start");

        incomingUpdateProcessor.process(update);

        Optional<TgUser> user = tgUserService.findByChatId(String.valueOf(chatId));

        assertTrue(user.isPresent());
        assertFalse(user.get().isJustCreated());
        assertEquals(user.get(), TgUserService.getCurrentUser());
        Mockito.verify(telegramSenderService, Mockito.only()).send(
                any(Text.class),
                any(Keyboard.class),
                eq(user.get())
        );
    }

//    @Test
    @Transactional
    void startWithInvitationMessageTest() {
        Mockito.doNothing().when(telegramSenderService).send(anyString(), any(ReplyKeyboard.class));

        TgUser inviter = new TgUser();
        inviter.setLocation(Location.BASE);
        inviter.setKicked(false);
        inviter.setId(1L);
        tgUserRepository.save(inviter);

        long chatId = 2L;

        Update update = createUpdate(chatId, "/start " + tgUserService.createInvitationUniqueCode(inviter));

        incomingUpdateProcessor.process(update);

        Optional<TgUser> user = tgUserService.findByChatId(String.valueOf(chatId));

        assertTrue(user.isPresent());
        assertTrue(user.get().isJustCreated());
        assertEquals(user.get().getInvitedBy(), inviter);
        assertEquals(user.get(), TgUserService.getCurrentUser());
        Mockito.verify(telegramSenderService, Mockito.only()).send(
                any(Text.class),
                any(Keyboard.class),
                eq(user.get())
        );
    }

    private Update createUpdate(long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);
        User from = new User();
        from.setUserName("username");
        from.setFirstName("firstname");
        from.setLastName("lastname");
        message.setFrom(from);
        Chat chat = new Chat();
        chat.setId(chatId);
        message.setChat(chat);
        update.setMessage(message);
        return update;
    }
}
