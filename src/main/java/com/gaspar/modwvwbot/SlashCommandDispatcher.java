package com.gaspar.modwvwbot;

import com.gaspar.modwvwbot.config.SlashCommandConfig;
import com.gaspar.modwvwbot.model.CommandUsageStatistic;
import com.gaspar.modwvwbot.services.botapi.CommandUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Gets all slash command events, and routes them to the different
 * services.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SlashCommandDispatcher extends ListenerAdapter {

    private final SlashCommandConfig.SlashCommandHandlers commandHandlers;
    private final CommandUsageService commandUsageService;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.debug("Slash command interaction arrived: {}", event.getCommandString());

        String command = getCommandName(event);
        log.debug("Saving command usage...");
        if(event.getGuild() != null) {
            commandUsageService.saveCommandUsage(event.getGuild().getIdLong(), command);
        } else {
            commandUsageService.saveCommandUsage(CommandUsageStatistic.PRIVATE_MESSAGE_SOURCE, command);
        }

        log.debug("Dispatching command to appropriate service...");
        try {
            for(var handler: commandHandlers.getHandlerList()) {
                if(handler.handlesMultipleCommands()) {
                    //this handler is responsible for multiple commands
                    //is this command one of them?
                    for(String handledCommand: handler.commandNames()) {
                        if(handledCommand.equals(command)) {
                            handler.handleSlashCommand(event);
                            return;
                        }
                    }
                } else {
                    //handles only one command. is this that command?
                    if(command.equals(handler.commandName())) {
                        handler.handleSlashCommand(event);
                        return;
                    }
                }
            }
            log.warn("Unknown command, unable to route to service: {}", event.getCommandString());
            event.reply("Ismeretlen parancs. Ez a bot hibája, kérlek jelezd a fejlesztő felé.").queue();
        } catch (Exception e) {
            log.error("Other exception while processing slash command.", e);
            event.reply("Hiba történt. Kérlek ezt jelezd a készítőmnek.").queue();
        }
    }

    /**
     * Extract name of the command, such as '/wvw_items'.
     * @param event The command event.
     * @return Command name.
     */
    private String getCommandName(SlashCommandInteractionEvent event) {
        var parts = event.getCommandString().split(" ");
        return parts[0];
    }
}
