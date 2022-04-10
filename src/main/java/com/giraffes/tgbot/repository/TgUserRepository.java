package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TgUserRepository extends JpaRepository<TgUser, Long> {
    TgUser findByName(String name);
    TgUser findByChatId(String chatId);

    @Query("select count(tgUser.id) from TgUser tgUser where tgUser.invitedBy = :tgUser")
    Integer invitedCount(@Param("tgUser") TgUser tgUser);
}
