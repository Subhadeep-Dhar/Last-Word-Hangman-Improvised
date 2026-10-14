package com.example.lastword;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.GameStatsRepository;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

public class SettingsActivity extends AppCompatActivity {
    private CheckBox cbMusic, cbSound, cbVibration, cbDarkMode;
    private Button btnResetData, btnBack;
    private PreferencesManager prefs;
    private SoundManager soundManager;
    private GameStatsRepository gameStatsRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);
        gameStatsRepo = GameStatsRepository.getInstance(this);
        AnimationHelper.enableImmersiveSticky(this);

        cbMusic = findViewById(R.id.cbMusic);
        cbSound = findViewById(R.id.cbSound);
        cbVibration = findViewById(R.id.cbVibration);
        cbDarkMode = findViewById(R.id.cbDarkMode);
        btnResetData = findViewById(R.id.btnResetData);
        btnBack = findViewById(R.id.btnBack);

        loadSettings();

        cbMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setMusicEnabled(isChecked);
            soundManager.playClick();
            if (isChecked) {
                soundManager.startMenuAtmosphere();
            } else {
                soundManager.stopAmbientAtmosphere();
            }
        });

        cbSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setSoundEnabled(isChecked);
            soundManager.playClick();
        });

        cbVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setVibrationEnabled(isChecked);
            soundManager.playClick();
        });

        cbDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setDarkMode(isChecked);
            soundManager.playClick();
        });

        btnResetData.setOnClickListener(v -> {
            soundManager.playClick();
            showResetConfirmationDialog();
        });

        btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });
    }

    private void showResetConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Erase Your Record?")
                .setMessage("All scores, saved lives, kills, streaks, and game history will be permanently erased from this device. This cannot be undone.")
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setPositiveButton("Reset Everything", (d, which) -> {
                    gameStatsRepo.resetAllData();
                    soundManager.playWrong();
                    Toast.makeText(SettingsActivity.this, "All records have been permanently erased.", Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().getDecorView().setBackgroundColor(Color.parseColor("#111111"));
        }
    }

    private void loadSettings() {
        cbMusic.setChecked(prefs.isMusicEnabled());
        cbSound.setChecked(prefs.isSoundEnabled());
        cbVibration.setChecked(prefs.isVibrationEnabled());
        cbDarkMode.setChecked(prefs.isDarkMode());
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
