package com.gaspar.modwvwbot;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Interface for services that want to handle slash commands.
 */
public interface SlashCommandHandler {

    /**
     * Process the command.
     * @param event Command event.
     */
    void handleSlashCommand(SlashCommandInteractionEvent event);

    /**
     * Get the name of the command which is handled by this service.
     */
    String commandName();

    /**
     * @return True only if the handler handles multiple
     * different commands. In this case {@link #commandNames()}
     * instead of {@link #commandName()}.
     * <p>
     * Such handlers are responsible to separate the commands they get.
     */
    default boolean handlesMultipleCommands() {
        return false;
    }

    /**
     * Get all commands which is handled by this handler.
     * Use this only if {@link #handlesMultipleCommands()} is true.
     */
    default String[] commandNames() {
        return new String[] {};
    }
}
