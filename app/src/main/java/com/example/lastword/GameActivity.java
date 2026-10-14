package com.example.lastword;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.GameStatsRepository;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.data.WordRepository;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.HangmanView;
import com.example.lastword.utils.SoundManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameActivity extends AppCompatActivity {
    private String category, difficulty, mode, targetWord;
    private boolean isDailyChallenge = false;
    private String dailyDate = null;
    private String gameId;

    private StringBuilder displayedWord;
    private int attemptsLeft = 6;
    private int horrorState = 0;
    private int score = 0;
    private long timeRemainingMillis = 60000;
    private CountDownTimer gameTimer;
    private List<Character> guessedLetters = new ArrayList<>();

    private TextView tvGameCategory, tvGameAttempts, tvGameTimer, tvHiddenWord, tvHorrorStatusText, tvGameHint;
    private View horrorEnvironmentView, vignetteOverlay, gameFlickerOverlay;
    private FrameLayout horrorFrame;
    private GridLayout glAlphabetKeyboard;
    private HangmanView hangmanView;
    private com.example.lastword.utils.BloodSplashOverlayView bloodSplashOverlay;
    private com.example.lastword.utils.HorrorAtmosphereView horrorAtmosphereView;

    private PreferencesManager prefs;
    private SoundManager soundManager;
    private GameStatsRepository gameStatsRepo;
    private boolean isGameOver = false;
    private boolean isProcessingGuess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);
        gameStatsRepo = GameStatsRepository.getInstance(this);
        gameId = UUID.randomUUID().toString();

        category = getIntent().getStringExtra("CATEGORY");
        difficulty = getIntent().getStringExtra("DIFFICULTY");
        mode = getIntent().getStringExtra("MODE");
        isDailyChallenge = getIntent().getBooleanExtra("IS_DAILY", false);
        dailyDate = getIntent().getStringExtra("DAILY_DATE");

        if (category == null) category = "ANIMALS";
        if (difficulty == null) difficulty = "EASY";
        if (mode == null) mode = "CLASSIC";

        // Daily Challenge always uses Medium difficulty
        if (isDailyChallenge) {
            difficulty = "MEDIUM";
        }

        tvGameCategory = findViewById(R.id.tvGameCategory);
        tvGameAttempts = findViewById(R.id.tvGameAttempts);
        tvGameTimer = findViewById(R.id.tvGameTimer);
        tvHiddenWord = findViewById(R.id.tvHiddenWord);
        tvHorrorStatusText = findViewById(R.id.tvHorrorStatusText);
        tvGameHint = findViewById(R.id.tvGameHint);
        horrorEnvironmentView = findViewById(R.id.horrorEnvironmentView);
        vignetteOverlay = findViewById(R.id.vignetteOverlay);
        gameFlickerOverlay = findViewById(R.id.gameFlickerOverlay);
        horrorFrame = findViewById(R.id.horrorFrame);
        glAlphabetKeyboard = findViewById(R.id.glAlphabetKeyboard);
        hangmanView = findViewById(R.id.hangmanView);
        bloodSplashOverlay = findViewById(R.id.bloodSplashOverlay);
        horrorAtmosphereView = findViewById(R.id.horrorAtmosphereView);
        if (horrorAtmosphereView != null) {
            horrorAtmosphereView.start();
        }

        View btnGameBack = findViewById(R.id.btnGameBack);
        if (btnGameBack != null) {
            btnGameBack.setOnClickListener(v -> {
                soundManager.playClick();
                finish();
            });
        }

        setupGame();
        AnimationHelper.enableImmersiveSticky(this);
        soundManager.startAmbientAtmosphere();
        AnimationHelper.startFlickerSystem(gameFlickerOverlay);
        AnimationHelper.fadeIn(findViewById(R.id.gameRootLayout), 1500);
    }

    private void setupGame() {
        soundManager.setDifficulty(difficulty);
        String customWord = getIntent().getStringExtra("TARGET_WORD");
        if (customWord != null && !customWord.trim().isEmpty()) {
            targetWord = customWord.trim().toUpperCase();
        } else {
            targetWord = WordRepository.getRandomWord(category, difficulty);
        }

        displayedWord = new StringBuilder();
        for (int i = 0; i < targetWord.length(); i++) {
            if (targetWord.charAt(i) == ' ') displayedWord.append(" ");
            else displayedWord.append("_");
        }

        String displayDiff = GameStatsRepository.normalizeDifficulty(difficulty);
        if (isDailyChallenge) {
            tvGameCategory.setText("DAILY • " + displayDiff);
        } else {
            tvGameCategory.setText(category.replace("_", " ") + " • " + displayDiff);
        }

        tvHiddenWord.setText(formatWord(displayedWord.toString()));
        updateAttemptsUI();

        // Clue visibility: Only in EASY mode for normal games, never in Medium, Hard, or Daily Challenge
        if ("EASY".equalsIgnoreCase(difficulty) && !isDailyChallenge) {
            tvGameHint.setVisibility(View.VISIBLE);
            tvGameHint.setText("CLUE: " + WordRepository.getHint(category, targetWord));
        } else {
            tvGameHint.setVisibility(View.GONE);
            tvGameHint.setText("");
        }
        
        if (hangmanView != null) {
            hangmanView.setDifficulty(difficulty);
        }
        if (soundManager != null) {
            soundManager.setDifficulty(difficulty);
        }

        if ("TIMER".equals(mode)) {
            startTimeLimit();
        } else {
            tvGameTimer.setVisibility(View.GONE);
        }

        if (bloodSplashOverlay != null) {
            bloodSplashOverlay.clearBlood();
        }

        createKeyboard();
        updateHorrorEnvironment();
    }

    private void startTimeLimit() {
        gameTimer = new CountDownTimer(timeRemainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isGameOver) return;
                timeRemainingMillis = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                tvGameTimer.setText("TIME: " + seconds + "s");

                if (seconds <= 10 && seconds > 5) {
                    tvGameTimer.setTextColor(Color.parseColor("#CC8400"));
                    AnimationHelper.heartbeatPulse(tvGameTimer, 1.05f);
                    soundManager.playHeartbeat(1);
                } else if (seconds <= 5 && seconds > 3) {
                    tvGameTimer.setTextColor(Color.RED);
                    AnimationHelper.heartbeatPulse(tvGameTimer, 1.1f);
                    soundManager.playHeartbeat(2);
                } else if (seconds <= 3 && seconds > 0) {
                    tvGameTimer.setTextColor(Color.RED);
                    AnimationHelper.heartbeatPulse(tvGameTimer, 1.2f);
                    soundManager.playHeartbeat(3);
                    vignetteOverlay.setAlpha(Math.min(1.0f, vignetteOverlay.getAlpha() + 0.05f));
                }
            }

            @Override
            public void onFinish() {
                if (!isGameOver) {
                    tvGameTimer.setText("TIME: 0s");
                    triggerFailure("THE CLOCK STOPPED.");
                }
            }
        }.start();
    }

    private void createKeyboard() {
        glAlphabetKeyboard.removeAllViews();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int btnHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 46, getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());

        for (int i = 0; i < alphabet.length(); i++) {
            final char letter = alphabet.charAt(i);
            int row = i / 7;
            int col = i % 7;

            if (row == 3) {
                if (i == 21) {
                    // Explicit left spacer so V-Z are perfectly centered
                    android.widget.Space leftSpacer = new android.widget.Space(this);
                    GridLayout.LayoutParams spacerParams = new GridLayout.LayoutParams();
                    spacerParams.width = 0;
                    spacerParams.height = btnHeight;
                    spacerParams.rowSpec = GridLayout.spec(3, 1);
                    spacerParams.columnSpec = GridLayout.spec(0, 1, 1f);
                    spacerParams.setMargins(margin, margin, margin, margin);
                    leftSpacer.setLayoutParams(spacerParams);
                    glAlphabetKeyboard.addView(leftSpacer);
                }
                col = (i - 21) + 1;
            }

            Button btn = new Button(this);
            btn.setText(String.valueOf(letter));
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(16f);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
            btn.setBackgroundResource(R.drawable.bg_button_dark);
            btn.setPadding(0, 0, 0, 0);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = btnHeight;
            params.rowSpec = GridLayout.spec(row, 1);
            params.columnSpec = GridLayout.spec(col, 1, 1f);
            params.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                soundManager.playClick();
                handleLetterGuess(letter, btn);
            });
            glAlphabetKeyboard.addView(btn);

            if (i == 25) {
                // Explicit right spacer so row 3 has balanced margins
                android.widget.Space rightSpacer = new android.widget.Space(this);
                GridLayout.LayoutParams spacerParams = new GridLayout.LayoutParams();
                spacerParams.width = 0;
                spacerParams.height = btnHeight;
                spacerParams.rowSpec = GridLayout.spec(3, 1);
                spacerParams.columnSpec = GridLayout.spec(6, 1, 1f);
                spacerParams.setMargins(margin, margin, margin, margin);
                rightSpacer.setLayoutParams(spacerParams);
                glAlphabetKeyboard.addView(rightSpacer);
            }
        }
    }

    private void handleLetterGuess(char letter, Button btn) {
        if (isGameOver || isProcessingGuess) return;
        
        btn.setEnabled(false);
        btn.setAlpha(0.15f);
        guessedLetters.add(letter);

        if (targetWord.contains(String.valueOf(letter))) {
            soundManager.playCorrect();
            for (int i = 0; i < targetWord.length(); i++) {
                if (targetWord.charAt(i) == letter) {
                    displayedWord.setCharAt(i, letter);
                }
            }
            tvHiddenWord.setText(formatWord(displayedWord.toString()));
            AnimationHelper.heartbeatPulse(tvHiddenWord, 1.05f);
            checkWin();
        } else {
            isProcessingGuess = true;

            // 1. IMMEDIATE FEEDBACK (0ms delay): Instant red screen shockwave & progressive redness
            if (bloodSplashOverlay != null) {
                bloodSplashOverlay.triggerRedMistakeFlash(horrorState + 1);
            }
            soundManager.playWrong();
            soundManager.playMistakeSound();
            soundManager.vibrate(120);
            AnimationHelper.shakeView(horrorFrame, horrorState + 1);
            tvHorrorStatusText.setText("WRONG.");
            tvHorrorStatusText.setTextColor(Color.RED);

            final Handler pacedHandler = new Handler(Looper.getMainLooper());

            // 2. Visceral procedural blood splash animated right onto the glass
            pacedHandler.postDelayed(() -> {
                if (bloodSplashOverlay != null) {
                    bloodSplashOverlay.splashBlood(horrorState + 1);
                    soundManager.playBloodSplashSound();
                }
            }, 100);

            // 3. Decrement chances and update horror atmosphere
            pacedHandler.postDelayed(() -> {
                attemptsLeft--;
                horrorState++;
                updateAttemptsUI();

                pacedHandler.postDelayed(() -> {
                    updateHorrorEnvironment();
                    isProcessingGuess = false;
                    checkLoss();
                }, 350);

            }, 300);
        }
    }

    private void updateAttemptsUI() {
        tvGameAttempts.setText("CHANCES: " + attemptsLeft);
        if (attemptsLeft <= 2) {
            tvGameAttempts.setTextColor(Color.RED);
            AnimationHelper.heartbeatPulse(tvGameAttempts, 1.15f);
        }
    }

    private void updateHorrorEnvironment() {
        float vignetteAlpha = 0.4f + (horrorState * 0.1f);
        int baseVal = Math.max(16 - (horrorState * 3), 2);
        int bgColor = Color.rgb(baseVal, baseVal, baseVal);
        
        horrorEnvironmentView.setBackgroundColor(bgColor);
        vignetteOverlay.setAlpha(Math.min(vignetteAlpha, 1.0f));
        tvHorrorStatusText.setTextColor(Color.parseColor("#FF6666"));
        
        if (hangmanView != null) {
            hangmanView.setMistakes(horrorState);
        }

        String msg = "THEY ARE STILL WAITING.";
        String normDiff = GameStatsRepository.normalizeDifficulty(difficulty);
        
        if ("EASY".equals(normDiff)) {
            switch (horrorState) {
                case 1: msg = "The creature stopped moving."; AnimationHelper.glitchView(tvHorrorStatusText); break;
                case 2: msg = "It's watching the dark now."; soundManager.playHeartbeat(1); break;
                case 3: msg = "The air in the cage is cold."; AnimationHelper.environmentalShift(horrorEnvironmentView); break;
                case 4: msg = "Panicked breathing echoes."; soundManager.playHeartbeat(2); break;
                case 5: msg = "One more mistake. It's almost gone."; break;
            }
        } else if ("MEDIUM".equals(normDiff)) {
            switch (horrorState) {
                case 1: msg = "The figure shifted its weight."; AnimationHelper.triggerSingleFlicker(gameFlickerOverlay); break;
                case 2: msg = "Did they just stand up?"; soundManager.playHeartbeat(1); break;
                case 3: msg = "Don't look away. They are closer."; AnimationHelper.environmentalShift(horrorFrame); break;
                case 4: msg = "I can hear them breathing."; AnimationHelper.glitchView(horrorFrame); break;
                case 5: msg = "PLEASE. They are right behind you."; soundManager.playHeartbeat(3); break;
            }
        } else {
            switch (horrorState) {
                case 1: msg = "The night light flickered off."; AnimationHelper.triggerSingleFlicker(gameFlickerOverlay); break;
                case 2: msg = "The lullaby is... wrong."; soundManager.playHeartbeat(1); break;
                case 3: msg = "Baby monitor static is screaming."; AnimationHelper.glitchView(tvHorrorStatusText); break;
                case 4: msg = "The crib is empty shadows."; AnimationHelper.environmentalShift(horrorEnvironmentView); break;
                case 5: msg = "Final error. Silence is worse."; soundManager.playHeartbeat(4); break;
            }
        }

        tvHorrorStatusText.setText(msg);
        AnimationHelper.slowZoom(horrorEnvironmentView, 1.0f + (horrorState * 0.03f), 1.0f + ((horrorState + 1) * 0.03f), 400);
    }

    private String formatWord(String word) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            sb.append(word.charAt(i)).append(" ");
        }
        return sb.toString().trim();
    }

    private void checkWin() {
        if (!displayedWord.toString().contains("_")) {
            isGameOver = true;
            if (gameTimer != null) gameTimer.cancel();

            // Disable all keyboard keys to prevent any further input
            for (int i = 0; i < glAlphabetKeyboard.getChildCount(); i++) {
                glAlphabetKeyboard.getChildAt(i).setEnabled(false);
            }
            glAlphabetKeyboard.animate().alpha(0.3f).setDuration(400).start();

            soundManager.stopAmbientAtmosphere();
            tvHorrorStatusText.setText("THE ROPE SNAPPED. THEY ARE COMING DOWN...");
            tvHorrorStatusText.setTextColor(Color.WHITE);
            AnimationHelper.heartbeatPulse(tvHorrorStatusText, 1.15f);
            
            String normDiff = GameStatsRepository.normalizeDifficulty(difficulty);
            int baseScore = normDiff.equals("EASY") ? 100 : (normDiff.equals("HARD") ? 500 : 250);
            int accuracyBonus = attemptsLeft * 30;
            int timeBonus = mode.equals("TIMER") ? (int)(timeRemainingMillis / 1000) * 15 : 0;
            score = baseScore + accuracyBonus + timeBonus;

            // Authoritative, transaction-safe local persistence
            gameStatsRepo.recordGameResult(gameId, category, normDiff, mode, true, score, isDailyChallenge, dailyDate);
            
            // Execute the realistic hangman descent saving animation
            if (hangmanView != null) {
                hangmanView.startRescueAnimation(() -> {
                    tvHorrorStatusText.setText("THEY ARE SAVED.");
                    soundManager.playVictorySound();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                        intent.putExtra("GAME_ID", gameId);
                        intent.putExtra("WON", true);
                        intent.putExtra("WORD", targetWord);
                        intent.putExtra("SCORE", score);
                        intent.putExtra("BASE", baseScore);
                        intent.putExtra("ACCURACY", accuracyBonus);
                        intent.putExtra("TIME_BONUS", timeBonus);
                        intent.putExtra("CATEGORY", category);
                        intent.putExtra("DIFFICULTY", normDiff);
                        intent.putExtra("IS_DAILY", isDailyChallenge);
                        startActivity(intent);
                        finish();
                    }, 800);
                });
            } else {
                soundManager.playVictorySound();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                    intent.putExtra("GAME_ID", gameId);
                    intent.putExtra("WON", true);
                    intent.putExtra("WORD", targetWord);
                    intent.putExtra("SCORE", score);
                    intent.putExtra("BASE", baseScore);
                    intent.putExtra("ACCURACY", accuracyBonus);
                    intent.putExtra("TIME_BONUS", timeBonus);
                    intent.putExtra("CATEGORY", category);
                    intent.putExtra("DIFFICULTY", normDiff);
                    intent.putExtra("IS_DAILY", isDailyChallenge);
                    startActivity(intent);
                    finish();
                }, 1200);
            }
        }
    }

    private void checkLoss() {
        if (attemptsLeft <= 0 && !isGameOver) {
            triggerFailure("THE CONNECTION SEVERED.");
        }
    }

    private void triggerFailure(String reason) {
        isGameOver = true;
        if (gameTimer != null) gameTimer.cancel();

        for (int i = 0; i < glAlphabetKeyboard.getChildCount(); i++) {
            glAlphabetKeyboard.getChildAt(i).setEnabled(false);
        }
        
        String normDiff = GameStatsRepository.normalizeDifficulty(difficulty);
        // Authoritative, transaction-safe local persistence
        gameStatsRepo.recordGameResult(gameId, category, normDiff, mode, false, 0, isDailyChallenge, dailyDate);
        soundManager.stopAmbientAtmosphere();
        
        glAlphabetKeyboard.setVisibility(View.INVISIBLE);
        
        // Reveal full word in red
        tvHiddenWord.setText(formatWord(targetWord));
        tvHiddenWord.setTextColor(Color.RED);
        
        tvHorrorStatusText.setText(reason);
        tvHorrorStatusText.setTextColor(Color.RED);
        AnimationHelper.heartbeatPulse(tvHorrorStatusText, 1.15f);

        // CINEMATIC DEATH SEQUENCE
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            soundManager.playFailureSound();
            
            AnimationHelper.creatureDeathSequence(horrorFrame, gameFlickerOverlay, tvHorrorStatusText, normDiff, 3500, () -> {
                Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                intent.putExtra("GAME_ID", gameId);
                intent.putExtra("WON", false);
                intent.putExtra("WORD", targetWord);
                intent.putExtra("REASON", reason);
                intent.putExtra("DIFFICULTY", normDiff);
                intent.putExtra("CATEGORY", category);
                intent.putExtra("IS_DAILY", isDailyChallenge);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            });
        }, 1500);
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
        if (horrorAtmosphereView != null) {
            horrorAtmosphereView.start();
        }
        if (!isGameOver && attemptsLeft > 0) {
            soundManager.startAmbientAtmosphere();
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
        if (gameTimer != null) gameTimer.cancel();
        soundManager.stopAmbientAtmosphere();
    }
}
