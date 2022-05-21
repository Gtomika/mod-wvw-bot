package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.misc.MatchupUtils;
import com.gaspar.modwvwbot.misc.TimeUtils;
import com.gaspar.modwvwbot.model.HomeWorld;
import com.gaspar.modwvwbot.model.matchup.MatchupResult;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupSide;
import com.gaspar.modwvwbot.services.gw2api.Gw2WvwService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Handles /wvw_matchup and /next_wvw_matchup commands.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WvwMatchupService implements SlashCommandHandler {

    private static final String WVW_MATCHUP_COMMAND = "/wvw_matchup";
    private static final String NEXT_WVW_MATCHUP_COMMAND = "/next_wvw_matchup";

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    private final HomeWorldCommandService homeWorldCommandService;
    private final Gw2WvwService gw2WvwService;
    private final MatchupUtils matchupUtils;

    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        if(!event.isFromGuild()) {
            log.error("Wvw matchup command was not called from a guild.");
            return;
        }
        long guildId = event.getGuild().getIdLong();
        var homeWorld = homeWorldCommandService.getGuildHomeWorld(guildId);
        if(homeWorld.isPresent()) {
            //send "thinking" response first
            event.deferReply().queue(hook -> {
                try {
                    LocalDateTime resetTime = matchupUtils.getWvwResetTime();
                    boolean nextResetIsRelink = matchupUtils.isRelink(resetTime);

                    if(event.getCommandString().startsWith(WVW_MATCHUP_COMMAND)) {
                        createAndSendWvwMatchupReport(homeWorld.get(), resetTime, nextResetIsRelink, hook);
                    } else if(event.getCommandString().startsWith(NEXT_WVW_MATCHUP_COMMAND)) {
                        createAndSendWvwPrediction(homeWorld.get(), resetTime, nextResetIsRelink, hook);
                    } else {
                        log.error("Unknown command: {}", event.getCommandString());
                        throw new RuntimeException("Unknown command: " + event.getCommandString());
                    }
                } catch (Gw2ApiException e) {
                    String error = EmoteUtils.defaultEmote("no_entry_sign");
                    hook.editOriginal("A Gw2 API hibás választ adott, vagy nem válaszolt " + error + ". " +
                            "Reset után egy-másfél óráig ez várható viselkedés.").queue();
                }
            });
        } else {
            log.info("Attempted to invoke 'Wvw matchup' command from guild with ID '{}'. No home world is set for this guild, ignoring.", guildId);
            event.reply("A guild-nek nincs beállított világa! Kérj meg valakit akinek joga van hozzá, hogy ezt tegye meg a **/home_world** paranccsal.").queue();
        }
    }

    /**
     * Creates and sends report of the current WvW matchup.
     * @param homeWorld Home world whose matchup is to be selected.
     * @param resetTime Time of reset.
     * @param isRelink If there is a re-link next reset.
     * @param hook Used to interact with discord message.
     */
    private void createAndSendWvwMatchupReport(
            HomeWorld homeWorld,
            LocalDateTime resetTime,
            boolean isRelink,
            InteractionHook hook
    ) {
        String loadingEmote = EmoteUtils.animatedEmote("loading", loadingId);
        hook.editOriginal("Wvw állapotának lekérdezése... " + loadingEmote).queue();
        //create report using API
        var report = gw2WvwService.createMatchupReport(homeWorld.getWorldId());
        //build message
        var message = new StringBuilder();
        message.append("**Jelentés** - ").append(homeWorld.getWorldName()).append("jelenlegi matchup-ja:\n");
        message.append(" - Tier ").append(report.getTier()).append("\n");
        message.append(" - ").append(getTimeStringUntilReset(resetTime));
        if(isRelink) {
            String warning = EmoteUtils.defaultEmote("warning");
            message.append(" - Figyelem ").append(warning).append(", relink lesz!\n\n");
        } else {
            message.append("\n\n");
        }
        message.append(getStringByPlacement(report.getFirstPlace(), homeWorld, 1)).append("\n");
        message.append(getStringByPlacement(report.getSecondPlace(), homeWorld, 2)).append("\n");
        message.append(getStringByPlacement(report.getThirdPlace(), homeWorld, 3)).append("\n\n");
        message.append("A jövő heti matchup jóslatáért használd a */next_wvw_matchup* parancsot.");
        hook.editOriginal(message.toString()).queue();
    }

    /**
     * Get how much time is left until Wvw reset (matchup end time).
     * @param resetTime Time of matchup ending.
     */
    private String getTimeStringUntilReset(LocalDateTime resetTime) {
        String resetAt = TimeUtils.getTimeString(resetTime);
        resetAt = TimeUtils.createHungarianTimeString(resetAt);
        //duration until reset
        long minutesUntilReset = LocalDateTime.now(TimeUtils.HU_TIME_ZONE).until(resetTime, ChronoUnit.MINUTES);
        String durationToReset = TimeUtils.createHungarianDurationStringFromMinutes(minutesUntilReset);
        String clock = EmoteUtils.defaultEmote("clock10");
        return "Reset ideje: " + resetAt + " " + clock +  ", hátralévő idő: " + durationToReset;
    }

    /**
     * Gets summary of a world which placed at the given spot in the matchup.
     * @param placement Placement, such as "1" for first place.
     * @param side The side of the matchup.
     * @param homeWorld Home world of the guild from where the command was received.
     */
    private String getStringByPlacement(WvwMatchupSide side, HomeWorld homeWorld, int placement) {
        //is caller's home world on this side?
        boolean homeWorldHere = side.getWorldNames().contains(homeWorld.getWorldName());

        var sideReport = new StringBuilder();
        String colorEmote = EmoteUtils.defaultEmote(side.getColor().getEmoteName());
        //color emote and optional home emote
        sideReport.append(colorEmote).append(" ");
        if(homeWorldHere) {
            String homeEmote = EmoteUtils.defaultEmote("house");
            sideReport.append(homeEmote).append(" ");
        }

        //world names, starting with main world, then linked worlds
        for(int i = side.getWorldNames().size() - 1; i >= 0; i--) {
            if(i == side.getWorldNames().size() - 1) {
                sideReport.append("**").append(side.getWorldNames().get(i)).append("**");
            } else {
                sideReport.append(side.getWorldNames().get(i));
            }

            if(i > 0) {
                sideReport.append(", ");
            } else {
                sideReport.append(": ");
            }
        }
        String placementEmote = placementEmote(placement);
        sideReport.append(side.getVictoryPoints()).append(" pont ");
        sideReport.append(placementEmote);

        sideReport.append("\n");
        sideReport.append(" - Kill-ek száma: ").append(side.getKillCount()).append("\n");
        sideReport.append(" - Halálok száma: ").append(side.getDeathCount()).append("\n");
        var formatter = new DecimalFormat("0.000");
        sideReport.append(" - K/D arány: ").append(formatter.format(side.getKillDeathRatio())).append("\n");

        return sideReport.toString();
    }

    private String placementEmote(int placement) {
        switch (placement) {
            case 1:
                return EmoteUtils.defaultEmote("first_place");
            case 2:
                return EmoteUtils.defaultEmote("second_place");
            case 3:
                return EmoteUtils.defaultEmote("third_place");
            default:
                log.error("Invalid placement: {}", placement);
                throw new IllegalArgumentException("Invalid placement: " + placement);
        }
    }

    /**
     * Creates and sends a prediction of the next WvW matchup.
     * @param homeWorld Home world whose matchup is to be selected.
     * @param resetTime Time of reset.
     * @param isRelink If there is a re-link next reset.
     * @param hook Used to interact with discord message.
     */
    private void createAndSendWvwPrediction(
            HomeWorld homeWorld,
            LocalDateTime resetTime,
            boolean isRelink,
            InteractionHook hook
    ) {
        if(isRelink) {
            //in case of re-link, can't predict
            String warning = EmoteUtils.defaultEmote("warning");
            hook.editOriginal("Figyelem " + warning + ", a következő reset egyben **relink** is. Ilyenkor nem lehet " +
                    "megjósolni hogy mi fog történni.").queue();
            return;
        }

        String loadingEmote = EmoteUtils.animatedEmote("loading", loadingId);
        hook.editOriginal("Wvw állapotának lekérdezése... " + loadingEmote).queue();
        //current matchup
        var currentMatchup = gw2WvwService.createMatchupReport(homeWorld.getWorldId());
        int currentTier = currentMatchup.getTier();
        log.debug("'{}'-s current tier is '{}'", homeWorld.getWorldName(), currentTier);
        //predict next weeks tier
        MatchupResult prediction = matchupUtils.predictNextTierOfHomeWorld(homeWorld.getWorldId(), currentMatchup);
        int predictedTier = currentTier + prediction.getOffset();
        log.debug("'{}'-s predicted tier for next week is '{}'", homeWorld.getWorldName(), predictedTier);
        //need to find out who will be in the predicted tier besides the home world.
        var predictedSides = matchupUtils.getPredictedSides(
                predictedTier,
                prediction,
                currentMatchup.getSideOfWorld(homeWorld.getWorldId()),
                currentMatchup.getPlacingOfWorld(homeWorld.getWorldId())
        );
        String message = getPredictionString(
                homeWorld,
                currentTier,
                predictedTier,
                prediction,
                predictedSides,
                resetTime
        );
        hook.editOriginal(message).queue();
    }

    /**
     * Creates a discord message of next weeks predicted matchup.
     * @param homeWorld Home world whose matchup is predicted.
     * @param currentTier Current tier of home world.
     * @param predictedTier Predicted tier of home world after reset.
     * @param predictedSides Predicted sides who will place next matchup of home world.
     * @param resetTime Time of next reset.
     */
    private String getPredictionString(
            HomeWorld homeWorld,
            int currentTier,
            int predictedTier,
            MatchupResult matchupResult,
            List<WvwMatchupSide> predictedSides,
            LocalDateTime resetTime
    ) {
        var message = new StringBuilder();
        String warningEmote = EmoteUtils.defaultEmote("warning");
        message.append("**Jóslat** - ").append(homeWorld.getWorldName()).append(" jövő heti matchup-ja:\n\n")
                .append("Figyelem ").append(warningEmote)
                .append(", ez csak akkor lesz így, ha a jelenlegi állások nem változnak reset-ig!\n\n");

        message.append("A ").append(homeWorld.getWorldName()).append(" jelenlegi szintje: Tier ")
                .append(currentTier).append("\n");
        switch (matchupResult) {
            case ADVANCES:
                message.append("Várhatóan fel fogunk lépni, új szint: Tier ").append(predictedTier).append("\n\n");
                break;
            case STAYS:
                message.append("Várhatóan maradunk ezen a szinten.\n\n");
                break;
            case DROPS_DOWN:
                message.append("Várhatóan visszaesünk, új szint: Tier ").append(predictedTier).append("\n\n");
                break;
        }

        message.append("Ezek a szerverek lehetnek a következő matchup-ban:\n");
        for(var side: predictedSides) {
            message.append(getPredictedSideString(side, side.containsWorld(homeWorld.getWorldId()))).append("\n");
        }
        message.append("\n");

        message.append(getTimeStringUntilReset(resetTime)).append("\n\n");

        message.append("Inkább a mostani matchup érdekel? Használd a */wvw_matchup* parancsot.");

        return message.toString();
    }

    /**
     * Creates a short summary of a matchup side.
     */
    private String getPredictedSideString(WvwMatchupSide side, boolean isHomeSide) {
        var sideMessage = new StringBuilder();
        sideMessage.append(" - ");
        String colorEmote = EmoteUtils.defaultEmote(side.getColor().getEmoteName());
        sideMessage.append(colorEmote).append(" ");
        if(isHomeSide) {
            String homeEmote = EmoteUtils.defaultEmote("house");
            sideMessage.append(homeEmote).append(" ");
        }
        //world names, starting with main world, then linked worlds
        for(int i = side.getWorldNames().size() - 1; i >= 0; i--) {
            if(i == side.getWorldNames().size() - 1) {
                sideMessage.append("**").append(side.getWorldNames().get(i)).append("**");
            } else {
                sideMessage.append(side.getWorldNames().get(i));
            }

            if(i > 0) {
                sideMessage.append(", ");
            } else {
                sideMessage.append(": ");
            }
        }
        return sideMessage.toString();
    }

    @Override
    public String commandName() {
        return null;
    }

    @Override
    public boolean handlesMultipleCommands() {
        return true;
    }

    @Override
    public String[] commandNames() {
        return new String[] {WVW_MATCHUP_COMMAND, NEXT_WVW_MATCHUP_COMMAND};
    }
}
