package com.example.lastword;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

public class GameSetupActivity extends AppCompatActivity {
    private RadioGroup rgCategory, rgDifficulty, rgMode;
    private Button btnEnterRoom;
    private SoundManager soundManager;
    private com.example.lastword.utils.HorrorAtmosphereView horrorAtmosphereView;
    private com.example.lastword.utils.BloodSplashOverlayView bloodSplashOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_setup);

        soundManager = new SoundManager(this);
        AnimationHelper.enableImmersiveSticky(this);

        bloodSplashOverlay = findViewById(R.id.bloodSplashOverlay);
        if (bloodSplashOverlay != null) {
            bloodSplashOverlay.setAsBackgroundAtmosphere(true);
        }
        horrorAtmosphereView = findViewById(R.id.horrorAtmosphereView);
        if (horrorAtmosphereView != null) {
            horrorAtmosphereView.start();
        }

        rgCategory = findViewById(R.id.rgCategory);
        rgDifficulty = findViewById(R.id.rgDifficulty);
        rgMode = findViewById(R.id.rgMode);
        btnEnterRoom = findViewById(R.id.btnEnterRoom);

        rgCategory.setOnCheckedChangeListener((g, id) -> soundManager.playClick());
        rgDifficulty.setOnCheckedChangeListener((g, id) -> soundManager.playClick());
        rgMode.setOnCheckedChangeListener((g, id) -> soundManager.playClick());

        android.view.View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                soundManager.playClick();
                finish();
            });
        }

        btnEnterRoom.setOnClickListener(v -> {
            soundManager.playClick();

            String category = "ANIMALS";
            int catChecked = rgCategory.getCheckedRadioButtonId();
            if (catChecked == R.id.rbHorrorMovies) {
                category = "HORROR_MOVIES";
            } else if (catChecked == R.id.rbCelebrities) {
                category = "CELEBRITIES";
            }

            String difficulty = "EASY";
            int diffChecked = rgDifficulty.getCheckedRadioButtonId();
            if (diffChecked == R.id.rbIntermediate) {
                difficulty = "MEDIUM";
            } else if (diffChecked == R.id.rbHard) {
                difficulty = "HARD";
            }

            String mode = "CLASSIC";
            int modeChecked = rgMode.getCheckedRadioButtonId();
            if (modeChecked == R.id.rbTimer) {
                mode = "TIMER";
            }

            Intent intent = new Intent(GameSetupActivity.this, GameActivity.class);
            intent.putExtra("CATEGORY", category);
            intent.putExtra("DIFFICULTY", difficulty);
            intent.putExtra("MODE", mode);
            startActivity(intent);
            finish();
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
        if (horrorAtmosphereView != null) {
            horrorAtmosphereView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (horrorAtmosphereView != null) {
            horrorAtmosphereView.stop();
        }
        soundManager.stopAmbientAtmosphere();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (horrorAtmosphereView != null) {
            horrorAtmosphereView.stop();
        }
        soundManager.stopAmbientAtmosphere();
    }
}
