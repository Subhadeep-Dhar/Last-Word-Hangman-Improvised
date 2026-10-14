package com.example.lastword;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
            tvGameCategory.setText("DAILY RITUAL: " + category + " / " + displayDiff);
        } else {
            tvGameCategory.setText("DESTINATION: " + category + " / " + displayDiff);
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
                tvGameTimer.setText("REMAINING: " + seconds + "s");

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
                    tvGameTimer.setText("REMAINING: 0s");
                    triggerFailure("THE CLOCK STOPPED.");
                }
            }
        }.start();
    }

    private void createKeyboard() {
        glAlphabetKeyboard.removeAllViews();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < alphabet.length(); i++) {
            final char letter = alphabet.charAt(i);
            Button btn = new Button(this);
            btn.setText(String.valueOf(letter));
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundResource(R.drawable.bg_button_dark);
            btn.setPadding(0, 0, 0, 0);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 120;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(6, 6, 6, 6);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> handleLetterGuess(letter, btn));
            glAlphabetKeyboard.addView(btn);
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
            final Handler pacedHandler = new Handler(Looper.getMainLooper());
            
            pacedHandler.postDelayed(() -> {
                tvHorrorStatusText.setText("WRONG.");
                tvHorrorStatusText.setTextColor(Color.RED);
                
                pacedHandler.postDelayed(() -> {
                    soundManager.playWrong();
                    soundManager.playMistakeSound();
                    AnimationHelper.shakeView(horrorFrame, horrorState + 1);
                    AnimationHelper.triggerSingleFlicker(gameFlickerOverlay);
                    soundManager.vibrate(100);

                    pacedHandler.postDelayed(() -> {
                        attemptsLeft--;
                        horrorState++;
                        updateAttemptsUI();
                        
                        pacedHandler.postDelayed(() -> {
                            updateHorrorEnvironment();
                            isProcessingGuess = false;
                            checkLoss();
                        }, 500);

                    }, 400);

                }, 400);

            }, 300);
        }
    }

    private void updateAttemptsUI() {
        tvGameAttempts.setText("TETHER: " + attemptsLeft);
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
        tvHorrorStatusText.setTextColor(Color.parseColor("#888888"));
        
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

            // Transition the Hangman character into the rescued/freed state
            if (hangmanView != null) {
                hangmanView.setFreed(true);
            }

            soundManager.stopAmbientAtmosphere();
            tvHorrorStatusText.setText("THE ROPE SNAPPED. THEY ARE FREE.");
            tvHorrorStatusText.setTextColor(Color.WHITE);
            AnimationHelper.heartbeatPulse(tvHorrorStatusText, 1.1f);
            
            String normDiff = GameStatsRepository.normalizeDifficulty(difficulty);
            int baseScore = normDiff.equals("EASY") ? 100 : (normDiff.equals("HARD") ? 500 : 250);
            int accuracyBonus = attemptsLeft * 30;
            int timeBonus = mode.equals("TIMER") ? (int)(timeRemainingMillis / 1000) * 15 : 0;
            score = baseScore + accuracyBonus + timeBonus;

            // Authoritative, transaction-safe local persistence
            gameStatsRepo.recordGameResult(gameId, category, normDiff, mode, true, score, isDailyChallenge, dailyDate);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                soundManager.playVictorySound();
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
        if (!isGameOver && attemptsLeft > 0) {
            soundManager.startAmbientAtmosphere();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        soundManager.stopAmbientAtmosphere();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) gameTimer.cancel();
        soundManager.stopAmbientAtmosphere();
    }
}
