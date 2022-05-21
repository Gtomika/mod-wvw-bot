package com.gaspar.modwvwbot.model.matchup;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * One of the 3 sides in a Wvw matchup (like "red").
 * @see WvwMatchupReport
 * @see WvwColor
 */
@Data
@Builder
public class WvwMatchupSide {

    /**
     * Color of this side.
     */
    private WvwColor color;

    /**
     * Names of the worlds on this side. The first one is always
     * the main server, the rest are the paired servers.
     */
    private List<String> worldNames;

    /**
     * Ids of the worlds on this side. The first one is always
     * the main server, the rest are the paired servers.
     */
    private List<Integer> worldIds;

    /**
     * Amount of victory points this side has.
     */
    private int victoryPoints;

    /**
     * Kill count of this side.
     */
    private int killCount;

    /**
     * Death count of this side.
     */
    private int deathCount;

    /**
     * K/D ratio.
     */
    private float killDeathRatio;

    /**
     * Checks if this side has the specified world.
     */
    public boolean containsWorld(int worldId) {
        return worldIds.contains(worldId);
    }
}
