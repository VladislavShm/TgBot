package com.giraffes.tgbot.repository;

import com.giraffes.tgbot.entity.TgUser;
import com.giraffes.tgbot.model.ParticipantInvites;
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

    @Query(value = "SELECT O.\"name\", (SELECT COUNT(*) FROM public.tg_user A WHERE A.invited_by = O.id) " +
            "as invites FROM public.tg_user O ORDER BY invites desc limit 10;", nativeQuery = true)
    List<ParticipantInvites> top10Participants();
}
