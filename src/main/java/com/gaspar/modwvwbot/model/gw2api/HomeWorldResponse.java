package com.gaspar.modwvwbot.model.gw2api;

import lombok.Data;

/**
 * Response of GW2 API when querying /worlds.
 */
@Data
public class HomeWorldResponse {

    private Integer id;

    private String name;

    private Population population;

}
