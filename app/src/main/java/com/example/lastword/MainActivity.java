package com.example.lastword;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

public class MainActivity extends AppCompatActivity {
    private PreferencesManager prefs;
    private SoundManager soundManager;
    private com.example.lastword.utils.HorrorAtmosphereView horrorAtmosphereView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        horrorAtmosphereView = findViewById(R.id.horrorAtmosphereView);
        if (horrorAtmosphereView != null) {
            horrorAtmosphereView.start();
        }

        AnimationHelper.enableImmersiveSticky(this);
        // Chilling horror windy ambiance on opening
        soundManager.startHorrorWindyAmbience();

        TextView tvTitle = findViewById(R.id.tvSplashTitle);
        TextView tvSub = findViewById(R.id.tvSplashSub);
        View flicker = findViewById(R.id.flickerOverlay);
        com.example.lastword.utils.HorrorLampView horrorLampView = findViewById(R.id.horrorLampView);

        // Realistic blinking lamp and horror environment sequence
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            horrorLampView.startFlickerSequence(isLightOn -> {
                if (isLightOn) {
                    soundManager.playFilamentBuzz();
                    AnimationHelper.triggerSingleFlicker(flicker);
                }
            });

            // Reveal Title bathed in the intermittent lamp glow
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                tvTitle.animate().alpha(1f).scaleX(1.05f).scaleY(1.05f).setDuration(1400).start();
                soundManager.playHeartbeat(1);

                // Reveal Subtitle
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    tvSub.animate().alpha(1f).setDuration(1200).start();
                    soundManager.playHeartbeat(2);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Smooth fade out effect before leaving splash screen (visual + audio concurrently)
                        soundManager.fadeOutAmbientAtmosphere(1000, null);
                        View splashRoot = findViewById(R.id.splashRoot);
                        if (splashRoot != null) {
                            splashRoot.animate().alpha(0f).setDuration(1000).withEndAction(() -> {
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }).start();
                        } else {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }, 2400);
                }, 1000);
            }, 1200);
        }, 400);
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
