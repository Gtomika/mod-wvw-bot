package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.HomeWorldNotFoundException;
import com.gaspar.modwvwbot.model.HomeWorld;
import com.gaspar.modwvwbot.model.gw2api.HomeWorldResponse;
import com.gaspar.modwvwbot.repository.HomeWorldRepository;
import com.gaspar.modwvwbot.services.gw2api.Gw2WorldService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Handles the /home_world command.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeWorldCommandService extends ListenerAdapter {

    private static final String HOME_WORLD_COMMAND = "/home_world";

    private static final String OPTION_WORLD_NAME = "world_name";

    private final AuthorizationService authorizationService;
    private final HomeWorldRepository homeWorldRepository;
    private final Gw2WorldService gw2WorldService;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(HOME_WORLD_COMMAND)) {
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
    }

    @Value
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
            var response = gw2WorldService.fetchHomeWorld(optionName.getAsString());
            return new OptionWorldCheck(true, true, response);
        } catch (Gw2ApiException e) {
            log.warn("Failed to validate world with name '{}' because of GW2 API failure.", optionName.getAsString(), e);
            event.reply("A GW2 API hibás választ adott. Sajnos nem sikerült beállítani az új világot.").queue();
            return new OptionWorldCheck(true, false, null);
        } catch (HomeWorldNotFoundException e) {
            log.info("'{}' is not a valid GW2 world name.", optionName.getAsString());
            event.reply("A '" + optionName.getAsString() + "' nem egy GW2 világ. Ellenőrizd, hogy nem " +
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
            event.reply("A guild WvW világa jelenleg " + homeWorld.get().getWorldName() + "\n" +
                    "A világ telítettsége: " + homeWorld.get().getPopulation().getHungarian())
                    .queue();
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
        event.reply("A guild WvW világa mostantól " + homeWorldResponse.getName() + ".").queue();
    }
}
