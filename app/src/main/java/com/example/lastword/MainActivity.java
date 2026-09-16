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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        TextView tvTitle = findViewById(R.id.tvSplashTitle);
        TextView tvSub = findViewById(R.id.tvSplashSub);
        View flicker = findViewById(R.id.flickerOverlay);

        // Cinematic Entrance
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvTitle.animate().alpha(1f).setDuration(2000).start();
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                tvSub.animate().alpha(1f).setDuration(1500).start();
                soundManager.playHeartbeat(1);
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    AnimationHelper.triggerSingleFlicker(flicker);
                    
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Route to Login
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }, 1000);
                }, 1000);
            }, 1000);
        }, 500);
    }
}
