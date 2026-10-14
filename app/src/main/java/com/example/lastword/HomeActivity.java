package com.example.lastword;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.GameStatsRepository;
import com.example.lastword.data.WordRepository;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {
    private Button btnEnterGame, btnDailyChallenge, btnScoreStats, btnInstructions;
    private ImageButton btnSettingsIcon;
    private TextView tvFooterGames, tvFooterStreak;
    private View flickerOverlay;
    private SoundManager soundManager;
    private GameStatsRepository gameStatsRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        soundManager = new SoundManager(this);
        gameStatsRepo = GameStatsRepository.getInstance(this);

        btnEnterGame = findViewById(R.id.btnEnterGame);
        btnDailyChallenge = findViewById(R.id.btnDailyChallenge);
        btnScoreStats = findViewById(R.id.btnScoreStats);
        btnInstructions = findViewById(R.id.btnInstructions);
        btnSettingsIcon = findViewById(R.id.btnSettingsIcon);

        tvFooterGames = findViewById(R.id.tvFooterGames);
        tvFooterStreak = findViewById(R.id.tvFooterStreak);
        flickerOverlay = findViewById(R.id.flickerOverlay);

        AnimationHelper.startFlickerSystem(flickerOverlay);
        AnimationHelper.enableImmersiveSticky(this);
        updateUI();

        // 1. Play Game
        btnEnterGame.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, GameSetupActivity.class));
        });

        // 2. Daily Challenge
        btnDailyChallenge.setOnClickListener(v -> {
            soundManager.playClick();
            handleDailyChallengeClick();
        });

        // 3. Score & Stats
        btnScoreStats.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        // 4. Help / Instructions
        btnInstructions.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, InstructionsActivity.class));
        });

        // Secondary Entry Point: Settings
        btnSettingsIcon.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            AnimationHelper.enableImmersiveSticky(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnimationHelper.enableImmersiveSticky(this);
        soundManager.startMenuAtmosphere();
        updateUI();
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

    private void handleDailyChallengeClick() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Check if today's daily challenge has already been completed
        if (gameStatsRepo.isDailyChallengeCompleted(todayDate)) {
            GameStatsRepository.DailyRecord record = gameStatsRepo.getDailyChallengeRecord(todayDate);
            String outcome = (record != null && record.won) ? "SAVED (+ " + record.score + " pts)" : "SLAIN (0 pts)";
            
            AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle("DAILY RITUAL COMPLETE")
                    .setMessage("You have already faced the tether for " + todayDate + ".\n\nOutcome: " + outcome +
                            "\n\nA new daily sacrifice awakens at midnight.")
                    .setPositiveButton("RETURN TO SURFACE", (d, which) -> d.dismiss())
                    .create();
            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().getDecorView().setBackgroundColor(Color.parseColor("#111111"));
            }
            return;
        }

        // Deterministic daily word & category derived from the local calendar date
        long seed = todayDate.hashCode();
        Random rand = new Random(seed);

        String[] categories = {
                WordRepository.CATEGORY_ANIMALS,
                WordRepository.CATEGORY_HORROR_MOVIES,
                WordRepository.CATEGORY_CELEBRITIES
        };
        String selectedCat = categories[Math.abs(rand.nextInt()) % categories.length];
        String[] words = WordRepository.getWordList(selectedCat, "MEDIUM");
        String selectedWord = words[Math.abs(rand.nextInt()) % words.length];

        // Launch directly into GameActivity with Medium difficulty locked
        Intent intent = new Intent(HomeActivity.this, GameActivity.class);
        intent.putExtra("CATEGORY", selectedCat);
        intent.putExtra("DIFFICULTY", "MEDIUM");
        intent.putExtra("MODE", "CLASSIC");
        intent.putExtra("TARGET_WORD", selectedWord);
        intent.putExtra("IS_DAILY", true);
        intent.putExtra("DAILY_DATE", todayDate);
        startActivity(intent);
    }

    private void updateUI() {
        GameStatsRepository.GameStats stats = gameStatsRepo.getOverallStats();
        tvFooterGames.setText("RITUALS: " + stats.gamesPlayed + " | SAVED: " + stats.livesSaved + " | KILLS: " + stats.kills);
        tvFooterStreak.setText("STREAK: " + stats.currentWinningStreak + " | HIGH SCORE: " + stats.highestScore);
    }
}
