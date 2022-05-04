package com.gaspar.modwvwbot.model.gw2api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties //API response has a lot more stuff, we only care about a few fields
public class Gw2User {

    @JsonProperty("name")
    private String accountName;

    @JsonProperty("wvw_rank")
    private Integer wvwLevel;

}
