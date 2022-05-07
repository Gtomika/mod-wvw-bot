package com.gaspar.modwvwbot.model.matchup;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response structure of "all_worlds" field in matchup response. The main world
 * is always the last one in the list.
 */
@Data
@NoArgsConstructor
public class AllWorldsResponse {

    private List<Integer> red;

    private List<Integer> blue;

    private List<Integer> green;

    /**
     * Get world names of one side.
     * @param color Color of the side.
     */
    public List<Integer> getByColor(WvwColor color) {
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
