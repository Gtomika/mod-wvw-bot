package com.gaspar.modwvwbot.services.botapi;

import com.gaspar.modwvwbot.controllers.dto.CommandUsageResponse;
import com.gaspar.modwvwbot.controllers.dto.GuildStatisticResponse;
import com.gaspar.modwvwbot.controllers.dto.MonthlyCommandUsageResponse;
import com.gaspar.modwvwbot.controllers.dto.UsageResponse;
import com.gaspar.modwvwbot.exception.NotFoundException;
import com.gaspar.modwvwbot.model.CommandUsageStatistic;
import com.gaspar.modwvwbot.repository.CommandUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Handles command usage related operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommandUsageService {

    private final CommandUsageRepository commandUsageRepository;
    private final DiscordGuildService discordGuildService;

    /**
     * Save that a command was used.
     * @param guildId Discord ID of the source guild. Can also be {@link com.gaspar.modwvwbot.model.CommandUsageStatistic#PRIVATE_MESSAGE_SOURCE}
     *                if this command came from a private message.
     * @param commandName The name of the command such as '/wvw_rank'.
     */
    public void saveCommandUsage(long guildId, String commandName) {
        int year = Year.now().getValue();
        Month month = LocalDateTime.now().getMonth();

        var optional = commandUsageRepository.findByGuildIdAndCommandNameAndYearAndMonth(guildId, commandName, year, month);
        if(optional.isPresent()) {
            //command already used in this guild this month, increment
            var usage = optional.get();
            usage.increment();
            commandUsageRepository.save(usage);
            log.debug("Incremented command usage of '{}' in guild with ID '{}'", commandName, guildId);
        } else {
            //used first time this guild this month
            var usage = CommandUsageStatistic.builder()
                    .guildId(guildId)
                    .commandName(commandName)
                    .year(year)
                    .month(month)
                    .count(1)
                    .build();
            commandUsageRepository.save(usage);
            log.info("Command '{}' used for the first time this month in guild with ID '{}'", commandName, guildId);
        }
    }

    private static final Comparator<GuildStatisticResponse> guildResponseComparator
            = Comparator.comparing(GuildStatisticResponse::getYear).reversed()
            .thenComparing(response -> response.getMonth().getValue()).reversed();

    /**
     * Get command usage statistics in an API ready format from one guild.
     * @throws NotFoundException If no guild was found with the ID that the bot is a member of.
     */
    public UsageResponse<GuildStatisticResponse> getStatisticsFromGuild(long guildId) throws NotFoundException {
        if(discordGuildService.isInGuild(guildId)) {
            //each is a year+month+commandName+guild row
            List<CommandUsageStatistic> guildStats = commandUsageRepository.findByGuildId(guildId);

            //each is a year+month+command list combo
            List<GuildStatisticResponse> responses = new ArrayList<>();

            for(var guildStat: guildStats) {
                addGuildStatistic(responses, guildStat);
            }

            //sort by year desc and then month desc
            responses.sort(guildResponseComparator);

            return new UsageResponse<>(responses);
        } else {
            throw new NotFoundException("The bot is not part of any guild with discord ID '" + guildId + "'!");
        }
    }

    /**
     * Add statistic to responses list (either as new element, or appending it to an existing element).
     */
    private void addGuildStatistic(List<GuildStatisticResponse> responses, CommandUsageStatistic statistic) {
        for(GuildStatisticResponse response: responses) {
            //is this response the same year+month as the statistic?
            if(sameYearAndMonth(response, statistic)) {
                //yes, append to this
                response.appendStatistic(statistic);
                return;
            }
        }
        //there was no response with this year+month, add a new one
        var response = new GuildStatisticResponse();
        response.setYear(statistic.getYear());
        response.setMonth(statistic.getMonth());
        var commandList = new ArrayList<CommandUsageResponse>();
        commandList.add(new CommandUsageResponse(statistic.getCommandName(), statistic.getCount()));
        response.setCommands(commandList);
        responses.add(response);
    }

    public boolean sameYearAndMonth(GuildStatisticResponse response, CommandUsageStatistic statistic) {
        return response.getYear() == statistic.getYear() && response.getMonth() == statistic.getMonth();
    }

    private static final Comparator<MonthlyCommandUsageResponse> commandResponseComparator
            = Comparator.comparing(MonthlyCommandUsageResponse::getYear).reversed()
            .thenComparing(response -> response.getMonth().getValue()).reversed();

    /**
     * Get all statistics of a specified command.
     * @param commandName Command name WITHOUT slash at the start.
     */
    public UsageResponse<MonthlyCommandUsageResponse> getCommandStatistics(String commandName) {
        commandName = "/" + commandName;
        List<CommandUsageStatistic> statistics = commandUsageRepository.findByCommandName(commandName);

        List<MonthlyCommandUsageResponse> responses = new ArrayList<>();

        for(var statistic: statistics) {
            addCommandStatistic(responses, statistic);
        }

        responses.sort(commandResponseComparator);
        return new UsageResponse<>(responses);
    }

    public boolean sameYearAndMonth(MonthlyCommandUsageResponse response, CommandUsageStatistic statistic) {
        return response.getYear() == statistic.getYear() && response.getMonth() == statistic.getMonth();
    }

    private void addCommandStatistic(List<MonthlyCommandUsageResponse> responses, CommandUsageStatistic statistic) {
        for(MonthlyCommandUsageResponse response: responses) {
            //is it in the same year+month?
            if(sameYearAndMonth(response, statistic)) {
                //yes, add it here
                response.increaseWith(statistic.getCount());
                return;
            }
        }
        //add new year+month for it
        var commandResponse = new MonthlyCommandUsageResponse(statistic.getYear(), statistic.getMonth(), statistic.getCount());
        responses.add(commandResponse);
    }

}
