package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.CommandUsageStatistic;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandUsageRepository extends JpaRepository<CommandUsageStatistic, Long> {

    /**
     * Get all statistics from a guild.
     */
    List<CommandUsageStatistic> findByGuildId(long guildId);

    /**
     * Get all command usage statistics of a command.
     */
    List<CommandUsageStatistic> findByCommandName(String commandName);

    /**
     * Find one command usage statistic.
     */
    Optional<CommandUsageStatistic> findByGuildIdAndCommandNameAndYearAndMonth(long guildId, String commandName, int year, Month month);

}
