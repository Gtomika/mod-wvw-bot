package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Handles the simple /help command.
 */
@Service
public class HelpCommandService implements SlashCommandHandler {

    private static final String HELP_COMMAND = "/help";

    @Value("${com.gaspar.modwvwbot.nubaras_discord_id}")
    private long devId;

    @Value("${com.gaspar.modwvwbot.documentation_url}")
    private String documentationUrl;

    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        var message = new StringBuilder();
        String dev = "<@" + devId + ">";
        message.append(" - A fejlesztőm: ").append(dev);
        String docuEmote = EmoteUtils.defaultEmote("bookmark_tabs");
        message.append(" - A dokumentációmért ").append(docuEmote).append(" és forráskódomért lásd:\n")
                .append("   ").append(documentationUrl).append("\n");
        String en = EmoteUtils.defaultEmote("regional_indicator_e") + " " + EmoteUtils.defaultEmote("regional_indicator_n");
        message.append(" - Figyelem, ezek angolul érhetőek el! ").append(en).append("\n");
        message.append(" - Ha hibát fedeztél fel, vagy javaslatod van, kérlek írj a készítőmnek!");
        event.reply(message.toString()).queue();
    }

    @Override
    public String commandName() {
        return HELP_COMMAND;
    }
}
