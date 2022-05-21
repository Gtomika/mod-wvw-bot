package com.gaspar.modwvwbot.model.matchup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Gw2 APIs response when querying /wvw with one world ID. End time is not a field because
 * API returns the reset for US servers and not EU!
 */
@Data
@JsonIgnoreProperties
public class WvwMatchupResponse {

    /**
     * Matchup ID, which is in the format of R-T, where R is region number (2 for EU, 1 for US)
     * and T is matchup tier (should be 1-5).
     */
    @JsonProperty("id")
    private String matchId;

    /**
     * IDs of the worlds participating in the matchup.
     * @see AllWorldsResponse
     */
    @JsonProperty("all_worlds")
    private AllWorldsResponse allWorlds;

    /**
     * Kills by each side.
     */
    private MatchupStatisticResponse kills;

    /**
     * Deaths by each side.
     */
    private MatchupStatisticResponse deaths;

    /**
     * Victory points by each side.
     */
    @JsonProperty("victory_points")
    private MatchupStatisticResponse victoryPoints;
}
