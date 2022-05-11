package com.gaspar.modwvwbot.misc;

import com.gaspar.modwvwbot.model.jsonclean.Target;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for processing Wvw logs.
 */
public class WvwLogUtils {

    /**
     * Regex to match enemy player names in the log. For example:
     *  - Scourge pl1-5432
     *  - Firebrand pl24-4685
     */
    private static final Pattern targetRegex = Pattern.compile("\\w+ pl\\d+-(\\d){4}");

    /**
     * Collect all enemy players that appear in the log.
     * @param jsonLog The log in JSON format before any cleaning.
     * @return All unique targets.
     * @see Target
     */
    public static List<Target> extractTargets(String jsonLog) {
        var enemyNames = new HashSet<String>();
        Matcher matcher = targetRegex.matcher(jsonLog);
        while (matcher.find()) {
            String player = matcher.group();
            enemyNames.add(player);
        }
        var enemies = new ArrayList<Target>();
        int id = 1;
        for(String name: enemyNames) {
            enemies.add(new Target(name, true, id++));
        }
        return enemies;
    }

}
