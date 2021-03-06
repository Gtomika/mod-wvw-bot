package com.gaspar.modwvwbot.model.gw2api;

import com.gaspar.modwvwbot.model.Population;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response of GW2 API when querying /worlds.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeWorldResponse {

    private Integer id;

    private String name;

    private Population population;

}
