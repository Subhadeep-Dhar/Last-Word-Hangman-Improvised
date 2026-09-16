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

public class ProfileActivity extends AppCompatActivity {
    private TextView tvUsernameLabel, tvPlayed, tvWins, tvLosses, tvWinRate, tvBestScore, tvStreak;
    private LinearLayout achievementsContainer;
    private Button btnBack;
    private PreferencesManager prefs;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        tvUsernameLabel = findViewById(R.id.tvUsernameLabel);
        tvPlayed = findViewById(R.id.tvPlayed);
        tvWins = findViewById(R.id.tvWins);
        tvLosses = findViewById(R.id.tvLosses);
        tvWinRate = findViewById(R.id.tvWinRate);
        tvBestScore = findViewById(R.id.tvBestScore);
        tvStreak = findViewById(R.id.tvStreak);
        achievementsContainer = findViewById(R.id.achievementsContainer);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });

        loadUserData();
    }

    private void loadUserData() {
        String username = prefs.getCurrentUser();
        if (username == null) return;

        tvUsernameLabel.setText("SUBJECT ID: " + username.toUpperCase());
        JSONObject user = prefs.getUserData(username);
        if (user != null) {
            int played = user.optInt("played", 0);
            int wins = user.optInt("wins", 0);
            int losses = user.optInt("losses", 0); 
            int highScore = user.optInt("highScore", 0);
            int streak = user.optInt("streak", 0);

            tvPlayed.setText("RITUALS PERFORMED: " + played);
            tvWins.setText("ESSENCES SAVED: " + wins);
            // Phase 19: Unified terminology
            tvLosses.setText("VICTIM LOSSES: " + losses);
            
            int rate = played > 0 ? (wins * 100 / played) : 0;
            tvWinRate.setText("STABILITY RATE: " + rate + "%");
            tvBestScore.setText("HIGHEST RESONANCE: " + highScore);
            tvStreak.setText("RESCUE STREAK: " + streak);

            JSONArray achs = user.optJSONArray("achievements");
            achievementsContainer.removeAllViews();
            
            if (achs != null && achs.length() > 0) {
                for (int i = 0; i < achs.length(); i++) {
                    String achName = achs.optString(i);
                    TextView tv = new TextView(this);
                    tv.setText("• MARK OF " + achName.replace("_", " "));
                    tv.setTextColor(Color.parseColor("#8B0000"));
                    tv.setTextSize(14);
                    tv.setPadding(0, 8, 0, 8);
                    tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    achievementsContainer.addView(tv);
                }
            } else {
                TextView tv = new TextView(this);
                tv.setText("No marks etched upon this soul.");
                tv.setTextColor(Color.DKGRAY);
                tv.setTextSize(14);
                tv.setPadding(0, 16, 0, 0);
                achievementsContainer.addView(tv);
            }
        }
    }
}
