package com.gaspar.modwvwbot.services;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

/**
 * Responds with error messages if something went wrong.
 */
@Service
@Slf4j
public class ErrorResponderService {

    /**
     * Send error message as reply.
     * @param messageThatCausedError Message that caused error.
     * @param errorToUser Text of the reply, explaining the error.
     */
    public void sendErrorResponse(Message messageThatCausedError, String errorToUser) {
        messageThatCausedError.reply(errorToUser).queue();
    }

    /**
     * Send error message to a slash command interaction.
     * @param event The slash command interaction.
     * @param errorToUser Text of the reply, explaining the error.
     */
    public void sendSlashCommandErrorResponse(SlashCommandInteractionEvent event, String errorToUser) {
        event.reply(errorToUser).queue();
    }

}
