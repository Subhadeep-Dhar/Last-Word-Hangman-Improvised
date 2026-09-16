package com.example.lastword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    private Button btnEnterGame, btnProfile, btnLeaderboard, btnInstructions, btnSettings;
    private TextView tvActiveSubject, tvSaved, tvLosses;
    private View flickerOverlay;
    private PreferencesManager prefs;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        btnEnterGame = findViewById(R.id.btnEnterGame);
        btnProfile = findViewById(R.id.btnProfile);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnInstructions = findViewById(R.id.btnInstructions);
        btnSettings = findViewById(R.id.btnSettings);
        tvActiveSubject = findViewById(R.id.tvActiveSubject);
        tvSaved = findViewById(R.id.tvSaved);
        tvLosses = findViewById(R.id.tvLosses);
        flickerOverlay = findViewById(R.id.flickerOverlay);

        AnimationHelper.startFlickerSystem(flickerOverlay);
        updateUI();

        btnEnterGame.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, GameSetupActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        btnLeaderboard.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, LeaderboardActivity.class));
        });

        btnInstructions.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, InstructionsActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        String user = prefs.getCurrentUser();
        if (user != null) {
            JSONObject data = prefs.getUserData(user);
            if (data != null) {
                int kills = data.optInt("losses", 0);
                int wins = data.optInt("wins", 0);
                tvActiveSubject.setText("Active Subject: " + user);
                tvSaved.setText("Saved: " + wins);
                tvLosses.setText("Victim Losses: " + kills);
            }
        }
    }
}
