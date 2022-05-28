package com.gaspar.modwvwbot;

import com.gaspar.modwvwbot.services.botapi.DiscordGuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Handles events when the bot joins or leaves a discord guild.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GuildMembershipHandler extends ListenerAdapter {

    private final DiscordGuildService discordGuildService;

    /**
     * Called when the bot joins a guild.
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        log.info("Bot just joined a discord guild, guild name is '{}'", event.getGuild().getName());
        discordGuildService.joinGuild(event.getGuild());
    }

    /**
     * Called when the bot leaves a guild.
     */
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        log.info("Bot just left a discord guild, guild name is '{}'", event.getGuild().getName());
        discordGuildService.leaveGuild(event.getGuild());
    }
}
