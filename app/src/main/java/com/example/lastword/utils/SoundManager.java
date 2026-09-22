package com.example.lastword.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import com.example.lastword.R;
import com.example.lastword.data.PreferencesManager;

public class SoundManager {
    private final PreferencesManager prefs;
    private final Context context;
    private AudioTrack ambientTrack;
    private boolean isAmbientPlaying = false;
    private Thread ambientThread;
    private String currentDifficulty = "EASY";
    private Handler fadeHandler = new Handler(Looper.getMainLooper());

    private final int[] adultScreams = {
        R.raw.adult_screem_1, R.raw.adult_screem_2, R.raw.adult_screem_3, R.raw.adult_screem_4, R.raw.adult_screem_5
    };
    private final int[] childCries = {
        R.raw.baby_crying_9, R.raw.baby_crying_10
    };
    private final int[] animalWhimpers = {
        R.raw.animal_cying_6, R.raw.animal_cying_7, R.raw.animal_cying_8
    };

    public void setDifficulty(String difficulty) {
        if (difficulty != null) {
            this.currentDifficulty = difficulty;
        }
    }

    public SoundManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new PreferencesManager(context);
    }

    public void vibrate(long duration) {
        if (!prefs.isVibrationEnabled()) return;
        try {
            Vibrator v;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                v = vm.getDefaultVibrator();
            } else {
                v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(duration);
                }
            }
        } catch (SecurityException ignored) {
            // Permission might still be missing in some edge cases or manifest sync issues
        }
    }

    public void playClick() {
        if (!prefs.isSoundEnabled()) return;
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 25);
            tg.startTone(ToneGenerator.TONE_PROP_ACK, 40);
            new Handler(Looper.getMainLooper()).postDelayed(tg::release, 100);
        } catch (Exception ignored) {}
    }

    public void playCorrect() {
        if (!prefs.isSoundEnabled()) return;
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 40);
            tg.startTone(ToneGenerator.TONE_CDMA_PIP, 60);
            new Handler(Looper.getMainLooper()).postDelayed(tg::release, 200);
        } catch (Exception ignored) {}
    }

    public void playWrong() {
        if (!prefs.isSoundEnabled()) return;
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 90);
            tg.startTone(ToneGenerator.TONE_CDMA_LOW_PBX_L, 350);
            new Handler(Looper.getMainLooper()).postDelayed(tg::release, 500);
        } catch (Exception ignored) {}
    }

    public void playHeartbeat(int intensity) {
        if (!prefs.isSoundEnabled()) return;
        try {
            int toneType = ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE;
            if ("HARD".equals(currentDifficulty)) {
                toneType = ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE; // Higher pitch for child
            } else if ("EASY".equals(currentDifficulty)) {
                toneType = ToneGenerator.TONE_CDMA_LOW_PBX_L; // Lower pitch for animal
            }
            final int finalToneType = toneType;

            final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 35 + (intensity * 12));
            tg.startTone(finalToneType, 100);
            vibrate(50);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    tg.startTone(finalToneType, 100);
                    vibrate(40);
                    new Handler(Looper.getMainLooper()).postDelayed(tg::release, 150);
                } catch (Exception ignored) {}
            }, 220);
        } catch (Exception ignored) {}
    }

    public void startAmbientAtmosphere() {
        if (!prefs.isMusicEnabled()) return;
        if (isAmbientPlaying) return;
        isAmbientPlaying = true;

        ambientThread = new Thread(() -> {
            int sampleRate = 8000;
            int numSamples = sampleRate * 5; 
            double[] sample = new double[numSamples];
            byte[] generatedSnd = new byte[2 * numSamples];

            for (int i = 0; i < numSamples; ++i) {
                double t = (double) i / sampleRate;
                
                double baseFreq = 40.0;
                if ("HARD".equals(currentDifficulty)) baseFreq = 60.0; // Higher frequency
                else if ("EASY".equals(currentDifficulty)) baseFreq = 30.0; // Deep growl freq

                double wave = Math.sin(2 * Math.PI * baseFreq * t) * 0.6;
                wave += Math.sin(2 * Math.PI * (baseFreq + 1.2) * t) * 0.4;
                wave += Math.sin(2 * Math.PI * (baseFreq * 2.75) * t) * 0.1 * Math.sin(2 * Math.PI * 0.2 * t);
                
                // Slow volume breathe
                double lfo = 0.7 + 0.3 * Math.sin(2 * Math.PI * 0.15 * t);
                sample[i] = wave * lfo * 0.35;
                
                // Random dust/static
                if (Math.random() > 0.9998) {
                    sample[i] += (Math.random() * 0.5 - 0.25);
                }
            }

            int idx = 0;
            for (final double dVal : sample) {
                final short val = (short) ((dVal * 32767));
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            try {
                ambientTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                        AudioTrack.MODE_STATIC);
                ambientTrack.write(generatedSnd, 0, generatedSnd.length);
                ambientTrack.setLoopPoints(0, numSamples, -1);
                
                if (isAmbientPlaying && ambientTrack != null) {
                    ambientTrack.play();
                }
            } catch (Exception e) {
                isAmbientPlaying = false;
            }
        });
        ambientThread.start();
    }

    public void stopAmbientAtmosphere() {
        isAmbientPlaying = false;
        if (ambientTrack != null) {
            try {
                ambientTrack.stop();
                ambientTrack.release();
            } catch (Exception ignored) {}
            ambientTrack = null;
        }
        if (ambientThread != null) {
            ambientThread.interrupt();
            ambientThread = null;
        }
    }

    private void playFadingSound(int rawResId, float maxVolume) {
        if (rawResId == 0) return;
        try {
            MediaPlayer mp = MediaPlayer.create(context, rawResId);
            if (mp == null) return;
            
            mp.setVolume(0f, 0f);
            mp.start();
            
            long duration = mp.getDuration();
            long fadeInTime = 300;
            long fadeOutTime = 500;
            if (duration < 800) {
                fadeInTime = duration / 2;
                fadeOutTime = duration / 2;
            }
            
            final long finalFadeIn = fadeInTime;
            final long finalFadeOut = fadeOutTime;
            
            android.animation.ValueAnimator fadeIn = android.animation.ValueAnimator.ofFloat(0f, maxVolume);
            fadeIn.setDuration(finalFadeIn);
            fadeIn.addUpdateListener(a -> {
                try {
                    float v = (float) a.getAnimatedValue();
                    mp.setVolume(v, v);
                } catch (Exception ignored) {}
            });
            fadeIn.start();
            
            long fadeOutStart = duration - fadeOutTime;
            fadeHandler.postDelayed(() -> {
                try {
                    if (mp.isPlaying()) {
                        android.animation.ValueAnimator fadeOut = android.animation.ValueAnimator.ofFloat(maxVolume, 0f);
                        fadeOut.setDuration(finalFadeOut);
                        fadeOut.addUpdateListener(a -> {
                            try {
                                float v = (float) a.getAnimatedValue();
                                mp.setVolume(v, v);
                            } catch (Exception ignored) {}
                        });
                        fadeOut.start();
                    }
                } catch (Exception ignored) {}
            }, fadeOutStart);
            
            mp.setOnCompletionListener(MediaPlayer::release);
        } catch (Exception ignored) {}
    }

    public void playMistakeSound() {
        if (!prefs.isSoundEnabled()) return;
        try {
            int rawResId = adultScreams[(int)(Math.random() * adultScreams.length)];
            if ("HARD".equals(currentDifficulty)) {
                rawResId = childCries[(int)(Math.random() * childCries.length)];
            } else if ("EASY".equals(currentDifficulty)) {
                rawResId = animalWhimpers[(int)(Math.random() * animalWhimpers.length)];
            }
            playFadingSound(rawResId, 0.3f); // Lower volume (0.3) for mistake
        } catch (Exception ignored) {}
    }

    public void playFailureSound() {
        stopAmbientAtmosphere();
        if (!prefs.isSoundEnabled()) return;
        try {
            int rawResId = adultScreams[(int)(Math.random() * adultScreams.length)];
            if ("HARD".equals(currentDifficulty)) {
                rawResId = childCries[(int)(Math.random() * childCries.length)];
            } else if ("EASY".equals(currentDifficulty)) {
                rawResId = animalWhimpers[(int)(Math.random() * animalWhimpers.length)];
            }
            playFadingSound(rawResId, 0.6f); // Slightly louder for full failure
            vibrate(1000);
        } catch (Exception ignored) {}
    }

    public void playVictorySound() {
        stopAmbientAtmosphere();
        if (!prefs.isSoundEnabled()) return;
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 50);
            tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 1000);
            new Handler(Looper.getMainLooper()).postDelayed(tg::release, 1200);
        } catch (Exception ignored) {}
    }
}
