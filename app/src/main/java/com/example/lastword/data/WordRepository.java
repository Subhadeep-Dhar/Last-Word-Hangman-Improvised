package com.example.lastword.data;

import java.util.Random;

public class WordRepository {
    private static final String[] ANIMALS = {
        "TIGER", "ELEPHANT", "LEOPARD", "CROCODILE", "WOLF", "PENGUIN", "RHINOCEROS", "GORILLA", "PANTHER", "CHEETAH", "HYENA", "VULTURE",
        "ZEBRA", "GIRAFFE", "KANGAROO", "LION", "BEAR", "HIPPOPOTAMUS", "BUFFALO", "MOOSE", "ALLIGATOR", "CHIMPANZEE", "ORANGUTAN", "BABOON",
        "OSTRICH", "FLAMINGO", "PEACOCK", "PELICAN", "DOLPHIN", "WHALE", "SHARK", "OCTOPUS", "SQUID", "JELLYFISH", "SEAHORSE", "WALRUS",
        "SEAL", "MANATEE", "TURTLE", "IGUANA", "CHAMELEON", "COBRA", "PYTHON", "ANACONDA", "RATTLESNAKE", "SCORPION", "TARANTULA", "FALCON",
        "EAGLE", "HAWK", "OWL", "RAVEN", "CROW", "VAMPIRE BAT", "KOMODO DRAGON", "SLOTH", "ARMADILLO", "PORCUPINE", "BADGER", "WOLVERINE"
    };

    private static final String[] OBJECTS = {
        "MIRROR", "LANTERN", "TELEPHONE", "CAMERA", "PENDULUM", "GUITAR", "COMPASS", "HOURGLASS", "TELESCOPE", "VIOLIN", "RAZOR", "COFFIN",
        "TYPEWRITER", "PHONOGRAPH", "CHANDELIER", "DAGGER", "SWORD", "SHIELD", "CROSSBOW", "GUILLOTINE", "NOOSE", "TOMBSTONE", "SKULL",
        "SKELETON", "GOBLET", "CHALICE", "CAULDRON", "BROOMSTICK", "WAND", "SPELLBOOK", "GRIMOIRE", "AMULET", "TALISMAN", "URN", "CASKET",
        "SARCOPHAGUS", "MANNEQUIN", "PUPPET", "DOLL", "MUSIC BOX", "CLOCK", "POCKET WATCH", "KEY", "PADLOCK", "CHAIN", "SHACKLE", "SCALPEL",
        "SYRINGE", "MICROSCOPE", "GLOBE", "MAP", "PAINTING", "PORTRAIT", "STATUE", "CANDELABRA", "UMBRELLA", "WALKING STICK", "MONOCLE"
    };

    private static final String[] CELEBRITIES = {
        "EINSTEIN", "CHAPLIN", "PRESLEY", "MONROE", "SHAKESPEARE", "NEWTON", "DA VINCI", "MOZART", "PICASSO", "BEETHOVEN", "HITCHCOCK", "POE",
        "LINCOLN", "WASHINGTON", "CHURCHILL", "GANDHI", "MANDELA", "KING", "CLEOPATRA", "CAESAR", "ALEXANDER", "NAPOLEON", "JOAN OF ARC",
        "CURIE", "TESLA", "EDISON", "BELL", "WRIGHT", "FORD", "DISNEY", "HEMINGWAY", "TWAIN", "DICKENS", "AUSTEN", "BRONTE", "SHELLEY",
        "STOKER", "LOVECRAFT", "KING", "SPIELBERG", "KUBRICK", "TARANTINO", "SCORSESE", "COPPOLA", "BRANDO", "DE NIRO", "PACINO", "NICHOLSON",
        "STREEP", "HEPBURN", "BOGART", "WAYNE", "EASTWOOD", "LEE", "JACKSON", "MADONNA", "LENNON", "MCCARTNEY", "DYLAN", "BOWIE", "MERCURY"
    };

    public static String getRandomWord(String category) {
        String[] list;
        switch (category.toUpperCase()) {
            case "OBJECTS":
                list = OBJECTS;
                break;
            case "CELEBRITIES":
                list = CELEBRITIES;
                break;
            case "ANIMALS":
            default:
                list = ANIMALS;
                break;
        }
        Random random = new Random();
        return list[random.nextInt(list.length)];
    }
}
