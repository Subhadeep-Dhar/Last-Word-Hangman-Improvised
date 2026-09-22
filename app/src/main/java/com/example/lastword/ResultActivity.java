package com.example.lastword;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

public class ResultActivity extends AppCompatActivity {
    private TextView tvResultTitle, tvResultWord, tvBaseScore, tvAccuracyBonus, tvTimeBonus, tvFinalScore;
    private Button btnPlayAgain, btnHome;
    private View resultRootLayout, contentLayout, buttonLayout;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        soundManager = new SoundManager(this);

        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultWord = findViewById(R.id.tvResultWord);
        tvBaseScore = findViewById(R.id.tvBaseScore);
        tvAccuracyBonus = findViewById(R.id.tvAccuracyBonus);
        tvTimeBonus = findViewById(R.id.tvTimeBonus);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnHome = findViewById(R.id.btnHome);
        resultRootLayout = findViewById(R.id.resultRootLayout);
        contentLayout = findViewById(R.id.contentLayout);
        buttonLayout = findViewById(R.id.buttonLayout);

        boolean won = getIntent().getBooleanExtra("WON", false);
        String word = getIntent().getStringExtra("WORD");
        String difficulty = getIntent().getStringExtra("DIFFICULTY");

        if (won) {
            showVictory(word);
        } else {
            showFailure(word, difficulty);
        }

        btnPlayAgain.setOnClickListener(v -> {
            soundManager.playClick();
            Intent intent = new Intent(ResultActivity.this, GameSetupActivity.class);
            startActivity(intent);
            finish();
        });

        btnHome.setOnClickListener(v -> {
            soundManager.playClick();
            Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void showVictory(String word) {
        resultRootLayout.setBackgroundColor(Color.parseColor("#050505"));
        buttonLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.VISIBLE);
        
        tvResultTitle.setText("YOU SAVED THEM.");
        tvResultTitle.setTextColor(Color.WHITE);
        tvResultWord.setText("THE WORD REVEALED: " + word);

        tvBaseScore.setText("RITUAL BASE: " + getIntent().getIntExtra("BASE", 0));
        tvAccuracyBonus.setText("ACCURACY REWARD: " + getIntent().getIntExtra("ACCURACY", 0));
        tvTimeBonus.setText("TIME PRESERVATION: " + getIntent().getIntExtra("TIME_BONUS", 0));
        tvFinalScore.setText("TOTAL RESONANCE: " + getIntent().getIntExtra("SCORE", 0));
        
        AnimationHelper.fadeIn(contentLayout, 1200);
    }

    private void showFailure(String word, String difficulty) {
        contentLayout.setVisibility(View.INVISIBLE);
        buttonLayout.setVisibility(View.INVISIBLE);
        resultRootLayout.setBackgroundColor(Color.BLACK);
        
        tvBaseScore.setVisibility(View.GONE);
        tvAccuracyBonus.setVisibility(View.GONE);
        tvTimeBonus.setVisibility(View.GONE);

        final Handler handler = new Handler(Looper.getMainLooper());

        String[] horribleMessages = {
            "You could have saved me... please, save the next one.",
            "Why did you let me drop? Don't let it happen again.",
            "I trusted you with my life... save the others.",
            "It hurts... please don't fail the next one.",
            "I didn't want to die... save them instead.",
            "You watched me hang... don't make them suffer too.",
            "I'm gone... but they are still waiting for you."
        };
        String failureMessage = horribleMessages[new java.util.Random().nextInt(horribleMessages.length)];

        // 1000ms delay + 1200ms from GameActivity = 2200ms total
        handler.postDelayed(() -> {
            tvResultTitle.setText("TOO LATE.");
            tvResultTitle.setTextColor(Color.parseColor("#8B0000"));
            tvResultTitle.setAlpha(0f);
            tvResultTitle.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.VISIBLE);
            tvResultWord.setVisibility(View.INVISIBLE);
            tvFinalScore.setVisibility(View.INVISIBLE);
            
            tvResultTitle.animate().alpha(1f).setDuration(1000).start();
            soundManager.playWrong();

            // 3200ms marker
            handler.postDelayed(() -> {
                tvResultWord.setText(failureMessage);
                tvResultWord.setTextColor(Color.parseColor("#660000"));
                tvResultWord.setAlpha(0f);
                tvResultWord.setVisibility(View.VISIBLE);
                
                tvResultWord.animate().alpha(1f).setDuration(1000).start();
                soundManager.playHeartbeat(2);

                // 4500ms marker
                handler.postDelayed(() -> {
                    tvFinalScore.setText("THE WORD WAS: " + word);
                    tvFinalScore.setTextColor(Color.WHITE);
                    tvFinalScore.setAlpha(0f);
                    tvFinalScore.setVisibility(View.VISIBLE);
                    
                    tvFinalScore.animate().alpha(1f).setDuration(1000).start();
                    AnimationHelper.glitchView(tvFinalScore);

                    // 5500ms marker
                    handler.postDelayed(() -> {
                        // The void swallows them. No further text needed.
                        
                        AnimationHelper.fadeIn(buttonLayout, 1500);
                    }, 1000);

                }, 1300);

            }, 1000);

        }, 1000);
    }
}
