package com.gaspar.modwvwbot.model.gw2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The Gw2 API responds with an array of such objects when
 * the account value is queried.
 */
@Data
public class CurrencyResponse {

    /**
     * Currency ID.
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * How many of the currency is there in the wallet.
     */
    @JsonProperty("value")
    private Integer amount;

}
