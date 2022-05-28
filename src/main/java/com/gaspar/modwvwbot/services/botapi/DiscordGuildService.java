package com.gaspar.modwvwbot.services.botapi;

import com.gaspar.modwvwbot.controllers.dto.GuildsResponse;
import com.gaspar.modwvwbot.model.DiscordGuild;
import com.gaspar.modwvwbot.repository.DiscordGuildRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Service performing operations in discord guilds.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordGuildService {

    private final DiscordGuildRepository discordGuildRepository;

    /**
     * Get all guilds the bot is member of in an API ready format.
     * @see GuildsResponse
     */
    public GuildsResponse getAllGuilds() {
        var response = new GuildsResponse();
        var guilds = new ArrayList<GuildsResponse.GuildResponse>();
        for(DiscordGuild guild: discordGuildRepository.findAll()) {
            guilds.add(new GuildsResponse.GuildResponse(guild.getGuildName(), guild.getGuildId()));
        }
        response.setGuilds(guilds);
        return response;
    }

    /**
     * Called when the bot joins a discord guild.
     */
    public void joinGuild(Guild guild) {
        var optional = discordGuildRepository.findByGuildId(guild.getIdLong());
        if(optional.isEmpty()) {
            DiscordGuild discordGuild = new DiscordGuild(guild.getIdLong(), guild.getName());
            discordGuildRepository.save(discordGuild);
            log.info("Saved new guild membership: {}", discordGuild);
        } else {
            log.warn("Bot was already part of the guild it joined. This is an illegal state.");
        }
    }

    /**
     * Called when the bot leaves a discord guild.
     */
    public void leaveGuild(Guild guild) {
        var optional = discordGuildRepository.findByGuildId(guild.getIdLong());
        if(optional.isPresent()) {
            discordGuildRepository.deleteById(guild.getIdLong());
            log.info("Deleted guild membership, guild name was '{}'", guild.getName());
        } else {
            log.warn("Bot was not part of guild it just left. This is an illegal state.");
        }
    }

    /**
     * Checks if the bot is currently in a guild.
     */
    public boolean isInGuild(long guildId) {
        var optional = discordGuildRepository.findByGuildId(guildId);
        return optional.isPresent();
    }
}
