package com.gaspar.modwvwbot.services;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class PrivateMessageResponderService extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(!event.isFromGuild() && !event.isWebhookMessage() && !event.getAuthor().isBot()) {
            //only private messages
            if(event.getMessage().getContentRaw().startsWith("/")) {
                event.getMessage().reply("A parancsaimat csak egy szerveren belül tudod használni. Onnan " +
                        "látod majd hogy jó helyen jársz, hogy a discord felajánlja a lehetőségeket amikor elkezdesz gépelni.").queue();
            } else if(event.getMessage().getContentRaw().startsWith("apikey")) {
                //TODO: apikey event
            } else {
                event.getMessage().reply("Privát üzenetben csak az API kulcsodat tudod megadni, sajnos másra itt nem vagyok " +
                        "képes. A kulcsot így adhatod meg: 'apikey [másold ide az API kulcsot]'").queue();
            }
        }
    }
}
