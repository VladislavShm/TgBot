package com.tgbot.repository;

import com.tgbot.entity.TgGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TgGroupRepository extends JpaRepository<TgGroup, Long> {
    Optional<TgGroup> findByChatId(Long chatId);

    List<TgGroup> findAllByNftGettingNotificationEnabledIsTrue();
}
