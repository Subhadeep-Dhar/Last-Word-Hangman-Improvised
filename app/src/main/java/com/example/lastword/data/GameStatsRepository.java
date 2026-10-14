package com.example.lastword.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class GameStatsRepository {
    private static GameStatsRepository instance;
    private final GameDatabaseHelper dbHelper;
    private final Context context;

    public static class GameStats {
        public final int gamesPlayed;
        public final int livesSaved;
        public final int kills;
        public final int totalScore;
        public final int highestScore;
        public final int currentWinningStreak;
        public final int highestWinningStreak;

        public GameStats(int gamesPlayed, int livesSaved, int kills, int totalScore,
                         int highestScore, int currentWinningStreak, int highestWinningStreak) {
            this.gamesPlayed = gamesPlayed;
            this.livesSaved = livesSaved;
            this.kills = kills;
            this.totalScore = totalScore;
            this.highestScore = highestScore;
            this.currentWinningStreak = currentWinningStreak;
            this.highestWinningStreak = highestWinningStreak;
        }

        public static GameStats empty() {
            return new GameStats(0, 0, 0, 0, 0, 0, 0);
        }
    }

    public static class DailyRecord {
        public final String dateStr;
        public final String targetWord;
        public final String category;
        public final boolean completed;
        public final boolean won;
        public final int score;

        public DailyRecord(String dateStr, String targetWord, String category,
                           boolean completed, boolean won, int score) {
            this.dateStr = dateStr;
            this.targetWord = targetWord;
            this.category = category;
            this.completed = completed;
            this.won = won;
            this.score = score;
        }
    }

    private GameStatsRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new GameDatabaseHelper(this.context);
        migrateLegacyDataIfNeeded();
    }

    public static synchronized GameStatsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new GameStatsRepository(context);
        }
        return instance;
    }

    public static String normalizeDifficulty(String diff) {
        if (diff == null) return "EASY";
        String upper = diff.trim().toUpperCase();
        if (upper.equals("INTERMEDIATE") || upper.equals("MEDIUM")) {
            return "MEDIUM";
        }
        if (upper.equals("HARD")) {
            return "HARD";
        }
        return "EASY";
    }

    public static String normalizeCategory(String cat) {
        if (cat == null) return "ANIMALS";
        String upper = cat.trim().toUpperCase();
        if (upper.equals("HORROR_MOVIES") || upper.equals("HORROR MOVIES") || upper.equals("MOVIES")) {
            return "HORROR_MOVIES";
        }
        if (upper.equals("CELEBRITIES")) {
            return "CELEBRITIES";
        }
        if (upper.equals("OBJECTS")) {
            return "OBJECTS"; // retained as legacy
        }
        return "ANIMALS";
    }

    /**
     * Records a finalized game result in a transaction-safe and idempotent manner.
     */
    public synchronized boolean recordGameResult(String gameId, String category, String difficulty,
                                                String mode, boolean won, int score,
                                                boolean isDaily, String dailyDate) {
        if (gameId == null || gameId.isEmpty()) {
            gameId = UUID.randomUUID().toString();
        }

        String normCategory = normalizeCategory(category);
        String normDifficulty = normalizeDifficulty(difficulty);
        String normMode = (mode == null || mode.trim().isEmpty()) ? "CLASSIC" : mode.trim().toUpperCase();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Check for duplicate gameId
            Cursor cursor = db.query(GameDatabaseHelper.TABLE_GAME_RECORDS,
                    new String[]{GameDatabaseHelper.COL_GAME_ID},
                    GameDatabaseHelper.COL_GAME_ID + " = ?",
                    new String[]{gameId}, null, null, null);
            boolean exists = (cursor != null && cursor.moveToFirst());
            if (cursor != null) cursor.close();

            if (exists) {
                // Game already recorded, do not duplicate
                db.setTransactionSuccessful();
                return false;
            }

            // If it's a daily challenge, check if today's daily challenge was already finalized
            if (isDaily && dailyDate != null) {
                Cursor dailyCursor = db.query(GameDatabaseHelper.TABLE_DAILY_CHALLENGES,
                        new String[]{GameDatabaseHelper.COL_COMPLETED},
                        GameDatabaseHelper.COL_DATE_STR + " = ? AND " + GameDatabaseHelper.COL_COMPLETED + " = 1",
                        new String[]{dailyDate}, null, null, null);
                boolean dailyAlreadyDone = (dailyCursor != null && dailyCursor.moveToFirst());
                if (dailyCursor != null) dailyCursor.close();

                if (dailyAlreadyDone) {
                    // Daily challenge already counted once. Do not record again.
                    db.setTransactionSuccessful();
                    return false;
                }
            }

            ContentValues values = new ContentValues();
            values.put(GameDatabaseHelper.COL_GAME_ID, gameId);
            values.put(GameDatabaseHelper.COL_CATEGORY, normCategory);
            values.put(GameDatabaseHelper.COL_DIFFICULTY, normDifficulty);
            values.put(GameDatabaseHelper.COL_MODE, normMode);
            values.put(GameDatabaseHelper.COL_WON, won ? 1 : 0);
            values.put(GameDatabaseHelper.COL_SCORE, Math.max(0, score));
            values.put(GameDatabaseHelper.COL_TIMESTAMP, System.currentTimeMillis());
            values.put(GameDatabaseHelper.COL_IS_DAILY, isDaily ? 1 : 0);
            values.put(GameDatabaseHelper.COL_DAILY_DATE, dailyDate);

            long insertId = db.insert(GameDatabaseHelper.TABLE_GAME_RECORDS, null, values);
            if (insertId == -1) {
                return false;
            }

            if (isDaily && dailyDate != null) {
                ContentValues dailyVals = new ContentValues();
                dailyVals.put(GameDatabaseHelper.COL_DATE_STR, dailyDate);
                dailyVals.put(GameDatabaseHelper.COL_TARGET_WORD, "");
                dailyVals.put(GameDatabaseHelper.COL_DAILY_CATEGORY, normCategory);
                dailyVals.put(GameDatabaseHelper.COL_COMPLETED, 1);
                dailyVals.put(GameDatabaseHelper.COL_DAILY_WON, won ? 1 : 0);
                dailyVals.put(GameDatabaseHelper.COL_DAILY_SCORE, Math.max(0, score));
                dailyVals.put(GameDatabaseHelper.COL_COMPLETED_AT, System.currentTimeMillis());

                db.insertWithOnConflict(GameDatabaseHelper.TABLE_DAILY_CHALLENGES, null,
                        dailyVals, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Computes authoritative overall player statistics reconstructed from game records.
     */
    public synchronized GameStats getOverallStats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " +
                "COUNT(*), " +
                "SUM(CASE WHEN " + GameDatabaseHelper.COL_WON + " = 1 THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN " + GameDatabaseHelper.COL_WON + " = 0 THEN 1 ELSE 0 END), " +
                "SUM(" + GameDatabaseHelper.COL_SCORE + "), " +
                "MAX(" + GameDatabaseHelper.COL_SCORE + ") " +
                "FROM " + GameDatabaseHelper.TABLE_GAME_RECORDS, null);

        int gamesPlayed = 0;
        int livesSaved = 0;
        int kills = 0;
        int totalScore = 0;
        int highestScore = 0;

        if (cursor != null && cursor.moveToFirst()) {
            gamesPlayed = cursor.getInt(0);
            livesSaved = cursor.getInt(1);
            kills = cursor.getInt(2);
            totalScore = cursor.getInt(3);
            highestScore = cursor.getInt(4);
            cursor.close();
        }

        // Calculate streaks chronologically
        int currentStreak = 0;
        int highestStreak = 0;

        Cursor streakCursor = db.query(GameDatabaseHelper.TABLE_GAME_RECORDS,
                new String[]{GameDatabaseHelper.COL_WON},
                null, null, null, null,
                GameDatabaseHelper.COL_TIMESTAMP + " ASC, " + GameDatabaseHelper.COL_ID + " ASC");

        if (streakCursor != null) {
            int running = 0;
            while (streakCursor.moveToNext()) {
                int won = streakCursor.getInt(0);
                if (won == 1) {
                    running++;
                    if (running > highestStreak) {
                        highestStreak = running;
                    }
                } else {
                    running = 0;
                }
            }
            currentStreak = running;
            streakCursor.close();
        }

        return new GameStats(gamesPlayed, livesSaved, kills, totalScore, highestScore,
                currentStreak, highestStreak);
    }

    /**
     * Computes statistics for a specific category and difficulty combination.
     */
    public synchronized GameStats getCategoryDifficultyStats(String category, String difficulty) {
        String normCategory = normalizeCategory(category);
        String normDifficulty = normalizeDifficulty(difficulty);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Handle possible intermediate/medium synonym query
        String whereClause = GameDatabaseHelper.COL_CATEGORY + " = ? AND (" +
                GameDatabaseHelper.COL_DIFFICULTY + " = ? OR " +
                (normDifficulty.equals("MEDIUM") ? GameDatabaseHelper.COL_DIFFICULTY + " = 'INTERMEDIATE'" : "0=1") + ")";
        String[] whereArgs = new String[]{normCategory, normDifficulty};

        Cursor cursor = db.rawQuery("SELECT " +
                "COUNT(*), " +
                "SUM(CASE WHEN " + GameDatabaseHelper.COL_WON + " = 1 THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN " + GameDatabaseHelper.COL_WON + " = 0 THEN 1 ELSE 0 END), " +
                "SUM(" + GameDatabaseHelper.COL_SCORE + "), " +
                "MAX(" + GameDatabaseHelper.COL_SCORE + ") " +
                "FROM " + GameDatabaseHelper.TABLE_GAME_RECORDS + " WHERE " + whereClause, whereArgs);

        int gamesPlayed = 0;
        int livesSaved = 0;
        int kills = 0;
        int totalScore = 0;
        int highestScore = 0;

        if (cursor != null && cursor.moveToFirst()) {
            gamesPlayed = cursor.getInt(0);
            livesSaved = cursor.getInt(1);
            kills = cursor.getInt(2);
            totalScore = cursor.getInt(3);
            highestScore = cursor.getInt(4);
            cursor.close();
        }

        // Streak for this specific combination
        int currentStreak = 0;
        int highestStreak = 0;

        Cursor streakCursor = db.query(GameDatabaseHelper.TABLE_GAME_RECORDS,
                new String[]{GameDatabaseHelper.COL_WON},
                whereClause, whereArgs, null, null,
                GameDatabaseHelper.COL_TIMESTAMP + " ASC, " + GameDatabaseHelper.COL_ID + " ASC");

        if (streakCursor != null) {
            int running = 0;
            while (streakCursor.moveToNext()) {
                int won = streakCursor.getInt(0);
                if (won == 1) {
                    running++;
                    if (running > highestStreak) {
                        highestStreak = running;
                    }
                } else {
                    running = 0;
                }
            }
            currentStreak = running;
            streakCursor.close();
        }

        return new GameStats(gamesPlayed, livesSaved, kills, totalScore, highestScore,
                currentStreak, highestStreak);
    }

    /**
     * Checks if the Daily Challenge for the given date is completed.
     */
    public synchronized boolean isDailyChallengeCompleted(String dateStr) {
        if (dateStr == null) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(GameDatabaseHelper.TABLE_DAILY_CHALLENGES,
                new String[]{GameDatabaseHelper.COL_COMPLETED},
                GameDatabaseHelper.COL_DATE_STR + " = ? AND " + GameDatabaseHelper.COL_COMPLETED + " = 1",
                new String[]{dateStr}, null, null, null);
        boolean completed = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return completed;
    }

    /**
     * Gets the Daily Challenge record for the given date if completed.
     */
    public synchronized DailyRecord getDailyChallengeRecord(String dateStr) {
        if (dateStr == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(GameDatabaseHelper.TABLE_DAILY_CHALLENGES,
                null,
                GameDatabaseHelper.COL_DATE_STR + " = ?",
                new String[]{dateStr}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String word = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COL_TARGET_WORD));
            String cat = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COL_DAILY_CATEGORY));
            boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COL_COMPLETED)) == 1;
            boolean won = cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COL_DAILY_WON)) == 1;
            int score = cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COL_DAILY_SCORE));
            cursor.close();
            return new DailyRecord(dateStr, word, cat, completed, won, score);
        }
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * Resets all game statistics and history safely in a single transaction.
     */
    public synchronized void resetAllData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(GameDatabaseHelper.TABLE_GAME_RECORDS, null, null);
            db.delete(GameDatabaseHelper.TABLE_DAILY_CHALLENGES, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void migrateLegacyDataIfNeeded() {
        try {
            PreferencesManager prefs = new PreferencesManager(context);
            if (prefs.isStatsMigrated()) {
                return;
            }

            // Check if there were old users in SharedPreferences
            String usersRaw = prefs.getRawUsersJson();
            if (usersRaw != null && !usersRaw.trim().isEmpty() && !usersRaw.equals("[]")) {
                JSONArray array = new JSONArray(usersRaw);
                // Look for current user or first user with stats
                String currentUser = prefs.getCurrentUser();
                JSONObject chosenUser = null;
                for (int i = 0; i < array.length(); i++) {
                    JSONObject u = array.getJSONObject(i);
                    if (currentUser != null && u.getString("username").equalsIgnoreCase(currentUser)) {
                        chosenUser = u;
                        break;
                    }
                }
                if (chosenUser == null && array.length() > 0) {
                    chosenUser = array.getJSONObject(0);
                }

                if (chosenUser != null) {
                    int wins = chosenUser.optInt("wins", 0);
                    int losses = chosenUser.optInt("losses", 0);
                    int highScore = chosenUser.optInt("highScore", 0);

                    // Insert legacy summary records if any exist
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.beginTransaction();
                    try {
                        long now = System.currentTimeMillis();
                        for (int w = 0; w < wins; w++) {
                            ContentValues cv = new ContentValues();
                            cv.put(GameDatabaseHelper.COL_GAME_ID, "legacy_win_" + w + "_" + UUID.randomUUID());
                            cv.put(GameDatabaseHelper.COL_CATEGORY, "ANIMALS");
                            cv.put(GameDatabaseHelper.COL_DIFFICULTY, "EASY");
                            cv.put(GameDatabaseHelper.COL_MODE, "CLASSIC");
                            cv.put(GameDatabaseHelper.COL_WON, 1);
                            cv.put(GameDatabaseHelper.COL_SCORE, (w == wins - 1) ? highScore : 100);
                            cv.put(GameDatabaseHelper.COL_TIMESTAMP, now - (1000L * (wins + losses - w)));
                            cv.put(GameDatabaseHelper.COL_IS_DAILY, 0);
                            db.insert(GameDatabaseHelper.TABLE_GAME_RECORDS, null, cv);
                        }
                        for (int l = 0; l < losses; l++) {
                            ContentValues cv = new ContentValues();
                            cv.put(GameDatabaseHelper.COL_GAME_ID, "legacy_loss_" + l + "_" + UUID.randomUUID());
                            cv.put(GameDatabaseHelper.COL_CATEGORY, "ANIMALS");
                            cv.put(GameDatabaseHelper.COL_DIFFICULTY, "EASY");
                            cv.put(GameDatabaseHelper.COL_MODE, "CLASSIC");
                            cv.put(GameDatabaseHelper.COL_WON, 0);
                            cv.put(GameDatabaseHelper.COL_SCORE, 0);
                            cv.put(GameDatabaseHelper.COL_TIMESTAMP, now - (500L * (losses - l)));
                            cv.put(GameDatabaseHelper.COL_IS_DAILY, 0);
                            db.insert(GameDatabaseHelper.TABLE_GAME_RECORDS, null, cv);
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }
            }

            prefs.setStatsMigrated(true);
            prefs.clearAuthData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
