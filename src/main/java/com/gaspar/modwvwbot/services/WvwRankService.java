package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.gw2api.Gw2Account;
import com.gaspar.modwvwbot.model.gw2api.HomeWorldResponse;
import com.gaspar.modwvwbot.services.gw2api.Gw2AccountService;
import com.gaspar.modwvwbot.services.gw2api.Gw2WorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WvwRankService implements SlashCommandHandler {

    private static final String WVW_RANK_COMMAND = "/wvw_rank";

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    @Value("${com.gaspar.modwvwbot.emote_ids.commander}")
    private long commanderEmoteId;

    private final ApiKeyService apiKeyService;
    private final Gw2AccountService gw2AccountService;
    private final Gw2WorldService gw2WorldService;

    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        log.info("/wvw_rank command sent by '{}'. Checking for API key...", event.getUser().getName());
        var apiKey = apiKeyService.getApiKeyByUserId(event.getUser().getIdLong());
        if(apiKey.isPresent()) {
            //this user already added an API key
            event.deferReply().queue(interactionHook -> getAndSendWvwRank(apiKey.get().getKey(), interactionHook, event.getUser().getName()));
        } else {
            log.info("User '{}' has no API key added, and the /wvw_rank command can't be started.", event.getUser().getName());
            event.reply(apiKeyService.getNoApiKeyAddedMessage()).queue();
        }
    }

    private void getAndSendWvwRank(String apiKey, InteractionHook hook, String userName) {
        try {
            String loading = EmoteUtils.animatedEmote("loading", loadingId);
            hook.editOriginal("WvW profilod olvasása... " + loading).queue();
            Gw2Account account = gw2AccountService.fetchGw2User(apiKey);
            if(account.getWvwLevel() == null) {
                log.info("User '{}'-s API key has no 'progression' permission to get WvW rank.", userName);
                throw new UnauthorizedException("No permission to read WvW rank.");
            }
            //resolve world
            HomeWorldResponse homeWorldResponse = gw2WorldService.fetchHomeWorldById(account.getWorldId());
            String message = getResponseMessage(account, homeWorldResponse.getName());
            hook.editOriginal(message).queue();
        } catch (UnauthorizedException e) {
            hook.editOriginal(apiKeyService.getNoPermissionsMessage()).queue();
        } catch (Gw2ApiException e) {
            String error = EmoteUtils.defaultEmote("no_entry_sign");
            hook.editOriginal("A Gw2 API hibás választ adott, vagy nem válaszolt " + error + ". Ez nem a te hibád, próbáld újra " +
                    "kicsit később.").queue();
        }
    }

    private String getResponseMessage(Gw2Account account, String homeWorld) {
        var message = new StringBuilder();
        message.append("**Jelentés**: ").append(account.getAccountName()).append("\n");
        message.append(" - WvW rang: ").append(account.getWvwLevel()).append(" (")
                .append(getWvwTier(account.getWvwLevel())).append(")\n");
        message.append(" - Világ: ").append(homeWorld).append("\n");

        String commander = EmoteUtils.customEmote("commander", commanderEmoteId);
        message.append(" - Commander tag: ").append(account.getHasCommanderTag() ? "megvan " : "nincs meg ").append(commander);
        return message.toString();
    }

    private String getWvwTier(Integer rank) {
        if(rank >= 10000) {
            return "God of WvW";
        }
        if(rank < 150) {
            return "fokozat nélküli";
        } else if(rank < 620) {
            return "bronz fokozat";
        } else if(rank < 1395) {
            return "ezüst fokozat";
        } else if(rank < 2545) {
            return "arany fokozat";
        } else if(rank < 4095) {
            return "platinum fokozat";
        } else if(rank < 6445) {
            return "mithril fokozat";
        } else {
            return "gyémánt fokozat";
        }
    }

    @Override
    public String commandName() {
        return WVW_RANK_COMMAND;
    }
}
