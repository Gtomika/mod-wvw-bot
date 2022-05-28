package com.gaspar.modwvwbot.controllers.dto;

import com.gaspar.modwvwbot.model.CommandUsageStatistic;
import lombok.Data;

import java.time.Month;
import java.util.List;

/**
 * Command usage statistics in one guild at a certain year and month.
 */
@Data
public class GuildStatisticResponse {

    private int year;

    private Month month;

    private List<CommandUsageResponse> commands;

    /**
     * Adds a new statistic to this response.
     * @param statistic The statistic, year and month is assumed to be the same as this response's year and month.
     */
    public void appendStatistic(CommandUsageStatistic statistic) {
        for(CommandUsageResponse command: commands) {
            if(command.getName().equals(statistic.getCommandName())) {
                //there is already data about this command, increase count
                command.increaseWith(statistic.getCount());
                return;
            }
        }
        //command was not saved yet
        commands.add(new CommandUsageResponse(statistic.getCommandName(), statistic.getCount()));
    }

}
