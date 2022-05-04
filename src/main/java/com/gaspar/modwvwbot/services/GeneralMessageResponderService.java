package com.gaspar.modwvwbot.services;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Handles general messages in public channels, which don't belong to
 * any other service's responsibility.
 */
@Service
@RequiredArgsConstructor
public class GeneralMessageResponderService extends ListenerAdapter {

    private final ApiKeyService apiKeyService;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getMessage().isFromGuild() && !event.getAuthor().isBot()) {
            //message on a public channel

            //if user tried to submit api key here, warn
            if(event.getMessage().getContentRaw().startsWith(ApiKeyService.APIKEY_TAG)) {
                apiKeyService.sendPublicMessageApiKeyInfo(event);
            }
        }
    }
}
