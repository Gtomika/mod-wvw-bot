package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.DiscordGuild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordGuildRepository extends JpaRepository<DiscordGuild, Long> {

    Optional<DiscordGuild> findByGuildId(long guildId);

}
