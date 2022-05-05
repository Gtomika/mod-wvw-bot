package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import com.gaspar.modwvwbot.services.gw2api.Gw2InventoryService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that responds to /wvw_items command.
 */
@Service
@Slf4j
public class WvwItemsService extends ListenerAdapter {

    private static final String WVW_ITEMS_COMMAND = "/wvw_items";

    private final List<WvwItemOrCurrency> wvwItems;
    private final ApiKeyService apiKeyService;
    private final Gw2InventoryService gw2InventoryService;

    public WvwItemsService(
            @Qualifier("wvwItems") List<WvwItemOrCurrency> wvwItems,
            ApiKeyService apiKeyService,
            Gw2InventoryService gw2InventoryService) {
        this.wvwItems = wvwItems;
        this.apiKeyService = apiKeyService;
        this.gw2InventoryService = gw2InventoryService;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(WVW_ITEMS_COMMAND)) {
            log.info("/wvw_items command sent by '{}'. Starting item fetching...", event.getUser().getName());

            var apiKey = apiKeyService.getApiKeyByUserId(event.getUser().getIdLong());
            if(apiKey.isPresent()) {
                //this user already added an API key
                event.deferReply().queue(interactionHook -> {
                    try {
                        var amounts = gw2InventoryService
                                .countItemsInInventories(apiKey.get().getKey(), wvwItems, interactionHook);
                        interactionHook.editOriginal("Végeztem").queue();
                    } catch (UnauthorizedException e) {
                        interactionHook.editOriginal(apiKeyService.getNoPermissionsMessage()).queue();
                    } catch (Gw2ApiException e) {
                        String error = EmoteUtils.defaultEmote("no_entry_sign");
                        interactionHook.editOriginal("A Gw2 API hibás választ adott " + error + ". Ez nem a te hibád, próbáld újra " +
                                "kicsit később.").queue();
                    }
                });
            } else {
                log.info("User '{}' has no API key added, and the /wvw_items command can't be started.", event.getUser().getName());
                event.reply(apiKeyService.getNoApiKeyAddedMessage()).queue();
            }


        }
    }
}
