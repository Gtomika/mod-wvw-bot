package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.gw2api.Gw2Account;
import com.gaspar.modwvwbot.model.gw2api.HomeWorldResponse;
import com.gaspar.modwvwbot.model.gw2api.WvwRank;
import com.gaspar.modwvwbot.services.gw2api.Gw2AccountService;
import com.gaspar.modwvwbot.services.gw2api.Gw2WorldService;
import com.gaspar.modwvwbot.services.gw2api.Gw2WvwService;
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
    private final Gw2WvwService gw2WvwService;

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
            hook.editOriginal("WvW profilod olvas??sa... " + loading).queue();
            Gw2Account account = gw2AccountService.fetchGw2User(apiKey);
            if(account.getWvwLevel() == null) {
                log.info("User '{}'-s API key has no 'progression' permission to get WvW rank.", userName);
                throw new UnauthorizedException("No permission to read WvW rank.");
            }
            //resolve world
            HomeWorldResponse homeWorldResponse = gw2WorldService.fetchHomeWorldById(account.getWorldId());
            //resolve wvw rank
            var ranks = gw2WvwService.getWvwRanks();
            String rankTitle = findRankTitle(account.getWvwLevel(), ranks);
            //create and send message
            String message = getResponseMessage(account, rankTitle, homeWorldResponse.getName());
            hook.editOriginal(message).queue();
        } catch (UnauthorizedException e) {
            hook.editOriginal(apiKeyService.getNoPermissionsMessage()).queue();
        } catch (Gw2ApiException e) {
            String error = EmoteUtils.defaultEmote("no_entry_sign");
            hook.editOriginal("A Gw2 API hib??s v??laszt adott, vagy nem v??laszolt " + error + ". Ez nem a te hib??d, pr??b??ld ??jra " +
                    "kicsit k??s??bb.").queue();
        }
    }

    /**
     * Create a message describing the user's account.
     * @param account Account details.
     * @param rankTitle Name of the user's wvw rank such as "diamon raider".
     * @param homeWorld Name of the user's home world.
     */
    private String getResponseMessage(Gw2Account account, String rankTitle, String homeWorld) {
        var message = new StringBuilder();
        message.append("**Jelent??s**: ").append(account.getAccountName()).append("\n");
        message.append(" - WvW rang: ").append(account.getWvwLevel()).append(" (")
                .append(rankTitle).append(")\n");
        message.append(" - Vil??g: ").append(homeWorld).append("\n");

        String commander = EmoteUtils.customEmote("commander", commanderEmoteId);
        message.append(" - Commander tag: ").append(account.getHasCommanderTag() ? "megvan " : "nincs meg ").append(commander);
        return message.toString();
    }

    /**
     * Find the user's wvw title from his rank.
     * @param userRank User wvw level.
     * @param ranks All ranks.
     */
    private String findRankTitle(int userRank, WvwRank[] ranks) {
        for(int i = ranks.length - 1; i >= 0; i--) {
            WvwRank rank = ranks[i];
            if(userRank >= rank.getMinRank()) {
                return rank.getTitle();
            }
        }
        return "Invader"; //should not get here
    }

    @Override
    public String commandName() {
        return WVW_RANK_COMMAND;
    }
}
