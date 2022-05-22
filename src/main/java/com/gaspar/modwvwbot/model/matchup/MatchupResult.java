package com.gaspar.modwvwbot.model.matchup;

import lombok.Getter;

/**
 * Possibilities that can happen to a world after the
 * matchup finishes.
 */
public enum MatchupResult {

    /**
     * Advances to the next tier.
     */
    ADVANCES(-1),

    /**
     * Stays in it's current tier.
     */
    STAYS(0),

    /**
     * Drops down to the tier below current tier.
     */
    DROPS_DOWN(1);

    @Getter
    private final int offset;

    MatchupResult(int offset) {
        this.offset = offset;
    }
}
