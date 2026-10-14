package com.example.lastword;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

import java.util.Random;

public class ResultActivity extends AppCompatActivity {
    private static final String KEY_SAVED_MESSAGE = "saved_thank_you_message";

    private TextView tvResultTitle, tvRescueMessage, tvResultWord, tvBaseScore, tvAccuracyBonus, tvTimeBonus, tvFinalScore;
    private Button btnPlayAgain, btnHome;
    private View resultRootLayout, contentLayout, buttonLayout;
    private SoundManager soundManager;
    private PreferencesManager prefs;
    private String displayedRescueMessage;

    private static final String[] THANK_YOU_MESSAGES = {
        "You came back for me… I thought no one would.",
        "I can breathe again. Thank you for not giving up on me.",
        "You saved me. I won't forget what you did here.",
        "I heard you fighting for me. Thank you.",
        "You gave me another chance. I owe you everything.",
        "I thought this was the end… and then you found me.",
        "You chose to save me. Thank you for seeing me.",
        "I was losing hope. You gave it back.",
        "The darkness was pulling me down... but your voice pulled me back.",
        "I felt the rope loosening... you really saved my life.",
        "I will never forget that you chose to fight for my soul.",
        "They told me no one could hear me in the void. You heard."
    };

    private static final String[] FAILURE_MESSAGES = {
        "You could have saved me... please, save the next one.",
        "Why did you let me drop? Don't let it happen again.",
        "I trusted you with my life... save the others.",
        "It hurts... please don't fail the next one.",
        "I didn't want to die... save them instead.",
        "You watched me hang... don't make them suffer too.",
        "I'm gone... but they are still waiting for you."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        soundManager = new SoundManager(this);
        prefs = new PreferencesManager(this);
        AnimationHelper.enableImmersiveSticky(this);

        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvRescueMessage = findViewById(R.id.tvRescueMessage);
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

        if (savedInstanceState != null) {
            displayedRescueMessage = savedInstanceState.getString(KEY_SAVED_MESSAGE);
        }

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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (displayedRescueMessage != null) {
            outState.putString(KEY_SAVED_MESSAGE, displayedRescueMessage);
        }
    }

    private void showVictory(String word) {
        resultRootLayout.setBackgroundColor(Color.parseColor("#050505"));
        buttonLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.VISIBLE);
        
        tvResultTitle.setText("YOU SAVED THEM.");
        tvResultTitle.setTextColor(Color.WHITE);

        // Pick a non-repeating personalized thank-you message
        if (displayedRescueMessage == null) {
            int lastIdx = prefs.getLastThankYouIndex();
            Random rand = new Random();
            int newIdx = rand.nextInt(THANK_YOU_MESSAGES.length);
            if (newIdx == lastIdx && THANK_YOU_MESSAGES.length > 1) {
                newIdx = (newIdx + 1) % THANK_YOU_MESSAGES.length;
            }
            prefs.setLastThankYouIndex(newIdx);
            displayedRescueMessage = THANK_YOU_MESSAGES[newIdx];
        }

        tvRescueMessage.setVisibility(View.VISIBLE);
        tvRescueMessage.setText("\"" + displayedRescueMessage + "\"");
        tvRescueMessage.setTextColor(Color.parseColor("#DDDDDD"));

        tvResultWord.setText("THE WORD REVEALED: " + word);
        tvResultWord.setTextColor(Color.parseColor("#888888"));

        tvBaseScore.setText("RITUAL BASE: " + getIntent().getIntExtra("BASE", 0));
        tvAccuracyBonus.setText("ACCURACY REWARD: " + getIntent().getIntExtra("ACCURACY", 0));
        tvTimeBonus.setText("TIME PRESERVATION: " + getIntent().getIntExtra("TIME_BONUS", 0));
        tvFinalScore.setText("TOTAL RESONANCE: " + getIntent().getIntExtra("SCORE", 0));
        
        AnimationHelper.fadeIn(contentLayout, 1200);
    }

    private void showFailure(String word, String difficulty) {
        tvRescueMessage.setVisibility(View.GONE);
        contentLayout.setVisibility(View.INVISIBLE);
        buttonLayout.setVisibility(View.INVISIBLE);
        resultRootLayout.setBackgroundColor(Color.BLACK);
        
        tvBaseScore.setVisibility(View.GONE);
        tvAccuracyBonus.setVisibility(View.GONE);
        tvTimeBonus.setVisibility(View.GONE);

        final Handler handler = new Handler(Looper.getMainLooper());
        String failureMessage = FAILURE_MESSAGES[new Random().nextInt(FAILURE_MESSAGES.length)];

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

            handler.postDelayed(() -> {
                tvResultWord.setText(failureMessage);
                tvResultWord.setTextColor(Color.parseColor("#8B0000"));
                tvResultWord.setAlpha(0f);
                tvResultWord.setVisibility(View.VISIBLE);
                
                tvResultWord.animate().alpha(1f).setDuration(1000).start();
                soundManager.playHeartbeat(2);

                handler.postDelayed(() -> {
                    tvFinalScore.setText("THE WORD WAS: " + word);
                    tvFinalScore.setTextColor(Color.WHITE);
                    tvFinalScore.setAlpha(0f);
                    tvFinalScore.setVisibility(View.VISIBLE);
                    
                    tvFinalScore.animate().alpha(1f).setDuration(1000).start();
                    AnimationHelper.glitchView(tvFinalScore);

                    handler.postDelayed(() -> {
                        AnimationHelper.fadeIn(buttonLayout, 1500);
                    }, 1000);

                }, 1300);

            }, 1000);

        }, 1000);
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
