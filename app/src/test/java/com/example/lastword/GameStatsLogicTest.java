package com.example.lastword;

import com.example.lastword.data.GameStatsRepository;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameStatsLogicTest {

    @Test
    public void testCategoryNormalization() {
        assertEquals("ANIMALS", GameStatsRepository.normalizeCategory("animals"));
        assertEquals("ANIMALS", GameStatsRepository.normalizeCategory("ANIMALS"));
        assertEquals("HORROR_MOVIES", GameStatsRepository.normalizeCategory("horror movies"));
        assertEquals("HORROR_MOVIES", GameStatsRepository.normalizeCategory("HORROR_MOVIES"));
        assertEquals("HORROR_MOVIES", GameStatsRepository.normalizeCategory("movies"));
        assertEquals("CELEBRITIES", GameStatsRepository.normalizeCategory("celebrities"));
        assertEquals("OBJECTS", GameStatsRepository.normalizeCategory("objects")); // legacy retained
        assertEquals("ANIMALS", GameStatsRepository.normalizeCategory(null));
    }

    @Test
    public void testDifficultyNormalization() {
        assertEquals("EASY", GameStatsRepository.normalizeDifficulty("easy"));
        assertEquals("EASY", GameStatsRepository.normalizeDifficulty("EASY"));
        assertEquals("MEDIUM", GameStatsRepository.normalizeDifficulty("intermediate"));
        assertEquals("MEDIUM", GameStatsRepository.normalizeDifficulty("INTERMEDIATE"));
        assertEquals("MEDIUM", GameStatsRepository.normalizeDifficulty("medium"));
        assertEquals("HARD", GameStatsRepository.normalizeDifficulty("hard"));
        assertEquals("EASY", GameStatsRepository.normalizeDifficulty(null));
    }

    @Test
    public void testStreakCalculationLogic() {
        // Sequence of wins and losses: W, W, W, L, W, W, L, W
        boolean[] outcomes = {true, true, true, false, true, true, false, true};
        int currentStreak = 0;
        int highestStreak = 0;

        for (boolean won : outcomes) {
            if (won) {
                currentStreak++;
                if (currentStreak > highestStreak) {
                    highestStreak = currentStreak;
                }
            } else {
                currentStreak = 0;
            }
        }

        assertEquals(1, currentStreak);
        assertEquals(3, highestStreak);
    }

    @Test
    public void testPureLossResetsStreak() {
        boolean[] outcomes = {true, true, true, true, false};
        int currentStreak = 0;
        int highestStreak = 0;

        for (boolean won : outcomes) {
            if (won) {
                currentStreak++;
                if (currentStreak > highestStreak) {
                    highestStreak = currentStreak;
                }
            } else {
                currentStreak = 0;
            }
        }

        assertEquals(0, currentStreak);
        assertEquals(4, highestStreak);
    }
}
