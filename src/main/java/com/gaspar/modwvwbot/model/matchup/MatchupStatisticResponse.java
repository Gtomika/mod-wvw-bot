package com.gaspar.modwvwbot.model.matchup;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Some statistic form the WvW matchup API response, such as "kills" or "victory_points".
 */
@Data
@NoArgsConstructor
public class MatchupStatisticResponse {

    /**
     * Statistic of red side.
     */
    private int red;

    /**
     * Statistic of blue side.
     */
    private int blue;

    /**
     * Statistic of green side.
     */
    private int green;

    /**
     * Get statistic of one side.
     * @param color Color of the side.
     */
    public int getByColor(WvwColor color) {
        switch (color) {
            case red:
                return this.red;
            case blue:
                return this.blue;
            case green:
                return this.green;
            default:
                throw new IllegalArgumentException("Unrecognized color: " + color);
        }
    }

}
