package com.example.lastword;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.utils.SoundManager;

public class InstructionsActivity extends AppCompatActivity {
    private Button btnBack;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        soundManager = new SoundManager(this);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });
    }
}
