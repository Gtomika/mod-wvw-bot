package com.gaspar.modwvwbot.model.gw2api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GW2 API response of the contents
 * of a single item slot in the inventory or
 * bank of the user.
 */
@Data
@JsonIgnoreProperties //can have other stuff such as binding, but that is not important
public class ItemResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("count")
    private Integer amount;

}
