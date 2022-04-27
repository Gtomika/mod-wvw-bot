package com.gaspar.modwvwbot.services;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Handles events from discord.
 */
@Service
@Slf4j
public class EventsListenerService extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        //don't reply to bots
        if(event.getAuthor().isBot()) return;

        var attachments = event.getMessage().getAttachments();
        if(!attachments.isEmpty()) {
            event.getMessage().reply("Nice file!").queue();
        }
    }


}
