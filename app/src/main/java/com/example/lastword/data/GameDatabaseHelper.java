package com.example.lastword.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "lastword_game.db";
    public static final int DATABASE_VERSION = 1;

    // Table: game_records
    public static final String TABLE_GAME_RECORDS = "game_records";
    public static final String COL_ID = "_id";
    public static final String COL_GAME_ID = "game_id";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DIFFICULTY = "difficulty";
    public static final String COL_MODE = "mode";
    public static final String COL_WON = "won";
    public static final String COL_SCORE = "score";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_IS_DAILY = "is_daily";
    public static final String COL_DAILY_DATE = "daily_date";

    // Table: daily_challenges
    public static final String TABLE_DAILY_CHALLENGES = "daily_challenges";
    public static final String COL_DATE_STR = "date_str";
    public static final String COL_TARGET_WORD = "target_word";
    public static final String COL_DAILY_CATEGORY = "category";
    public static final String COL_COMPLETED = "completed";
    public static final String COL_DAILY_WON = "won";
    public static final String COL_DAILY_SCORE = "score";
    public static final String COL_COMPLETED_AT = "completed_at";

    public GameDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createGameRecords = "CREATE TABLE " + TABLE_GAME_RECORDS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_GAME_ID + " TEXT UNIQUE NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_DIFFICULTY + " TEXT NOT NULL, "
                + COL_MODE + " TEXT NOT NULL, "
                + COL_WON + " INTEGER NOT NULL, "
                + COL_SCORE + " INTEGER NOT NULL, "
                + COL_TIMESTAMP + " INTEGER NOT NULL, "
                + COL_IS_DAILY + " INTEGER NOT NULL DEFAULT 0, "
                + COL_DAILY_DATE + " TEXT"
                + ");";

        String createDailyChallenges = "CREATE TABLE " + TABLE_DAILY_CHALLENGES + " ("
                + COL_DATE_STR + " TEXT PRIMARY KEY NOT NULL, "
                + COL_TARGET_WORD + " TEXT NOT NULL, "
                + COL_DAILY_CATEGORY + " TEXT NOT NULL, "
                + COL_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_DAILY_WON + " INTEGER NOT NULL DEFAULT 0, "
                + COL_DAILY_SCORE + " INTEGER NOT NULL DEFAULT 0, "
                + COL_COMPLETED_AT + " INTEGER NOT NULL DEFAULT 0"
                + ");";

        db.execSQL(createGameRecords);
        db.execSQL(createDailyChallenges);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the initial release
    }
}
