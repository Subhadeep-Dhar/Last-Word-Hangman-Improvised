package com.example.lastword;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

public class InstructionsActivity extends AppCompatActivity {
    private Button btnBack;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        soundManager = new SoundManager(this);
        AnimationHelper.enableImmersiveSticky(this);
        btnBack = findViewById(R.id.btnBack);
        android.view.View btnTopBack = findViewById(R.id.btnTopBack);

        android.view.View.OnClickListener backListener = v -> {
            soundManager.playClick();
            finish();
        };

        if (btnBack != null) btnBack.setOnClickListener(backListener);
        if (btnTopBack != null) btnTopBack.setOnClickListener(backListener);
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
