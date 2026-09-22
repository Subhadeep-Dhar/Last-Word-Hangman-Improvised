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
    private TextView tvTitle, tvSubtitle, tvActiveSubject, tvAnimalsStat, tvAdultsStat, tvChildrenStat;
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
        tvAnimalsStat = findViewById(R.id.tvAnimalsStat);
        tvAdultsStat = findViewById(R.id.tvAdultsStat);
        tvChildrenStat = findViewById(R.id.tvChildrenStat);
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
                tvActiveSubject.setText("Active Subject: " + user);
                
                int animalsSaved = data.optInt("animals_saved", 0);
                int animalsKilled = data.optInt("animals_killed", 0);
                int adultsSaved = data.optInt("adults_saved", 0);
                int adultsKilled = data.optInt("adults_killed", 0);
                int childrenSaved = data.optInt("children_saved", 0);
                int childrenKilled = data.optInt("children_killed", 0);

                tvAnimalsStat.setText("Animals: " + animalsSaved + " Saved | " + animalsKilled + " Sacrificed");
                tvAdultsStat.setText("Adults: " + adultsSaved + " Saved | " + adultsKilled + " Murdered");
                tvChildrenStat.setText("Children: " + childrenSaved + " Saved | " + childrenKilled + " Lost");
            }
        } else {
            tvActiveSubject.setText("Active Subject: None");
            tvAnimalsStat.setText("Animals: 0 Saved | 0 Sacrificed");
            tvAdultsStat.setText("Adults: 0 Saved | 0 Murdered");
            tvChildrenStat.setText("Children: 0 Saved | 0 Lost");
        }
    }
}
