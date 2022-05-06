package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.HomeWorldNotFoundException;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.HomeWorld;
import com.gaspar.modwvwbot.model.gw2api.HomeWorldResponse;
import com.gaspar.modwvwbot.model.gw2api.Population;
import com.gaspar.modwvwbot.repository.HomeWorldRepository;
import com.gaspar.modwvwbot.services.gw2api.Gw2WorldService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Handles the /home_world command.
 */
@Service
@Slf4j
public class HomeWorldCommandService implements SlashCommandHandler {

    private static final String HOME_WORLD_COMMAND = "/home_world";

    private static final String OPTION_WORLD_NAME = "world_name";

    private final AuthorizationService authorizationService;
    private final HomeWorldRepository homeWorldRepository;
    private final Gw2WorldService gw2WorldService;
    private final JDA jda;
    private final ChannelCommandsService channelCommandsService;
    private final RoleCommandsService roleCommandsService;

    @Value("${com.gaspar.modwvwbot.emote_ids.gem}")
    private long gemEmoteId;

    public HomeWorldCommandService(AuthorizationService authorizationService, HomeWorldRepository homeWorldRepository,
                                   Gw2WorldService gw2WorldService, @Lazy JDA jda, ChannelCommandsService channelCommandsService,
                                   RoleCommandsService roleCommandsService) {
        this.authorizationService = authorizationService;
        this.homeWorldRepository = homeWorldRepository;
        this.gw2WorldService = gw2WorldService;
        this.jda = jda;
        this.channelCommandsService = channelCommandsService;
        this.roleCommandsService = roleCommandsService;
    }

    @Override
    public void handleSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        var check = getValidWorldNameOptionOrNull(event);
        if(check.isProvided() && !check.isValid()) {
            return; //error response already sent
        }
        if(check.isProvided()) {
            //authorize
            if(authorizationService.authorize(event)) {
                //set new home world
                saveNewHomeWorld(event, check.homeWorldResponse);
            }
        } else {
            replyWithCurrentHomeWorld(event);
        }
    }

    /**
     * Get the name of the command which is handled by this service.
     */
    @Override
    public String commandName() {
        return HOME_WORLD_COMMAND;
    }

    @lombok.Value
    static class OptionWorldCheck {
        boolean provided;
        boolean valid;
        HomeWorldResponse homeWorldResponse;
    }

    /**
     * Extract parameter 'world_name' from the command. It is allowed for this to not be present, in this
     * case the bot will reply with the current home world. If it is present, it must be a real GW2 world.
     * @return Valid world name or null.
     */
    private OptionWorldCheck getValidWorldNameOptionOrNull(SlashCommandInteractionEvent event) {
        var optionName = event.getOption(OPTION_WORLD_NAME);
        if(optionName == null) {
            return new OptionWorldCheck(false, true, null);
        }
        //is present, validate
        try {
            log.debug("Validating home world name '{}' with GW2 API...", optionName.getAsString());
            var response = gw2WorldService.fetchHomeWorldByName(optionName.getAsString());
            return new OptionWorldCheck(true, true, response);
        } catch (Gw2ApiException e) {
            log.warn("Failed to validate world with name '{}' because of GW2 API failure.", optionName.getAsString(), e);
            event.reply("A GW2 API hibás választ adott. Sajnos nem sikerült beállítani az új világot.").queue();
            return new OptionWorldCheck(true, false, null);
        } catch (HomeWorldNotFoundException e) {
            log.info("'{}' is not a valid GW2 world name.", optionName.getAsString());
            event.reply("A *'" + optionName.getAsString() + "'* nem egy GW2 világ. Ellenőrizd, hogy nem " +
                    "gépelted-e el. Figyelem, a nem angol világoknál a nyelvi tag is a név része, pl: 'Dzagonur [DE]'.").queue();
            return new OptionWorldCheck(true, false, null);
        }
    }

    private void replyWithCurrentHomeWorld(SlashCommandInteractionEvent event) {
        var homeWorld = homeWorldRepository.findByGuildId(event.getGuild().getIdLong());
        if(homeWorld.isPresent()) {
            //reply with current home world
            log.info("'{}' has queried the home world of guild '{}', which is '{}'.", event.getUser().getName(),
                    event.getGuild().getName(), homeWorld.get().getWorldName());

            StringBuilder message = new StringBuilder();
            message.append(" - A guild WvW világa jelenleg **").append(homeWorld.get().getWorldName()).append("**\n")
                            .append(" - A világ telítettsége: ").append(homeWorld.get().getPopulation().getHungarian());
            if(homeWorld.get().getPopulation() != Population.Full) {
                String gemEmote = EmoteUtils.customEmote("gem", gemEmoteId);
                message.append("\n").append(" - A transfer költsége: ").append(homeWorld.get().getPopulation().getTransferCost())
                        .append(gemEmote);
            }

            event.reply(message.toString()).queue();
        } else {
            log.info("'{}' has queried the home world of guild '{}', but there is not home world set.",
                    event.getUser().getName(), event.getGuild().getName());
            event.reply("Jelenleg nincs beállítva WvW világ a guildnek. Ez szükséges egyes parancsok működéséhez, és " +
                    "a '/home_world [világ neve]' utasítással tehető meg.").queue();
        }
    }

    private void saveNewHomeWorld(SlashCommandInteractionEvent event, HomeWorldResponse homeWorldResponse) {
        var homeWorldOpt = homeWorldRepository.findByGuildId(event.getGuild().getIdLong());
        if(homeWorldOpt.isPresent()) {
            log.info("'{}' has set a home world for guild '{}'. Previous one was '{}'.",
                    event.getUser().getName(), event.getGuild().getName(), homeWorldOpt.get().getWorldName());
            //update
            HomeWorld updatedHomeWorld = homeWorldOpt.get();
            updatedHomeWorld.setWorldName(homeWorldResponse.getName());
            updatedHomeWorld.setWorldId(homeWorldResponse.getId());
            updatedHomeWorld.setPopulation(homeWorldResponse.getPopulation());
            homeWorldRepository.save(updatedHomeWorld);
        } else {
            log.info("'{}' has set a home world for guild '{}'. Previously there was no home world set.",
                    event.getUser().getName(), event.getGuild().getName());
            HomeWorld newHomeWorld = HomeWorld.builder()
                    .guildId(event.getGuild().getIdLong())
                    .worldName(homeWorldResponse.getName())
                    .worldId(homeWorldResponse.getId())
                    .population(homeWorldResponse.getPopulation())
                    .build();
            homeWorldRepository.save(newHomeWorld);
        }
        event.reply("A guild WvW világa mostantól **" + homeWorldResponse.getName() + "**.").queue();
    }

    /**
     * Run a job every day once that checks if the home worlds have filled up, or in case if they were full,
     * opened up. These events trigger notifications on the announcement channels of the guild.
     */
    @Scheduled(cron = "0 0 16 * * *")
    public void runHomeWorldJob() {
        var homeWorlds = homeWorldRepository.findAll();
        log.info("Running home world population check job for {} guilds...", homeWorlds.size());

        for(HomeWorld homeWorld: homeWorlds) {
            var guild = jda.getGuildById(homeWorld.getGuildId());
            if(guild == null) {
                log.warn("No guild with ID '{}' was found, even though it has a home world set. Skipping...", homeWorld.getGuildId());
                continue;
            }
            try {
                HomeWorldResponse response = gw2WorldService.fetchHomeWorldById(homeWorld.getWorldId());
                if(homeWorld.getPopulation() == Population.Full && response.getPopulation() != Population.Full) {
                    log.info("Home world of guild '{}', which is '{}' is no longer full. Sending notification...",
                            guild.getName(), homeWorld.getWorldName());
                    homeWorld = saveWorldPopulationChange(homeWorld, response.getPopulation());
                    notifyGuildAboutHomeWorldOpened(guild, homeWorld);
                } else if(homeWorld.getPopulation() != Population.Full && response.getPopulation() == Population.Full) {
                    log.info("Home world of guild '{}', which is '{}' is now full. Sending notification...",
                            guild.getName(), homeWorld.getWorldName());
                    homeWorld = saveWorldPopulationChange(homeWorld, response.getPopulation());
                    notifyGuildAboutHomeWorldClosed(guild, homeWorld);
                } else {
                    log.debug("Home world of guild '{}', which is '{}' has not opened or closed. It is still: {}",
                            guild.getName(), homeWorld.getWorldName(), homeWorld.getPopulation().name());
                }
            } catch (Exception e) {
                log.error("Failed to check population change for guild '{}'.", guild.getName(), e);
            }
        }
    }

    private void notifyGuildAboutHomeWorldOpened(Guild guild, HomeWorld homeWorld) {
        var wvwRoles = roleCommandsService.getWvwRoleIdsFormatted(homeWorld.getGuildId());
        if(wvwRoles.isEmpty()) {
            log.info("Guild '{}' has no WvW roles, so the notification will not ping anyone.", guild.getName());
        }

        String gemEmote = EmoteUtils.customEmote("gem", gemEmoteId);
        StringBuilder message = new StringBuilder();
        message.append("Figyelem: a guild WvW világa, **").append(homeWorld.getWorldName())
                .append("**, mostantól nyitva van.\n")
                .append(" - Az új telítettség: ").append(homeWorld.getPopulation().getHungarian()).append("\n")
                .append(" - A transfer költsége ").append(homeWorld.getPopulation().getTransferCost()).append(gemEmote);
        for(String role: wvwRoles) {
            message.append(role).append(" ");
        }

        var announcementChannels = channelCommandsService.getAnnouncementChannels(homeWorld.getGuildId());
        if(announcementChannels.isEmpty()) {
            log.info("Guild '{}' has no announcement channels, so the notification about population change can't be posted.", guild.getName());
            return;
        }
        for(long channelId: announcementChannels) {
            TextChannel textChannel = jda.getTextChannelById(channelId);
            if(textChannel == null) {
                log.warn("Text channel with ID '{}' was not found in guild '{}'. Skipping...", channelId, guild.getName());
                continue;
            }
            textChannel.sendMessage(message.toString()).queue();
        }
    }

    private void notifyGuildAboutHomeWorldClosed(Guild guild, HomeWorld homeWorld) {
        var wvwRoles = roleCommandsService.getWvwRoleIdsFormatted(homeWorld.getGuildId());
        if(wvwRoles.isEmpty()) {
            log.info("Guild '{}' has no WvW roles, so the notification will not ping anyone.", guild.getName());
        }

        StringBuilder message = new StringBuilder();
        message.append("Figyelem: a guild WvW világa, **").append(homeWorld.getWorldName())
                .append("**, mostantól tele van, nem lehet transferelni oda.\n");
        for(String role: wvwRoles) {
            message.append(role).append(" ");
        }

        var announcementChannels = channelCommandsService.getAnnouncementChannels(homeWorld.getGuildId());
        if(announcementChannels.isEmpty()) {
            log.info("Guild '{}' has no announcement channels, so the notification about population change can't be posted.", guild.getName());
            return;
        }
        for(long channelId: announcementChannels) {
            TextChannel textChannel = jda.getTextChannelById(channelId);
            if(textChannel == null) {
                log.warn("Text channel with ID '{}' was not found in guild '{}'. Skipping...", channelId, guild.getName());
                continue;
            }
            textChannel.sendMessage(message.toString()).queue();
        }
    }

    private HomeWorld saveWorldPopulationChange(HomeWorld homeWorld, Population newPopulation) {
        homeWorld.setPopulation(newPopulation);
        return homeWorldRepository.save(homeWorld);
    }

}
