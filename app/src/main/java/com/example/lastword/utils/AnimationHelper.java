package com.example.lastword.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

public class AnimationHelper {

    public static void shakeView(View view) {
        shakeView(view, 1);
    }

    public static void shakeView(View view, int intensity) {
        if (view == null) return;
        float offset = 12f * intensity;
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, -offset, offset, -offset, offset, -offset / 2, offset / 2, 0);
        shake.setDuration(150 + (intensity * 40));
        shake.setInterpolator(new AccelerateDecelerateInterpolator());
        shake.start();
    }

    public static void heartbeatPulse(View view, float scale) {
        if (view == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, scale, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, scale, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(450);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    public static void startFlickerSystem(final View overlayView) {
        if (overlayView == null) return;
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (Math.random() > 0.7) {
                    triggerSingleFlicker(overlayView);
                }
                handler.postDelayed(this, 1500 + (long) (Math.random() * 4500));
            }
        };
        handler.post(runnable);
    }

    public static void triggerSingleFlicker(final View view) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.2f + (float) Math.random() * 0.5f);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            view.setAlpha(0f);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Math.random() > 0.6) {
                    view.setAlpha(0.3f);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        view.setAlpha(0f);
                        view.setVisibility(View.GONE);
                    }, 60);
                } else {
                    view.setVisibility(View.GONE);
                }
            }, 200);
        }, 60);
    }

    public static void glitchView(final View view) {
        if (view == null) return;
        final float originalX = view.getTranslationX();
        final float originalAlpha = view.getAlpha();

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            if (Math.random() > 0.7) {
                view.setTranslationX(originalX + (float) (Math.random() * 40 - 20));
                view.setAlpha((float) Math.random());
            } else {
                view.setTranslationX(originalX);
                view.setAlpha(originalAlpha);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setTranslationX(originalX);
                view.setAlpha(originalAlpha);
            }
        });
        animator.start();
    }

    /**
     * Cinematic Death Sequence: High-intensity visual feedback for the creature's demise
     */
    public static void creatureDeathSequence(final View viewport, final View globalFlicker, final TextView statusText, String difficulty, long duration, Runnable onEnd) {
        if (viewport == null) return;
        
        // 1. Initial extreme spasms and "Blood" wash
        violentGlitch(viewport, (long) (duration * 0.6));
        shakeView(viewport, 5);

        int deathColor;
        if ("EASY".equals(difficulty)) {
            if (statusText != null) statusText.setText("THE CREATURE PERISHED.");
            deathColor = Color.parseColor("#770000"); // Blood red
        } else if ("INTERMEDIATE".equals(difficulty)) {
            if (statusText != null) statusText.setText("THE SUBJECT HAS CEASED.");
            deathColor = Color.parseColor("#444444"); // Shadowy gray
        } else {
            if (statusText != null) statusText.setText("CONNECTION SEVERED PERMANENTLY.");
            deathColor = Color.WHITE; // Static white burst
        }
        
        ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), Color.TRANSPARENT, deathColor, Color.BLACK);
        colorAnim.setDuration(duration);
        colorAnim.addUpdateListener(animation -> viewport.setBackgroundColor((int) animation.getAnimatedValue()));
        colorAnim.start();

        // 2. Collapse animation: Scale down to nothing
        viewport.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .rotation((float) (Math.random() * 60 - 30))
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .setStartDelay(duration / 10)
                .withEndAction(onEnd)
                .start();
        
        // 3. Global distress feedback
        if (globalFlicker != null) {
            final Handler handler = new Handler(Looper.getMainLooper());
            for (int i = 0; i < 5; i++) {
                handler.postDelayed(() -> triggerSingleFlicker(globalFlicker), i * (duration / 5));
            }
        }
    }

    public static void violentGlitch(final View view, long duration) {
        if (view == null) return;
        final float originalX = view.getTranslationX();
        final float originalY = view.getTranslationY();
        
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            view.setTranslationX(originalX + (float) (Math.random() * 120 - 60));
            view.setTranslationY(originalY + (float) (Math.random() * 120 - 60));
            view.setAlpha((float) Math.random());
            if (Math.random() > 0.5) view.setScaleX(1.5f); else view.setScaleX(0.5f);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setTranslationX(originalX);
                view.setTranslationY(originalY);
                view.setAlpha(0f);
                view.setScaleX(1f);
            }
        });
        animator.start();
    }

    public static void slowZoom(View view, float startScale, float endScale, long duration) {
        if (view == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", startScale, endScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", startScale, endScale);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(duration);
        set.setInterpolator(new LinearInterpolator());
        set.start();
    }

    public static void environmentalShift(View view) {
        if (view == null) return;
        float moveX = (float) (Math.random() * 60 - 30);
        float moveY = (float) (Math.random() * 40 - 20);
        view.animate()
                .translationX(moveX)
                .translationY(moveY)
                .setDuration(6000)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void fadeOut(View view, long duration, Runnable onEnd) {
        if (view == null) return;
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(onEnd)
                .start();
    }

    public static void fadeIn(View view, long duration) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .start();
    }
}
