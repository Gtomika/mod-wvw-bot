package com.gaspar.modwvwbot.model.gw2api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Gw2 API response containing the items in
 * one of the bags of a character.
 * @see ItemResponse
 */
@Data
@JsonIgnoreProperties
public class BagResponse {

    @JsonProperty("inventory")
    List<ItemResponse> items;

}
