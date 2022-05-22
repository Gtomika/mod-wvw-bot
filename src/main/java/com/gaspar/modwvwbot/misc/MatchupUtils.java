package com.gaspar.modwvwbot.misc;

import com.gaspar.modwvwbot.model.matchup.MatchupResult;
import com.gaspar.modwvwbot.model.matchup.WvwColor;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupReport;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupSide;
import com.gaspar.modwvwbot.services.gw2api.Gw2WvwService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Wvw matchup utilities.
 */
@Component
@RequiredArgsConstructor
public class MatchupUtils {

    /**
     * Total number of WvW tiers in the EU region.
     */
    public static final int EW_TIER_COUNT = 5;

    /**
     * Gw2 API identifier for WvW region EU.
     */
    public static final int EU_REGION_ID = 2;

    @Value("${com.gaspar.modwvwbot.reset_time_summer}")
    private int resetHourSummer;

    @Value("${com.gaspar.modwvwbot.reset_time_winter}")
    private int resetHourWinter;

    private final Gw2WvwService gw2WvwService;

    /**
     * Predicts if the home world will advance, stay or drop down a tier after this matchup
     * is finished. This prediction will only be accurate if the ranking by the victory points don't change
     * until reset.
     * @see MatchupResult
     */
    public MatchupResult predictNextTierOfHomeWorld(int homeWorldId, WvwMatchupReport currentMatchup) {
        int placing = currentMatchup.getPlacingOfWorld(homeWorldId);
        //if the world is in first place and NOT in tier 1 already, then it advances
        if(placing == 1 && currentMatchup.getTier() > 1) {
            return MatchupResult.ADVANCES;
        }
        //if the world is in last place and not in last tier already, then it drops down
        if(placing == 3 && currentMatchup.getTier() < EW_TIER_COUNT) {
            return MatchupResult.DROPS_DOWN;
        }
        //all other cases it stays in current tier
        return MatchupResult.STAYS;
    }

    /**
     * Predict the sides who will participate in the home worlds next matchup.
     * @param predictedTier Predicted tier of the home world.
     * @param homeWorldResult Result of current wvw matchup.
     * @param homeWorldSide Side of the home world. This side will always be part of the returned sides,
     *                      although it's color may change.
     * @param homeCurrentPlacement Current placement of home world, for example 1 if home world is leading
     *                             current matchup.
     * @return List of sides who will participate in the next matchup of the home world. Warning, don't use the
     * statistics of these sides, only use world names and colors.
     * @see WvwMatchupSide
     */
    public List<WvwMatchupSide> getPredictedSides(
            int predictedTier,
            MatchupResult homeWorldResult,
            WvwMatchupSide homeWorldSide,
            int homeCurrentPlacement
    ) {
        List<WvwMatchupSide> sides = new ArrayList<>();
        //add home world side (color to be decided)
        sides.add(homeWorldSide);
        //predict other participants
        switch (homeWorldResult) {
            case ADVANCES:
                //the home world advances to then next tier
                //need to find out who stays in the predicted tier and who drops down here
                if(predictedTier == 1) {
                    //predicted tier is top tier, no one will drop down here (home world advances + 2 side stays)
                    WvwMatchupReport predictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier));
                    var first = predictedTierReport.getFirstPlace();
                    first.setColor(WvwColor.green);
                    var second = predictedTierReport.getSecondPlace();
                    second.setColor(WvwColor.blue);
                    sides.add(first);
                    sides.add(second);
                } else {
                    //predicted tier is not top, besides the home world, one side drops down here, and one will stay
                    WvwMatchupReport predictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier));
                    var stays = predictedTierReport.getSecondPlace();
                    stays.setColor(WvwColor.blue);
                    sides.add(stays);
                    WvwMatchupReport abovePredictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier-1));
                    var dropsDown = abovePredictedTierReport.getThirdPlace();
                    dropsDown.setColor(WvwColor.green);
                    sides.add(dropsDown);
                }
                homeWorldSide.setColor(WvwColor.red);
                break;
            case DROPS_DOWN:
                //the home world drops down to the tier below
                //need to find out who advances to this tier and who stays in this tier
                if(predictedTier == EW_TIER_COUNT) {
                    //predicted tier is lowest, nobody advances here, instead 1 drops down (home) and 2 stays
                    WvwMatchupReport predictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier));
                    var second = predictedTierReport.getSecondPlace();
                    second.setColor(WvwColor.blue);
                    var third = predictedTierReport.getThirdPlace();
                    third.setColor(WvwColor.red);
                    sides.add(second);
                    sides.add(third);
                } else {
                    //1 stays in predicted tier and 1 advances from tier below
                    WvwMatchupReport predictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier));
                    var stays = predictedTierReport.getSecondPlace();
                    stays.setColor(WvwColor.blue);
                    sides.add(stays);
                    WvwMatchupReport belowPredictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier+1));
                    var advances = belowPredictedTierReport.getFirstPlace();
                    advances.setColor(WvwColor.red);
                    sides.add(advances);
                }
                homeWorldSide.setColor(WvwColor.green);
                break;
            case STAYS:
                //the home world stays in current tier
                //find out who advances here, and who drops down
                if(predictedTier == 1) {
                    WvwMatchupReport predictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier));
                    WvwMatchupReport belowPredictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier+1));
                    var advances = belowPredictedTierReport.getFirstPlace();
                    advances.setColor(WvwColor.red);
                    sides.add(advances);
                    //home stays in tier 1, but is it second or first?
                    if(homeCurrentPlacement == 2) {
                        var stays = predictedTierReport.getFirstPlace();
                        stays.setColor(WvwColor.green);
                        sides.add(stays);
                        homeWorldSide.setColor(WvwColor.blue);
                    } else {
                        var stays = predictedTierReport.getSecondPlace();
                        stays.setColor(WvwColor.blue);
                        sides.add(stays);
                        homeWorldSide.setColor(WvwColor.green);
                    }
                } else if(predictedTier == EW_TIER_COUNT) {
                    WvwMatchupReport predictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier));
                    WvwMatchupReport abovePredictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier-1));
                    var dropsDown = abovePredictedTierReport.getThirdPlace();
                    dropsDown.setColor(WvwColor.green);
                    sides.add(dropsDown);
                    //home stays in tier 5, but is it seconds or third?
                    if(homeCurrentPlacement == 2) {
                        var stays = predictedTierReport.getThirdPlace();
                        stays.setColor(WvwColor.red);
                        sides.add(stays);
                        homeWorldSide.setColor(WvwColor.blue);
                    } else {
                        var stays = predictedTierReport.getSecondPlace();
                        stays.setColor(WvwColor.blue);
                        sides.add(stays);
                        homeWorldSide.setColor(WvwColor.red);
                    }
                } else {
                    //home stays in a middle tier
                    WvwMatchupReport belowPredictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier+1));
                    WvwMatchupReport abovePredictedTierReport = gw2WvwService.createMatchupReport(matchupId(predictedTier-1));
                    var dropsDown = abovePredictedTierReport.getThirdPlace();
                    dropsDown.setColor(WvwColor.green);
                    sides.add(dropsDown);
                    var advances = belowPredictedTierReport.getFirstPlace();
                    advances.setColor(WvwColor.red);
                    sides.add(advances);
                    homeWorldSide.setColor(WvwColor.blue);
                }
                break;
        }
        return sides;
    }

    /**
     * Calculate WvW reset time. For EU servers it is 20:00 PM when daylight savings are active
     * and 19:00 PM when they aren't.
     */
    public LocalDateTime getWvwResetTime() {
        boolean daylightSavings = TimeUtils.isDaylightSavingsInHungary();
        //reset time depends if daylight saving time is on
        int resetHour = daylightSavings ? resetHourSummer : resetHourWinter;
        LocalDateTime now = LocalDateTime.now(TimeUtils.HU_TIME_ZONE);
        LocalDateTime nextReset;
        if(now.getDayOfWeek() == DayOfWeek.FRIDAY && now.getHour() >= resetHour) {
            //it is friday, after reset, get next friday
            nextReset = now.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        } else {
            //get next friday 20:00 time
            nextReset = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        }
        return nextReset.withHour(resetHour).withMinute(0);
    }

    /**
     * Checks if a reset is a re-link at the same time. This means that
     * paired servers change.
     * @param resetTime Time of next reset.
     */
    public boolean isRelink(LocalDateTime resetTime) {
        LocalDateTime relinkTime = LocalDateTime.now(TimeUtils.HU_TIME_ZONE);
        if(!isOddMonth(relinkTime.getMonth())) {
            //relink is on odd months last friday
            //if this is not an odd month, move to the next
            relinkTime = relinkTime.with(TemporalAdjusters.firstDayOfNextMonth());
        }
        //get last friday of this month
        relinkTime = relinkTime.with(TemporalAdjusters.lastInMonth(DayOfWeek.FRIDAY));
        return isSameDay(resetTime, relinkTime);
    }

    /**
     * Checks if a month is odd, that is 1., 3., 5., etc...
     */
    private boolean isOddMonth(Month month) {
        return month.getValue() % 2 != 0;
    }

    /**
     * Checks if two dates are on the same day.
     */
    private boolean isSameDay(LocalDateTime date1, LocalDateTime date2) {
        return date1.toLocalDate().equals(date2.toLocalDate());
    }

    private String matchupId(int tier) {
        return EU_REGION_ID + "-" + tier;
    }

}
