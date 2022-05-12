package com.gaspar.modwvwbot.model.gw2api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Gw2 API response when fetching account details.
 */
@Data
@JsonIgnoreProperties //API response has a lot more stuff, we only care about a few fields
public class Gw2Account {

    @JsonProperty("name")
    private String accountName;

    /**
     * Wvw level of the player. This is possible null, because it requires
     * the API key permission 'progression'.
     */
    @JsonProperty("wvw_rank")
    @Nullable
    private Integer wvwLevel;

    @JsonProperty("world")
    private Integer worldId;

    @JsonProperty("commander")
    private Boolean hasCommanderTag;

}
