package com.gaspar.modwvwbot.model.gw2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Gw2 API response of a Wvw rank, such as "Bronze invader".
 */
@Data
public class WvwRank {

    private int id;

    /**
     * Name, such as "Diamond Raider".
     */
    private String title;

    /**
     * Minimum level of this rank. Between this level and the next ranks
     * minimum level the player has this rank.
     */
    @JsonProperty("min_rank")
    private int minRank;

}
