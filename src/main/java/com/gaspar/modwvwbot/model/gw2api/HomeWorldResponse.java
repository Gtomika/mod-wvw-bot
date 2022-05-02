package com.gaspar.modwvwbot.model.gw2api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response of GW2 API when querying /worlds.
 */
@Data
@AllArgsConstructor
public class HomeWorldResponse {

    private Integer id;

    private String name;

    private Population population;

}
