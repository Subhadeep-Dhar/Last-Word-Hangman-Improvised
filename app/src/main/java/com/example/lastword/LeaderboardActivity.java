package com.example.lastword;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.utils.SoundManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class LeaderboardActivity extends AppCompatActivity {
    private LinearLayout leaderboardContainer;
    private Button btnBack;
    private PreferencesManager prefs;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        leaderboardContainer = findViewById(R.id.leaderboardContainer);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        leaderboardContainer.removeAllViews();
        JSONArray users = prefs.getAllUsersSortedByScore();

        if (users.length() == 0) {
            TextView tv = new TextView(this);
            tv.setText("The void remains empty. No records found.");
            tv.setTextColor(Color.GRAY);
            tv.setTextSize(14);
            leaderboardContainer.addView(tv);
            return;
        }

        String currentUser = prefs.getCurrentUser();

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                String name = user.getString("username");
                int score = user.optInt("highScore", 0);
                int losses = user.optInt("losses", 0); // Terminology change: Victim Losses

                TextView tv = new TextView(this);
                // Phase 19: Terminology update - Victim Losses
                tv.setText((i + 1) + ". " + name + " — Resonance: " + score + " | Victim Losses: " + losses);
                tv.setTextSize(15);
                tv.setPadding(16, 16, 16, 16);
                tv.setTypeface(android.graphics.Typeface.SERIF);

                if (name.equalsIgnoreCase(currentUser)) {
                    tv.setTextColor(Color.parseColor("#CC8400"));
                    tv.setBackgroundColor(Color.parseColor("#1A0000"));
                } else {
                    tv.setTextColor(Color.parseColor("#BBBBBB"));
                }

                leaderboardContainer.addView(tv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
