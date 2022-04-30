package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.WvwRaid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WvwRaidRepository extends JpaRepository<WvwRaid, Long> {

    Optional<WvwRaid> findByGuildIdAndTime(long guildId, String time);

    List<WvwRaid> findByGuildId(long guildId);

    List<WvwRaid> findByRemindTime(String remindTime);
}
