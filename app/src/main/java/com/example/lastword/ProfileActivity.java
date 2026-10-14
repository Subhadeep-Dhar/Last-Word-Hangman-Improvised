package com.example.lastword;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.GameStatsRepository;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvPrimaryGames, tvPrimarySaved, tvPrimaryKills, tvPrimaryStreak;
    private TextView tvSecondaryHighScore, tvSecondaryTotalScore, tvSecondaryBestStreak;
    private TextView tvBreakdownGames, tvBreakdownSaved, tvBreakdownKills, tvBreakdownTotalScore;
    private TextView tvBreakdownHighScore, tvBreakdownCurrentStreak, tvBreakdownHighestStreak;
    private Spinner spCategory, spDifficulty;
    private Button btnBack;

    private GameStatsRepository gameStatsRepo;
    private SoundManager soundManager;

    private final String[] categories = {"ANIMALS", "HORROR MOVIES", "CELEBRITIES"};
    private final String[] difficulties = {"EASY", "MEDIUM", "HARD"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gameStatsRepo = GameStatsRepository.getInstance(this);
        soundManager = new SoundManager(this);
        AnimationHelper.enableImmersiveSticky(this);

        tvPrimaryGames = findViewById(R.id.tvPrimaryGames);
        tvPrimarySaved = findViewById(R.id.tvPrimarySaved);
        tvPrimaryKills = findViewById(R.id.tvPrimaryKills);
        tvPrimaryStreak = findViewById(R.id.tvPrimaryStreak);

        tvSecondaryHighScore = findViewById(R.id.tvSecondaryHighScore);
        tvSecondaryTotalScore = findViewById(R.id.tvSecondaryTotalScore);
        tvSecondaryBestStreak = findViewById(R.id.tvSecondaryBestStreak);

        spCategory = findViewById(R.id.spCategory);
        spDifficulty = findViewById(R.id.spDifficulty);

        tvBreakdownGames = findViewById(R.id.tvBreakdownGames);
        tvBreakdownSaved = findViewById(R.id.tvBreakdownSaved);
        tvBreakdownKills = findViewById(R.id.tvBreakdownKills);
        tvBreakdownTotalScore = findViewById(R.id.tvBreakdownTotalScore);
        tvBreakdownHighScore = findViewById(R.id.tvBreakdownHighScore);
        tvBreakdownCurrentStreak = findViewById(R.id.tvBreakdownCurrentStreak);
        tvBreakdownHighestStreak = findViewById(R.id.tvBreakdownHighestStreak);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });

        setupSpinners();
        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnimationHelper.enableImmersiveSticky(this);
        soundManager.startMenuAtmosphere();
        loadStats();
    }

    private void setupSpinners() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(14);
                return tv;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#141414"));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        ArrayAdapter<String> diffAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, difficulties) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(14);
                return tv;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#141414"));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(diffAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBreakdown();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spCategory.setOnItemSelectedListener(listener);
        spDifficulty.setOnItemSelectedListener(listener);
    }

    private void loadStats() {
        // Load Primary & Secondary summaries
        GameStatsRepository.GameStats overall = gameStatsRepo.getOverallStats();

        tvPrimaryGames.setText("GAMES PLAYED: " + overall.gamesPlayed);
        tvPrimarySaved.setText("LIVES SAVED: " + overall.livesSaved);
        tvPrimaryKills.setText("KILLS CAUSED: " + overall.kills);
        tvPrimaryStreak.setText("CURRENT STREAK: " + overall.currentWinningStreak);

        tvSecondaryHighScore.setText("HIGHEST SCORE: " + overall.highestScore);
        tvSecondaryTotalScore.setText("TOTAL SCORE: " + overall.totalScore);
        tvSecondaryBestStreak.setText("HIGHEST STREAK: " + overall.highestWinningStreak);

        updateBreakdown();
    }

    private void updateBreakdown() {
        String cat = (String) spCategory.getSelectedItem();
        String diff = (String) spDifficulty.getSelectedItem();

        if (cat == null) cat = "ANIMALS";
        if (diff == null) diff = "EASY";

        GameStatsRepository.GameStats breakdown = gameStatsRepo.getCategoryDifficultyStats(cat, diff);

        tvBreakdownGames.setText("GAMES PLAYED: " + breakdown.gamesPlayed);
        tvBreakdownSaved.setText("LIVES SAVED: " + breakdown.livesSaved);
        tvBreakdownKills.setText("KILLS CAUSED: " + breakdown.kills);
        tvBreakdownTotalScore.setText("TOTAL SCORE: " + breakdown.totalScore);
        tvBreakdownHighScore.setText("HIGHEST SCORE: " + breakdown.highestScore);
        tvBreakdownCurrentStreak.setText("CURRENT STREAK: " + breakdown.currentWinningStreak);
        tvBreakdownHighestStreak.setText("HIGHEST STREAK: " + breakdown.highestWinningStreak);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            AnimationHelper.enableImmersiveSticky(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        soundManager.stopAmbientAtmosphere();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.stopAmbientAtmosphere();
    }
}
