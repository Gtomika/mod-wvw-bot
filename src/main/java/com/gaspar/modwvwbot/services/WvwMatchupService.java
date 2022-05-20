package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.misc.TimeUtils;
import com.gaspar.modwvwbot.model.HomeWorld;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupSide;
import com.gaspar.modwvwbot.services.gw2api.Gw2WvwService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Handles /wvw_matchup command.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WvwMatchupService implements SlashCommandHandler {

    private static final String WVW_MATCHUP_COMMAND = "/wvw_matchup";

    @Value("${com.gaspar.modwvwbot.reset_time_summer}")
    private int resetHourSummer;

    @Value("${com.gaspar.modwvwbot.reset_time_winter}")
    private int resetHourWinter;

    private final HomeWorldCommandService homeWorldCommandService;
    private final Gw2WvwService gw2WvwService;

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
            event.deferReply().queue(hook -> createAndSendWvwMatchupReport(homeWorld.get(), hook));
        } else {
            log.info("Attempted to invoke '/wvw_matchup' command from guild with ID '{}'. No home world is set for this guild, ignoring.", guildId);
            event.reply("A guild-nek nincs beállított világa! Kérj meg valakit akinek joga van hozzá, hogy ezt tegye meg a **/home_world** paranccsal.").queue();
        }
    }

    private void createAndSendWvwMatchupReport(HomeWorld homeWorld, InteractionHook hook) {
        //create report using API
        try {
            var report = gw2WvwService.createMatchupReport(homeWorld.getWorldId());
            report.setEndsAt(getWvwResetTime());
            //build message
            String message = "**Jelentés:**\n" +
                    getTimeStringUntilReset(report.getEndsAt()) + "\n\n" +
                    getStringByPlacement(report.getFirstPlace(), homeWorld, 1) + "\n" +
                    getStringByPlacement(report.getSecondPlace(), homeWorld, 2) + "\n" +
                    getStringByPlacement(report.getThirdPlace(), homeWorld, 3) + "\n";
            hook.editOriginal(message).queue();
        } catch (Gw2ApiException e) {
            String error = EmoteUtils.defaultEmote("no_entry_sign");
            hook.editOriginal("A Gw2 API hibás választ adott, vagy nem válaszolt " + error + ". " +
                    "Reset után egy-másfél óráig ez várható viselkedés.").queue();
        }
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
     * Calculate WvW reset time. For EU servers it is 20:00 PM when daylight savings are active
     * and 19:00 PM when they aren't.
     */
    private LocalDateTime getWvwResetTime() {
        boolean daylightSavings = TimeUtils.isDaylightSavingsInHungary();
        //reset time depends if daylight saving time is on
        int resetHour = daylightSavings ? resetHourSummer : resetHourWinter;
        var now = LocalDateTime.now();
        LocalDateTime nextReset;
        if(now.getDayOfWeek() == DayOfWeek.FRIDAY && now.getHour() >= resetHour) {
            //it is friday, after reset, get next friday
            nextReset = LocalDateTime.now(TimeUtils.HU_TIME_ZONE).with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        } else {
            //get next friday 20:00 time
            nextReset = LocalDateTime.now(TimeUtils.HU_TIME_ZONE).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        }
        return nextReset.withHour(resetHour).withMinute(0);
    }

    @Override
    public String commandName() {
        return WVW_MATCHUP_COMMAND;
    }
}
