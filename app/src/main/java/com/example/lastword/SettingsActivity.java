package com.example.lastword;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.utils.SoundManager;

public class SettingsActivity extends AppCompatActivity {
    private CheckBox cbMusic, cbSound, cbVibration, cbDarkMode;
    private Button btnLogout, btnBack;
    private PreferencesManager prefs;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        cbMusic = findViewById(R.id.cbMusic);
        cbSound = findViewById(R.id.cbSound);
        cbVibration = findViewById(R.id.cbVibration);
        cbDarkMode = findViewById(R.id.cbDarkMode);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        loadSettings();

        cbMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setMusicEnabled(isChecked);
            soundManager.playClick();
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

        btnLogout.setOnClickListener(v -> {
            soundManager.playClick();
            prefs.logout();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });
    }

    private void loadSettings() {
        cbMusic.setChecked(prefs.isMusicEnabled());
        cbSound.setChecked(prefs.isSoundEnabled());
        cbVibration.setChecked(prefs.isVibrationEnabled());
        cbDarkMode.setChecked(prefs.isDarkMode());
    }
}
