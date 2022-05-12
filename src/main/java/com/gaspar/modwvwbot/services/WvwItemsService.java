package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.services.gw2api.Gw2BankService;
import com.gaspar.modwvwbot.services.gw2api.Gw2InventoryService;
import com.gaspar.modwvwbot.services.gw2api.Gw2StorageService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that responds to /wvw_items command.
 */
@Service
@Slf4j
public class WvwItemsService implements SlashCommandHandler {

    private static final String WVW_ITEMS_COMMAND = "/wvw_items";

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    private final List<WvwItemOrCurrency> wvwItems;
    private final ApiKeyService apiKeyService;
    private final Gw2InventoryService gw2InventoryService;
    private final Gw2BankService gw2BankService;
    private final Gw2StorageService gw2StorageService;

    public WvwItemsService(
            @Qualifier("wvwItems") List<WvwItemOrCurrency> wvwItems,
            ApiKeyService apiKeyService,
            Gw2InventoryService gw2InventoryService,
            Gw2BankService gw2BankService,
            Gw2StorageService gw2StorageService) {
        this.wvwItems = wvwItems;
        this.apiKeyService = apiKeyService;
        this.gw2InventoryService = gw2InventoryService;
        this.gw2BankService = gw2BankService;
        this.gw2StorageService = gw2StorageService;
    }

    @Override
    public void handleSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        log.info("/wvw_items command sent by '{}'. Checking for API key...", event.getUser().getName());
        var apiKey = apiKeyService.getApiKeyByUserId(event.getUser().getIdLong());
        if(apiKey.isPresent()) {
            //this user already added an API key
            event.deferReply().queue(interactionHook -> countItemsAndReply(apiKey.get().getKey(), interactionHook));
        } else {
            log.info("User '{}' has no API key added, and the /wvw_items command can't be started.", event.getUser().getName());
            event.reply(apiKeyService.getNoApiKeyAddedMessage()).queue();
        }
    }

    /**
     * Get the name of the command which is handled by this service.
     */
    @Override
    public String commandName() {
        return WVW_ITEMS_COMMAND;
    }

    /**
     * Count items in every possible place and send a reply. The interaction might fail, which is also handled here.
     * @param apiKey API key of the user.
     * @param interactionHook Used to reply.
     */
    private void countItemsAndReply(String apiKey, InteractionHook interactionHook) {
        try {
            String loading = EmoteUtils.animatedEmote("loading", loadingId);
            var amounts = AmountUtils.emptyAmounts(wvwItems);
            //first count items in character inventories
            log.debug("Counting items of interest in character inventories...");
            gw2InventoryService.countItemsInInventories(apiKey, amounts, interactionHook);
            //count items in the bank
            log.debug("Counting items of interest in the bank...");
            interactionHook.editOriginal("A bankod tartalmának vizsgálata... " + loading).queue();
            gw2BankService.countItemsInBank(apiKey, amounts);
            //count items in the material storage
            log.debug("Counting items of interest in the material storage...");
            interactionHook.editOriginal("A tárhelyed tartalmának vizsgálata... " + loading).queue();
            gw2StorageService.countItemsInStorage(apiKey, amounts);
            //reply
            sendSummaryResponse(amounts, interactionHook);
        } catch (UnauthorizedException e) {
            interactionHook.editOriginal(apiKeyService.getNoPermissionsMessage()).queue();
        } catch (Gw2ApiException e) {
            String error = EmoteUtils.defaultEmote("no_entry_sign");
            interactionHook.editOriginal("A Gw2 API hibás választ adott " + error + ". Ez nem a te hibád, próbáld újra " +
                    "kicsit később.").queue();
        }
    }

    /**
     * After a successful count, summarize the results and send response message.
     * @param amounts Items of interest and how many of them were found.
     * @param hook Used to reply with.
     */
    private void sendSummaryResponse(List<Amount> amounts, InteractionHook hook) {
        StringBuilder message = new StringBuilder();
        message.append("Ezeket a WvW-s tárgyakat találtam a fiókodban:\n");
        for(Amount amount: amounts) {
            String itemEmote = EmoteUtils.customEmote(
                    amount.getItemOrCurrency().getEmoteName(),
                    amount.getItemOrCurrency().getEmoteId()
            );
            message.append(" - ").append(amount.getItemOrCurrency().getName()).append(": ");
            message.append(amount.getAmount()).append(" ").append(itemEmote).append("\n");
        }
        hook.editOriginal(message.toString()).queue();
    }
}
