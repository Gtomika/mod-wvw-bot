package com.gaspar.modwvwbot.model.gw2api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Gw2 API response containing all the items in
 * the inventory of a character, separated in bags.
 * @see BagResponse
 */
@Data
@JsonIgnoreProperties
public class InventoryResponse {

    @JsonProperty("bags")
    List<BagResponse> bags;

}
