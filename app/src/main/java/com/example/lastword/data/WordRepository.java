package com.example.lastword.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WordRepository {
    public static final String CATEGORY_ANIMALS = "ANIMALS";
    public static final String CATEGORY_HORROR_MOVIES = "HORROR_MOVIES";
    public static final String CATEGORY_CELEBRITIES = "CELEBRITIES";

    // --- ANIMALS: Partitioned by difficulty (No shared words between Easy, Medium, Hard) ---
    public static final String[] ANIMALS_EASY = {
        "LION", "TIGER", "BEAR", "WOLF", "ZEBRA", "EAGLE", "SHARK", "WHALE", "CROW", "OWL",
        "PENGUIN", "TURTLE", "DOLPHIN", "FALCON", "SEAL", "BADGER", "HAWK", "RAVEN", "SLOTH", "MOOSE"
    };

    public static final String[] ANIMALS_MEDIUM = {
        "LEOPARD", "GIRAFFE", "PANTHER", "CHEETAH", "GORILLA", "KANGAROO", "OCTOPUS", "PELICAN",
        "FLAMINGO", "PEACOCK", "CHAMELEON", "SCORPION", "VULTURE", "HYENA", "WALRUS", "BUFFALO",
        "SQUID", "JELLYFISH", "SEAHORSE", "IGUANA"
    };

    public static final String[] ANIMALS_HARD = {
        "RHINOCEROS", "HIPPOPOTAMUS", "CROCODILE", "ALLIGATOR", "CHIMPANZEE", "ORANGUTAN",
        "TARANTULA", "KOMODO DRAGON", "VAMPIRE BAT", "ARMADILLO", "PORCUPINE", "WOLVERINE",
        "RATTLESNAKE", "ANACONDA", "BABOON", "MANATEE", "COBRA", "PYTHON", "OSTRICH", "ELEPHANT"
    };

    // --- HORROR MOVIES: Partitioned by difficulty (No shared words between Easy, Medium, Hard) ---
    public static final String[] HORROR_MOVIES_EASY = {
        "SAW", "JAWS", "IT", "ALIEN", "SCREAM", "PSYCHO", "CARRIE", "US",
        "SPLIT", "REC", "THE FLY", "MISERY", "THE RING", "THE OMEN", "THE BIRDS", "HOSTEL"
    };

    public static final String[] HORROR_MOVIES_MEDIUM = {
        "HALLOWEEN", "THE SHINING", "THE EXORCIST", "HEREDITARY", "THE THING", "THE CONJURING",
        "A QUIET PLACE", "GET OUT", "SINISTER", "INSIDIOUS", "THE BABADOOK", "EVIL DEAD",
        "HELLRAISER", "CANDYMAN", "CHILDS PLAY", "THE WITCH"
    };

    public static final String[] HORROR_MOVIES_HARD = {
        "PARANORMAL ACTIVITY", "NIGHTMARE ON ELM STREET", "FRIDAY THE 13TH", "TEXAS CHAINSAW MASSACRE",
        "BLAIR WITCH", "THE STRANGERS", "JACOBS LADDER", "AUDITION", "THE DESCENT", "DRACULA",
        "FRANKENSTEIN", "THE MUMMY", "THE INVISIBLE MAN", "ROSEMARYS BABY", "POLTERGEIST", "SUSPIRIA", "MIDSOMMAR"
    };

    // --- CELEBRITIES: Partitioned by difficulty (No shared words between Easy, Medium, Hard) ---
    public static final String[] CELEBRITIES_EASY = {
        "PRESLEY", "CHAPLIN", "MONROE", "EINSTEIN", "MOZART", "NEWTON", "PICASSO", "MADONNA",
        "TESLA", "EDISON", "DISNEY", "BOWIE", "MERCURY", "DYLAN", "POE", "CAESAR", "FORD", "BELL"
    };

    public static final String[] CELEBRITIES_MEDIUM = {
        "SHAKESPEARE", "BEETHOVEN", "HITCHCOCK", "WASHINGTON", "CHURCHILL", "GANDHI", "MANDELA",
        "STEPHEN KING", "CLEOPATRA", "NAPOLEON", "CURIE", "HEMINGWAY", "TWAIN", "DICKENS",
        "BRANDO", "DE NIRO", "AUSTEN", "BRONTE"
    };

    public static final String[] CELEBRITIES_HARD = {
        "DA VINCI", "JOAN OF ARC", "ALEXANDER", "LINCOLN", "SPIELBERG", "KUBRICK", "TARANTINO",
        "SCORSESE", "COPPOLA", "PACINO", "NICHOLSON", "STREEP", "HEPBURN", "BOGART", "WAYNE",
        "EASTWOOD", "LEE", "JACKSON"
    };

    private static final Map<String, String> HINTS = new HashMap<>();

    static {
        // --- ANIMALS HINTS ---
        HINTS.put("LION", "The maned king of the savannah pride.");
        HINTS.put("TIGER", "A large striped feline apex predator.");
        HINTS.put("BEAR", "A heavy mammal with shaggy fur and sharp claws.");
        HINTS.put("WOLF", "A pack-hunting canid associated with lunar howls.");
        HINTS.put("ZEBRA", "An African equine distinguished by black and white stripes.");
        HINTS.put("EAGLE", "A majestic raptor with keen eyesight and razor-sharp talons.");
        HINTS.put("SHARK", "A cartilaginous marine apex predator with rows of serrated teeth.");
        HINTS.put("WHALE", "A colossal oceanic mammal that breaches the surface to breathe.");
        HINTS.put("CROW", "A clever black corvid known for complex problem-solving.");
        HINTS.put("OWL", "A nocturnal hunter of the darkness with silent flight and wide eyes.");
        HINTS.put("PENGUIN", "A flightless bird thriving in Antarctic ice.");
        HINTS.put("TURTLE", "A hard-shelled reptile found in oceans and freshwater.");
        HINTS.put("DOLPHIN", "An agile, highly intelligent marine mammal that uses echolocation.");
        HINTS.put("FALCON", "A bird of prey capable of supersonic dives through the skies.");
        HINTS.put("SEAL", "A fin-footed ocean mammal adapted for cold polar waters.");
        HINTS.put("BADGER", "A fiercely stubborn subterranean predator with strong digging claws.");
        HINTS.put("HAWK", "A medium-sized bird of prey known for sharp vision and sudden attacks.");
        HINTS.put("RAVEN", "An ominous black bird associated with mythology and dark omens.");
        HINTS.put("SLOTH", "An exceptionally slow tree-dweller that spends life hanging upside down.");
        HINTS.put("MOOSE", "The largest species in the deer family with palmate antlers.");

        HINTS.put("LEOPARD", "A spotted big cat known for climbing trees.");
        HINTS.put("GIRAFFE", "The tallest living terrestrial animal with an elongated neck.");
        HINTS.put("PANTHER", "A stealthy black-coated leopard or jaguar.");
        HINTS.put("CHEETAH", "The fastest land animal on the planet.");
        HINTS.put("GORILLA", "A powerful ground-dwelling great ape of African forests.");
        HINTS.put("KANGAROO", "A jumping marsupial carrying its young in a pouch.");
        HINTS.put("OCTOPUS", "An eight-armed cephalopod capable of camouflage and ink emission.");
        HINTS.put("PELICAN", "A large water bird with a throat pouch used for catching fish.");
        HINTS.put("FLAMINGO", "A wading bird known for vivid pink feathers and standing on one leg.");
        HINTS.put("PEACOCK", "A male peafowl renowned for its magnificent iridescent tail fan.");
        HINTS.put("CHAMELEON", "A color-shifting lizard with independent 360-degree eyes.");
        HINTS.put("SCORPION", "An eight-legged arachnid with grasping claws and a venomous arched tail.");
        HINTS.put("VULTURE", "A bird of prey that circles dying prey and feeds on carrion.");
        HINTS.put("HYENA", "A scavening predator known for eerie laughing vocalizations.");
        HINTS.put("WALRUS", "A massive arctic pinniped recognized by long tusks and whiskers.");
        HINTS.put("BUFFALO", "A heavily built horned bovine.");
        HINTS.put("SQUID", "A fast-swimming cephalopod with eight arms and two long tentacles.");
        HINTS.put("JELLYFISH", "A translucent bell-shaped sea creature with stinging tentacles.");
        HINTS.put("SEAHORSE", "A tiny equine-headed marine fish where the male carries unborn young.");
        HINTS.put("IGUANA", "A large herbivorous lizard with a spiny crest along its back.");

        HINTS.put("RHINOCEROS", "A massive herbivore with a horned snout.");
        HINTS.put("HIPPOPOTAMUS", "A large, aggressive semi-aquatic river beast.");
        HINTS.put("CROCODILE", "A stealthy armored aquatic reptile with powerful jaws.");
        HINTS.put("ALLIGATOR", "A broad-snouted freshwater crocodilian native to swamps.");
        HINTS.put("CHIMPANZEE", "A highly intelligent primate closely related to humans.");
        HINTS.put("ORANGUTAN", "A reddish-brown tree-dwelling ape native to rainforests.");
        HINTS.put("TARANTULA", "A large, hairy arachnid found in burrows and dark corners.");
        HINTS.put("KOMODO DRAGON", "The heaviest living lizard, armed with toxic bite and sharp teeth.");
        HINTS.put("VAMPIRE BAT", "A winged nocturnal mammal that feeds exclusively on blood.");
        HINTS.put("ARMADILLO", "A leathery-plated burrowing mammal that curls into a defensive ball.");
        HINTS.put("PORCUPINE", "A rodent covered in sharp barbed defensive quills.");
        HINTS.put("WOLVERINE", "A solitary northern forest carnivore known for ferocity far beyond its size.");
        HINTS.put("RATTLESNAKE", "A pit viper that warns intruders with a buzzing tail segment.");
        HINTS.put("ANACONDA", "A giant South American water boa, one of the heaviest snakes on Earth.");
        HINTS.put("BABOON", "A large ground-dwelling Old World monkey with a dog-like muzzle.");
        HINTS.put("MANATEE", "A gentle, slow-moving aquatic herbivore known as a sea cow.");
        HINTS.put("COBRA", "A venomous elapid snake that flares its threatening neck hood.");
        HINTS.put("PYTHON", "A massive non-venomous constrictor snake that squeezes its prey.");
        HINTS.put("OSTRICH", "The largest and fastest running bird in the world.");
        HINTS.put("ELEPHANT", "A huge mammal with a trunk.");

        // --- HORROR MOVIES HINTS ---
        HINTS.put("SAW", "A sinister mastermind forces victims to play sadistic survival games.");
        HINTS.put("JAWS", "A monstrous great white shark terrorizes a summer beach town.");
        HINTS.put("IT", "Stephen King's entity Pennywise the Dancing Clown terrorizes children in Derry.");
        HINTS.put("ALIEN", "Ridley Scott's sci-fi nightmare: 'In space no one can hear you scream.'");
        HINTS.put("SCREAM", "Wes Craven's meta-slasher asking: 'What's your favorite scary movie?'");
        HINTS.put("PSYCHO", "Alfred Hitchcock's masterpiece set at a lonely motel with Norman Bates.");
        HINTS.put("CARRIE", "A bullied telekinetic high school girl unleashes fury at senior prom.");
        HINTS.put("US", "A family is confronted by a terrifying group of red-suited doppelgängers.");
        HINTS.put("SPLIT", "A man with 23 distinct personalities awakens the superhuman 24th 'Beast'.");
        HINTS.put("REC", "Spanish found-footage horror in an apartment quarantine overrun by an infection.");
        HINTS.put("THE FLY", "David Cronenberg's body-horror transformation of an ambitious scientist.");
        HINTS.put("MISERY", "A severely obsessed fan holds her favorite novelist captive in a remote cabin.");
        HINTS.put("THE RING", "Watch a cursed videotape and receive a phone call giving seven days to live.");
        HINTS.put("THE OMEN", "An ambassador realizes his adopted son Damien is the Antichrist.");
        HINTS.put("THE BIRDS", "Flocks of birds begin violent and unexplained attacks on humans.");
        HINTS.put("HOSTEL", "Backpackers in Eastern Europe fall victim to an elite torture syndicate.");

        HINTS.put("HALLOWEEN", "John Carpenter's slasher classic featuring the masked killer Michael Myers.");
        HINTS.put("THE SHINING", "Stanley Kubrick's adaptation where Jack Torrance unravels at the Overlook Hotel.");
        HINTS.put("THE EXORCIST", "William Friedkin's chilling film about the demonic possession of a young girl.");
        HINTS.put("HEREDITARY", "Ari Aster's haunting psychological dread of grief and ancestral demonic pacts.");
        HINTS.put("THE THING", "John Carpenter's Antarctic paranoia about a shapeshifting alien entity.");
        HINTS.put("THE CONJURING", "A supernatural horror film involving paranormal investigators.");
        HINTS.put("A QUIET PLACE", "Blind monsters with hyper-sensitive hearing hunt the silent survivors.");
        HINTS.put("GET OUT", "Jordan Peele's psychological thriller about a sinister hypnotic family trap.");
        HINTS.put("SINISTER", "A true-crime writer discovers snuff 8mm reels linked to the demon Bughuul.");
        HINTS.put("INSIDIOUS", "Paranormal entity traps a boy's soul in a dark realm called 'The Further'.");
        HINTS.put("THE BABADOOK", "A children's pop-up book brings a shadowy monster to life.");
        HINTS.put("EVIL DEAD", "Sam Raimi's cabin-in-the-woods awakening of Kandarian demons via the Necronomicon.");
        HINTS.put("HELLRAISER", "Solving a puzzle box summons the sadistic Cenobite Pinhead.");
        HINTS.put("CANDYMAN", "Say his name five times in the mirror to summon the hook-handed spirit.");
        HINTS.put("CHILDS PLAY", "A serial killer's soul possesses a red-haired Good Guy doll named Chucky.");
        HINTS.put("THE WITCH", "A Puritan 1630s family exiled into the woods faces darkness.");

        HINTS.put("PARANORMAL ACTIVITY", "Found-footage recording nighttime disturbances inside a suburban home.");
        HINTS.put("NIGHTMARE ON ELM STREET", "Freddy Krueger stalks and slashes teenagers within their dreams.");
        HINTS.put("FRIDAY THE 13TH", "Camp Crystal Lake stalked by a hockey-masked vengeance.");
        HINTS.put("TEXAS CHAINSAW MASSACRE", "Leatherface and a cannibalistic family prey on travelers.");
        HINTS.put("BLAIR WITCH", "Found footage of student filmmakers lost in the woods of Burkittsville.");
        HINTS.put("THE STRANGERS", "A couple trapped in a vacation home terrorized by masked intruders.");
        HINTS.put("JACOBS LADDER", "A Vietnam veteran suffers terrifying disorienting hallucinations.");
        HINTS.put("AUDITION", "A widower holds mock auditions for a new wife with horrifying consequences.");
        HINTS.put("THE DESCENT", "Spelunkers explore an unmapped cave system inhabited by blind crawlers.");
        HINTS.put("DRACULA", "Bram Stoker's iconic vampire count journeying from Transylvania to London.");
        HINTS.put("FRANKENSTEIN", "Mary Shelley's reanimated creature built from stolen corpse parts.");
        HINTS.put("THE MUMMY", "An ancient cursed high priest awakened from his sealed tomb.");
        HINTS.put("THE INVISIBLE MAN", "A scientist discovers optical invisibility, descending into megalomania.");
        HINTS.put("ROSEMARYS BABY", "Roman Polanski's psychological dread of a sinister apartment cult pregnancy.");
        HINTS.put("POLTERGEIST", "A suburban home besieged by spirits through the television screen.");
        HINTS.put("SUSPIRIA", "A sinister European ballet academy secretly run by a coven of witches.");
        HINTS.put("MIDSOMMAR", "A Swedish pagan cult festival turns into daylight psychedelic horror.");

        // --- CELEBRITIES HINTS ---
        HINTS.put("PRESLEY", "The legendary 'King of Rock and Roll' who recorded at Sun Studio.");
        HINTS.put("CHAPLIN", "Iconic silent film comedic legend known for 'The Tramp' persona.");
        HINTS.put("MONROE", "Hollywood blonde bombshell and star of 'Some Like It Hot'.");
        HINTS.put("EINSTEIN", "Physicist who formulated the theory of relativity and E=mc².");
        HINTS.put("MOZART", "Child prodigy Austrian composer of classical symphonies and operas.");
        HINTS.put("NEWTON", "Mathematician who formulated universal gravitation and the laws of motion.");
        HINTS.put("PICASSO", "Spanish pioneer of the Cubist modern art movement.");
        HINTS.put("MADONNA", "The boundary-pushing 'Queen of Pop' and multi-decade music icon.");
        HINTS.put("TESLA", "Inventive genius of alternating current (AC) electricity and wireless energy.");
        HINTS.put("EDISON", "Prolific American inventor of the phonograph and practical incandescent light bulb.");
        HINTS.put("DISNEY", "Pioneering animator and entertainment mogul who created Mickey Mouse.");
        HINTS.put("BOWIE", "Glam rock innovator and chameleon behind Ziggy Stardust.");
        HINTS.put("MERCURY", "Charismatic four-octave Queen frontman who sang 'Bohemian Rhapsody'.");
        HINTS.put("DYLAN", "Nobel Prize-winning poetic folk songwriter of 'Blowin' in the Wind'.");
        HINTS.put("POE", "Macabre master of gothic literature, author of 'The Raven' and 'The Tell-Tale Heart'.");
        HINTS.put("CAESAR", "Roman general and statesman whose dictatorship reshaped the Roman Republic.");
        HINTS.put("FORD", "Industrialist founder of Ford Motor Company and assembly-line pioneer.");
        HINTS.put("BELL", "Inventor and patent holder of the first practical telephone.");

        HINTS.put("SHAKESPEARE", "The Bard of Avon who penned Hamlet, Macbeth, and Romeo & Juliet.");
        HINTS.put("BEETHOVEN", "German composer who composed groundbreaking masterworks even while deaf.");
        HINTS.put("HITCHCOCK", "The 'Master of Suspense' who directed Vertigo, Rear Window, and Psycho.");
        HINTS.put("WASHINGTON", "The commander-in-chief of the Continental Army and first US President.");
        HINTS.put("CHURCHILL", "British Prime Minister whose iron will led the nation through WWII.");
        HINTS.put("GANDHI", "Leader of India's independence movement through nonviolent civil resistance.");
        HINTS.put("MANDELA", "South African anti-apartheid revolutionary and first black president.");
        HINTS.put("STEPHEN KING", "A bestselling author known for supernatural horror.");
        HINTS.put("CLEOPATRA", "The last active ruler of the Ptolemaic Kingdom of Egypt.");
        HINTS.put("NAPOLEON", "French military leader who conquered much of Europe in the early 19th century.");
        HINTS.put("CURIE", "Pioneering physicist and chemist who conducted groundbreaking research on radioactivity.");
        HINTS.put("HEMINGWAY", "Nobel Prize-winning author of 'The Old Man and the Sea'.");
        HINTS.put("TWAIN", "American literary humorist and author of 'The Adventures of Tom Sawyer'.");
        HINTS.put("DICKENS", "Victorian novelist who wrote 'A Christmas Carol' and 'Great Expectations'.");
        HINTS.put("BRANDO", "Legendary method actor who portrayed Vito Corleone and Terry Malloy.");
        HINTS.put("DE NIRO", "Oscar-winning powerhouse actor known for Taxi Driver and Raging Bull.");
        HINTS.put("AUSTEN", "English novelist acclaimed for 'Pride and Prejudice' and 'Sense and Sensibility'.");
        HINTS.put("BRONTE", "Literary family of sisters behind 'Jane Eyre' and 'Wuthering Heights'.");

        HINTS.put("DA VINCI", "Renaissance polymath who painted the Mona Lisa and designed flying machines.");
        HINTS.put("JOAN OF ARC", "French peasant heroine and patron saint who led armies against England.");
        HINTS.put("ALEXANDER", "King of Macedonia who conquered an empire stretching to India.");
        HINTS.put("LINCOLN", "The 16th US President who preserved the Union and issued the Emancipation Proclamation.");
        HINTS.put("SPIELBERG", "Visionary film director of Jaws, E.T., and Jurassic Park.");
        HINTS.put("KUBRICK", "Meticulous director of 2001: A Space Odyssey and A Clockwork Orange.");
        HINTS.put("TARANTINO", "Director known for nonlinear storytelling, dialogue, and Pulp Fiction.");
        HINTS.put("SCORSESE", "Acclaimed director of Taxi Driver, Goodfellas, and Raging Bull.");
        HINTS.put("COPPOLA", "Cinematic titan behind The Godfather trilogy and Apocalypse Now.");
        HINTS.put("PACINO", "Electrifying actor acclaimed for Michael Corleone and Scarface.");
        HINTS.put("NICHOLSON", "Legendary actor celebrated for One Flew Over the Cuckoo's Nest and The Shining.");
        HINTS.put("STREEP", "Widely regarded as one of the greatest dramatic actresses in cinema history.");
        HINTS.put("HEPBURN", "Elegantly iconic Hollywood actress of Breakfast at Tiffany's and Roman Holiday.");
        HINTS.put("BOGART", "Cinematic noir icon of Casablanca and The Maltese Falcon.");
        HINTS.put("WAYNE", "Enduring Western film star known as 'The Duke'.");
        HINTS.put("EASTWOOD", "Legendary Western actor and Oscar-winning director of Unforgiven.");
        HINTS.put("LEE", "Martial arts cultural icon and star of Enter the Dragon.");
        HINTS.put("JACKSON", "Prolific screen legend known for unforgettable roles and intense line delivery.");
    }

    public static String[] getWordList(String category, String difficulty) {
        String normCat = GameStatsRepository.normalizeCategory(category);
        String normDiff = GameStatsRepository.normalizeDifficulty(difficulty);

        if (CATEGORY_HORROR_MOVIES.equals(normCat)) {
            if ("EASY".equals(normDiff)) return HORROR_MOVIES_EASY;
            if ("HARD".equals(normDiff)) return HORROR_MOVIES_HARD;
            return HORROR_MOVIES_MEDIUM;
        } else if (CATEGORY_CELEBRITIES.equals(normCat)) {
            if ("EASY".equals(normDiff)) return CELEBRITIES_EASY;
            if ("HARD".equals(normDiff)) return CELEBRITIES_HARD;
            return CELEBRITIES_MEDIUM;
        } else {
            if ("EASY".equals(normDiff)) return ANIMALS_EASY;
            if ("HARD".equals(normDiff)) return ANIMALS_HARD;
            return ANIMALS_MEDIUM;
        }
    }

    public static String[] getWordList(String category) {
        return getWordList(category, "MEDIUM");
    }

    public static String getRandomWord(String category, String difficulty) {
        String[] list = getWordList(category, difficulty);
        Random random = new Random();
        return list[random.nextInt(list.length)];
    }

    public static String getRandomWord(String category) {
        return getRandomWord(category, "MEDIUM");
    }

    /**
     * Returns an offline descriptive clue for Easy mode with safe fallbacks.
     */
    public static String getHint(String category, String word) {
        if (word == null || word.trim().isEmpty()) {
            return "A hidden mystery waiting in the gloom.";
        }
        String cleanWord = word.trim().toUpperCase();
        if (HINTS.containsKey(cleanWord)) {
            return HINTS.get(cleanWord);
        }

        // Safe categorical fallbacks
        String normCat = GameStatsRepository.normalizeCategory(category);
        switch (normCat) {
            case CATEGORY_HORROR_MOVIES:
                return "A chilling cinematic vision of death and dread.";
            case CATEGORY_CELEBRITIES:
                return "An iconic soul etched in history and memory.";
            case CATEGORY_ANIMALS:
            default:
                return "A creature lurking in the wild shadows.";
        }
    }
}
