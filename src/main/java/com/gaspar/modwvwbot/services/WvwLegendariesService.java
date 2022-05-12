package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import com.gaspar.modwvwbot.services.gw2api.Gw2LegendaryService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles the /wvw_legendaries command.
 */
@Service
@Slf4j
public class WvwLegendariesService implements SlashCommandHandler {

    private static final String WVW_LEGENDARIES_COMMAND = "/wvw_legendaries";

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    private final ApiKeyService apiKeyService;
    private final List<WvwItemOrCurrency> wvwLegendaries;
    private final Gw2LegendaryService gw2LegendaryService;

    public WvwLegendariesService(
            ApiKeyService apiKeyService,
            @Qualifier("wvwLegendaries") List<WvwItemOrCurrency> wvwLegendaries,
            Gw2LegendaryService gw2LegendaryService) {
        this.apiKeyService = apiKeyService;
        this.wvwLegendaries = wvwLegendaries;
        this.gw2LegendaryService = gw2LegendaryService;
    }

    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        log.info("/wvw_legendaries command sent by '{}'. Checking for API key...", event.getUser().getName());
        var apiKey = apiKeyService.getApiKeyByUserId(event.getUser().getIdLong());
        if(apiKey.isPresent()) {
            //this user already added an API key
            event.deferReply().queue(interactionHook -> getAndSendWvwLegendaries(apiKey.get().getKey(), interactionHook));
        } else {
            log.info("User '{}' has no API key added, and the /wvw_legendaries command can't be started.", event.getUser().getName());
            event.reply(apiKeyService.getNoApiKeyAddedMessage()).queue();
        }
    }

    private void getAndSendWvwLegendaries(String apiKey, InteractionHook hook) {
        try {
            String loading = EmoteUtils.animatedEmote("loading", loadingId);
            var amounts = AmountUtils.emptyAmounts(wvwLegendaries);
            hook.editOriginal("Legendás tárgyaid lekérdezése... " + loading).queue();
            gw2LegendaryService.countLegendaries(amounts, apiKey);
            String message = getDisplayString(amounts);
            hook.editOriginal(message).queue();
        } catch (UnauthorizedException e) {
            hook.editOriginal(apiKeyService.getNoPermissionsMessage()).queue();
        } catch (Gw2ApiException e) {
            String error = EmoteUtils.defaultEmote("no_entry_sign");
            hook.editOriginal("A Gw2 API hibás választ adott " + error + ". Ez nem a te hibád, próbáld újra " +
                    "kicsit később.").queue();
        }
    }

    /**
     * Create summary message of the legendary items.
     */
    private String getDisplayString(List<Amount> amounts) {
        var message = new StringBuilder();
        message.append("A legendás WvW tárgyaid:\n");
        for(var amount: amounts) {
            message.append(" - ").append(amount.getItemOrCurrency().getName()).append(": ");
            String emote = EmoteUtils.customEmote(amount.getItemOrCurrency().getEmoteName(), amount.getItemOrCurrency().getEmoteId());
            if(amount.getAmount() == 0) {
                message.append("Nincs meg ").append(emote);
            } else if (amount.getAmount() == 1) {
                message.append("Megvan ").append(emote);
            } else {
                message.append(amount.getAmount()).append(" darabod van ").append(emote);
            }
            message.append("\n");
        }
        message.append("(jelenleg technikai nehézségek miatt nem számolom a legendás páncélokat)");
        return message.toString();
    }

    @Override
    public String commandName() {
        return WVW_LEGENDARIES_COMMAND;
    }
}
