package com.gaspar.modwvwbot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.Month;

/**
 * Command usage table. Saves for each guild the amount of commands used. Includes all command
 * usage: success, fail, server error, anything.
 */
@Entity
@Table(name = "command_usage_stat", uniqueConstraints = @UniqueConstraint(
        columnNames = {"guild_id", "command_name", "year", "month"}
))
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
public class CommandUsageStatistic {

    /**
     * This is used as guild ID of the command is sent in a private message.
     */
    public static final long PRIVATE_MESSAGE_SOURCE = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Discord id of the guild or {@link #PRIVATE_MESSAGE_SOURCE} if it is sent in private message.
     */
    @Column(nullable = false, name = "guild_id")
    private Long guildId;

    /**
     * Year of the statistic.
     */
    @Column(nullable = false)
    private Integer year;

    /**
     * Month of the statistic.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Month month;

    /**
     * Name of the command such as '/wvw_items'.
     */
    @Column(nullable = false, name = "command_name")
    private String commandName;

    /**
     * How many times the command with was used within the guild, in one month.
     */
    @Column(nullable = false)
    private Integer count;

    /**
     * Increase command usage by 1.
     */
    public void increment() {
        ++count;
    }

}
