package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TgUserRepository extends JpaRepository<TgUser, Long> {
    TgUser findByName(String name);
    TgUser findByChatId(String chatId);

    @Query("select count(u.id) from TgUser u where u.invitedBy = :tgUser and u.kicked = false")
    Integer invitedCount(@Param("tgUser") TgUser tgUser);

    @Query("select u.chatId from TgUser u where u.kicked = false")
    List<String> queryAllChatIds();
}
