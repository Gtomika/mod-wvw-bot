package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.services.gw2api.Gw2WalletService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that handles the /wvw_currencies command.
 */
@Service
@Slf4j
public class WvwCurrenciesService implements SlashCommandHandler {

    private static final String WVW_CURRENCIES_COMMAND = "/wvw_currencies";

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    private final List<WvwItemOrCurrency> wvwItems;
    private final ApiKeyService apiKeyService;
    private final Gw2WalletService gw2WalletService;

    public WvwCurrenciesService(
            @Qualifier("wvwCurrencies") List<WvwItemOrCurrency> wvwItems,
            ApiKeyService apiKeyService,
            Gw2WalletService gw2WalletService) {
        this.wvwItems = wvwItems;
        this.apiKeyService = apiKeyService;
        this.gw2WalletService = gw2WalletService;
    }

    @Override
    public void handleSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        log.info("/wvw_currencies command sent by '{}'. Starting currency fetching...", event.getUser().getName());
        var apiKey = apiKeyService.getApiKeyByUserId(event.getUser().getIdLong());
        if(apiKey.isPresent()) {
            //this user already added an API key
            event.deferReply().queue(interactionHook -> countCurrenciesAndReply(apiKey.get().getKey(), interactionHook));
        } else {
            log.info("User '{}' has no API key added, and the /wvw_currencies command can't be started.", event.getUser().getName());
            event.reply(apiKeyService.getNoApiKeyAddedMessage()).queue();
        }
    }

    /**
     * Get the name of the command which is handled by this service.
     */
    @Override
    public String commandName() {
        return WVW_CURRENCIES_COMMAND;
    }

    /**
     * Count currencies and send a reply. Errors and handled here.
     * @param apiKey API key.
     * @param hook Used to respond to the interaction.
     */
    private void countCurrenciesAndReply(String apiKey, InteractionHook hook) {
        try {
            String loading = EmoteUtils.animatedEmote("loading", loadingId);
            log.info("Fetching currencies from Gw2 API...");
            hook.editOriginal("A fizetőeszközeid lekérdezése... " + loading).queue();
            var amounts = AmountUtils.emptyAmounts(wvwItems);
            gw2WalletService.countCurrenciesInWallet(apiKey, amounts);
            //reply
            sendSummaryReply(amounts, hook);
        } catch (UnauthorizedException e) {
            hook.editOriginal(apiKeyService.getNoPermissionsMessage()).queue();
        } catch (Gw2ApiException e) {
            String error = EmoteUtils.defaultEmote("no_entry_sign");
            hook.editOriginal("A Gw2 API hibás választ adott " + error + ". Ez nem a te hibád, próbáld újra " +
                    "kicsit később.").queue();
        }
    }

    /**
     * Summarize and send the currencies found.
     * @param amounts Amounts of currencies.
     * @param hook Used to respond to the interaction.
     */
    private void sendSummaryReply(List<Amount> amounts, InteractionHook hook) {
        StringBuilder message = new StringBuilder();
        message.append("Ezeket a WvW-s fizetőeszközöket találtam a fiókodban:\n");
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
