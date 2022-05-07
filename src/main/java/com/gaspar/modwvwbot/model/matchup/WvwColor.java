package com.gaspar.modwvwbot.model.matchup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Possible Wvw team colors.
 */
@RequiredArgsConstructor
public enum WvwColor {

    red("red_circle"),

    blue("blue_circle"),

    green("green_circle");

    /**
     * Name of default emote that represents this color.
     */
    @Getter
    private final String emoteName;
}
