package com.example.lastword;

import com.example.lastword.data.WordRepository;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class DailyChallengeLogicTest {

    private String[] resolveChallenge(String dateStr) {
        long seed = dateStr.hashCode();
        Random rand = new Random(seed);
        String[] categories = {
                WordRepository.CATEGORY_ANIMALS,
                WordRepository.CATEGORY_HORROR_MOVIES,
                WordRepository.CATEGORY_CELEBRITIES
        };
        String selectedCat = categories[Math.abs(rand.nextInt()) % categories.length];
        String[] words = WordRepository.getWordList(selectedCat);
        String selectedWord = words[Math.abs(rand.nextInt()) % words.length];
        return new String[]{selectedCat, selectedWord};
    }

    @Test
    public void testDailyChallengeConsistencySameDate() {
        String dateStr = "2026-10-14";
        String[] first = resolveChallenge(dateStr);
        for (int i = 0; i < 50; i++) {
            String[] next = resolveChallenge(dateStr);
            assertEquals("Category must be identical for same date", first[0], next[0]);
            assertEquals("Word must be identical for same date", first[1], next[1]);
        }
    }

    @Test
    public void testDailyChallengeDifferentDates() {
        String[] day1 = resolveChallenge("2026-10-14");
        String[] day2 = resolveChallenge("2026-10-15");
        String[] day3 = resolveChallenge("2026-10-16");

        // Verify all yield valid categories and words
        assertNotNull(day1[0]);
        assertNotNull(day1[1]);
        assertNotNull(day2[0]);
        assertNotNull(day2[1]);
        assertNotNull(day3[0]);
        assertNotNull(day3[1]);
    }
}
