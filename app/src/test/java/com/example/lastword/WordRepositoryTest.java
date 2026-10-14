package com.example.lastword;

import com.example.lastword.data.WordRepository;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class WordRepositoryTest {

    @Test
    public void testWordListsNotEmpty() {
        String[] animals = WordRepository.getWordList(WordRepository.CATEGORY_ANIMALS);
        String[] horror = WordRepository.getWordList(WordRepository.CATEGORY_HORROR_MOVIES);
        String[] celebs = WordRepository.getWordList(WordRepository.CATEGORY_CELEBRITIES);

        assertNotNull(animals);
        assertTrue(animals.length > 0);

        assertNotNull(horror);
        assertTrue(horror.length > 0);

        assertNotNull(celebs);
        assertTrue(celebs.length > 0);
    }

    @Test
    public void testNoSharedWordsBetweenDifficulties() {
        String[] categories = {
                WordRepository.CATEGORY_ANIMALS,
                WordRepository.CATEGORY_HORROR_MOVIES,
                WordRepository.CATEGORY_CELEBRITIES
        };

        for (String cat : categories) {
            String[] easyWords = WordRepository.getWordList(cat, "EASY");
            String[] mediumWords = WordRepository.getWordList(cat, "MEDIUM");
            String[] hardWords = WordRepository.getWordList(cat, "HARD");

            assertNotNull(easyWords);
            assertNotNull(mediumWords);
            assertNotNull(hardWords);

            Set<String> easySet = new HashSet<>(Arrays.asList(easyWords));
            Set<String> mediumSet = new HashSet<>(Arrays.asList(mediumWords));
            Set<String> hardSet = new HashSet<>(Arrays.asList(hardWords));

            // Verify Easy vs Medium has 0 shared words
            Set<String> easyMedOverlap = new HashSet<>(easySet);
            easyMedOverlap.retainAll(mediumSet);
            assertTrue("Easy and Medium must not share words for category " + cat + " (shared: " + easyMedOverlap + ")",
                    easyMedOverlap.isEmpty());

            // Verify Easy vs Hard has 0 shared words
            Set<String> easyHardOverlap = new HashSet<>(easySet);
            easyHardOverlap.retainAll(hardSet);
            assertTrue("Easy and Hard must not share words for category " + cat + " (shared: " + easyHardOverlap + ")",
                    easyHardOverlap.isEmpty());

            // Verify Medium vs Hard has 0 shared words
            Set<String> medHardOverlap = new HashSet<>(mediumSet);
            medHardOverlap.retainAll(hardSet);
            assertTrue("Medium and Hard must not share words for category " + cat + " (shared: " + medHardOverlap + ")",
                    medHardOverlap.isEmpty());
        }
    }

    @Test
    public void testGetRandomWordWithDifficulty() {
        String[] categories = {
                WordRepository.CATEGORY_ANIMALS,
                WordRepository.CATEGORY_HORROR_MOVIES,
                WordRepository.CATEGORY_CELEBRITIES
        };
        String[] difficulties = {"EASY", "MEDIUM", "HARD"};

        for (String cat : categories) {
            for (String diff : difficulties) {
                Set<String> expectedWords = new HashSet<>(Arrays.asList(WordRepository.getWordList(cat, diff)));
                for (int i = 0; i < 20; i++) {
                    String word = WordRepository.getRandomWord(cat, diff);
                    assertNotNull(word);
                    assertTrue("Word " + word + " must belong to difficulty " + diff + " of category " + cat,
                            expectedWords.contains(word));
                }
            }
        }
    }

    @Test
    public void testHintsDefined() {
        String elephantHint = WordRepository.getHint(WordRepository.CATEGORY_ANIMALS, "ELEPHANT");
        assertNotNull(elephantHint);
        assertTrue(elephantHint.toLowerCase().contains("trunk"));

        String conjuringHint = WordRepository.getHint(WordRepository.CATEGORY_HORROR_MOVIES, "THE CONJURING");
        assertNotNull(conjuringHint);
        assertTrue(conjuringHint.toLowerCase().contains("paranormal") || conjuringHint.toLowerCase().contains("supernatural"));

        String kingHint = WordRepository.getHint(WordRepository.CATEGORY_CELEBRITIES, "STEPHEN KING");
        assertNotNull(kingHint);
        assertTrue(kingHint.toLowerCase().contains("horror") || kingHint.toLowerCase().contains("author"));
    }

    @Test
    public void testHintSafeFallback() {
        String fallbackAnimal = WordRepository.getHint(WordRepository.CATEGORY_ANIMALS, "NON_EXISTENT_CREATURE_XYZ");
        assertNotNull(fallbackAnimal);
        assertFalse(fallbackAnimal.trim().isEmpty());

        String fallbackNull = WordRepository.getHint(WordRepository.CATEGORY_HORROR_MOVIES, null);
        assertNotNull(fallbackNull);
        assertFalse(fallbackNull.trim().isEmpty());
    }
}
