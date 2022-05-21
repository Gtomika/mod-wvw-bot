package com.gaspar.modwvwbot.model.matchup;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores state of a Wvw matchup.
 * @see WvwMatchupSide
 */
@Data
public class WvwMatchupReport {

    /**
     * Tier of the matchup.
     */
    private int tier;

    /**
     * List with the 3 sides sorted by victory points.
     */
    private final List<WvwMatchupSide> sides;

    public WvwMatchupReport(List<WvwMatchupSide> sides, int tier) {
        this.sides = sides.stream()
                .sorted(Comparator.comparing(WvwMatchupSide::getVictoryPoints))
                .collect(Collectors.toList());
        this.tier = tier;
    }

    public WvwMatchupSide getFirstPlace() {
       return sides.get(2);
    }

    public WvwMatchupSide getSecondPlace() {
       return sides.get(1);
    }

    public WvwMatchupSide getThirdPlace() {
       return sides.get(0);
    }

    /**
     * Given a world ID, returns the placement of this world in this matchup. Such as
     * 1 is it is leading the matchup, 2 if it is second place, and 3 if it's last.
     */
    public int getPlacingOfWorld(int worldId) {
        if(getFirstPlace().getWorldIds().contains(worldId)) {
            return 1;
        } else if(getSecondPlace().getWorldIds().contains(worldId)) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Get side where selected world is on.
     */
    public WvwMatchupSide getSideOfWorld(int worldId) {
        for(var side: sides) {
            if(side.getWorldIds().contains(worldId)) {
                return side;
            }
        }
        throw new IllegalArgumentException("World with id " + worldId + " is not in the matchup!");
    }
}
