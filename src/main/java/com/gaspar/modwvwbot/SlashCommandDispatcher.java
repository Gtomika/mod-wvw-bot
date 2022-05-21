package com.gaspar.modwvwbot;

import com.gaspar.modwvwbot.config.SlashCommandConfig;
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

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.debug("Slash command interaction arrived, dispatching to appropriate service...");
        try {
            String command = getCommandName(event);
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
