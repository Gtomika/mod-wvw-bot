package com.gaspar.modwvwbot.services;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Answers private messages from users.
 */
@Service
@RequiredArgsConstructor
public class PrivateMessageResponderService extends ListenerAdapter {

    private final ApiKeyService apiKeyService;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(!event.isFromGuild() && !event.isWebhookMessage() && !event.getAuthor().isBot()) {
            //only private messages
            if(event.getMessage().getContentRaw().startsWith("/")) {
                event.getMessage().reply("A parancsaimat csak egy szerveren belül tudod használni. Onnan " +
                        "látod majd hogy jó helyen jársz, hogy a discord felajánlja a lehetőségeket amikor elkezdesz gépelni.").queue();
            } else if(event.getMessage().getContentRaw().startsWith(ApiKeyService.APIKEY_TAG)) {
                apiKeyService.processApiKeyMessage(event);
            } else {
                apiKeyService.sendPrivateMessageApiKeyInfo(event);
            }
        }
    }
}
