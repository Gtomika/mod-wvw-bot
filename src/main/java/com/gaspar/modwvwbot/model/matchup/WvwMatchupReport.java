package com.gaspar.modwvwbot.model.matchup;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores state of a Wvw matchup.
 * @see WvwMatchupSide
 */
@Data
public class WvwMatchupReport {

    /**
     * List with the 3 sides sorted by victory points.
     */
    private final List<WvwMatchupSide> sides;

    /**
     * When the matchup ends.
     */
    private final LocalDateTime endsAt;

    public WvwMatchupReport(List<WvwMatchupSide> sides, LocalDateTime endsAt) {
        this.sides = sides.stream()
                .sorted(Comparator.comparing(WvwMatchupSide::getVictoryPoints))
                .collect(Collectors.toList());
        this.endsAt = endsAt;
    }

    public WvwMatchupSide getFirstPlace() {
       return sides.get(2);
    }

    public WvwMatchupSide getSecondPlace() {
       return sides.get(1);
    }

    public WvwMatchupSide getThirdPlace() {
       return sides.get(0);
    }

}
