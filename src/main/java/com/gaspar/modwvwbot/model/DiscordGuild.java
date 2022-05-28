package com.gaspar.modwvwbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Table to store discord guilds.
 */
@Entity
@Table(name = "discord_guild")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DiscordGuild {

    @Id
    @Column(nullable = false, name = "guild_id")
    private long guildId;

    @Column(nullable = false, name = "guild_name")
    private String guildName;

}
