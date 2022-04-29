package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.model.WvwRaid;
import com.gaspar.modwvwbot.repository.WvwRaidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Handles wvw raid slash commands.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WvwRaidService extends ListenerAdapter {

    private static final String WVW_RAID_ADD_COMMAND = "/wvw_raid_add";

    private static final String WVW_RAID_DELETE_COMMAND = "/wvw_raid_delete";

    private static final String WVW_RAID_LIST_COMMAND = "/wvw_raid_list";

    private static final String OPTION_TIME = "time";

    private static final String OPTION_DURATION = "duration";

    private static final String OPTION_REMIND_TIME = "remind_time";

    private final AuthorizationService authorizationService;
    private final WvwRaidRepository wvwRaidRepository;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(WVW_RAID_ADD_COMMAND)) {
            //auth
            if(authorizationService.isUnauthorizedToManageBot(event.getMember())) {
                log.info("Unauthorized user '{}' attempted to invoke command '{}'", event.getUser().getName(), event.getCommandString());
                event.reply(authorizationService.getUnauthorizedMessage()).queue();
                return;
            }
            onWvwRaidAddCommand(event);
        } else if(event.getCommandString().startsWith(WVW_RAID_DELETE_COMMAND)) {
            //auth
            if(authorizationService.isUnauthorizedToManageBot(event.getMember())) {
                log.info("Unauthorized user '{}' attempted to invoke command '{}'", event.getUser().getName(), event.getCommandString());
                event.reply(authorizationService.getUnauthorizedMessage()).queue();
                return;
            }
            onWvwRaidDeleteCommand(event);
        } else if(event.getCommandString().startsWith(WVW_RAID_LIST_COMMAND)) {
            //not authorized command
            onWvwRaidListCommand(event);
        }
    }

    private void onWvwRaidAddCommand(SlashCommandInteractionEvent event) {
        //TODO
        WvwRaid raid = new WvwRaid();
        log.info("Wvw raid added for guild '{}' by '{}': {}", event.getGuild().getName(), event.getUser().getName(), raid);
    }

    private void onWvwRaidDeleteCommand(SlashCommandInteractionEvent event) {
        //TODO: log
    }

    private void onWvwRaidListCommand(SlashCommandInteractionEvent event) {
        //TODO: log
    }
}
