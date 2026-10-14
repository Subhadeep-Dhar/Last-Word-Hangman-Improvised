package com.example.lastword.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HangmanView extends View {
    private Paint bloodPaint;
    private Paint spatterPaint;
    private Paint freedPaint;
    private Paint rescuePaint;
    private int mistakes = 0;
    private String difficulty = "EASY";
    private float timeOffset = 0;
    private boolean isFreed = false;
    private float rescueProgress = -1f;
    private ValueAnimator rescueAnimator;
    private List<Spatter> spatters = new ArrayList<>();
    private Random random = new Random();

    private class Spatter {
        Path path;
        float yOffset;
        
        Spatter(float x, float y, float r) {
            path = new Path();
            path.moveTo(x, y - r);
            for (int i = 1; i <= 8; i++) {
                double angle = (i * Math.PI / 4) + (random.nextDouble() - 0.5);
                float dist = r * (0.3f + 0.9f * random.nextFloat());
                float px = x + (float)(Math.cos(angle) * dist);
                float py = y + (float)(Math.sin(angle) * dist);
                path.quadTo(x, y, px, py);
            }
            path.close();
            
            // Add a main drip
            float dripX = x + (random.nextFloat() * r - r/2);
            float dripY = y + r;
            path.moveTo(dripX, dripY);
            path.quadTo(dripX + 2, dripY + r, dripX, dripY + r * 2.5f);
            
            // Add detached high-velocity droplets
            int numDroplets = 2 + random.nextInt(3);
            for (int i = 0; i < numDroplets; i++) {
                float dropAngle = (float) (random.nextDouble() * Math.PI * 2);
                float dropDist = r * (1.5f + random.nextFloat() * 1.5f);
                float dropX = x + (float)(Math.cos(dropAngle) * dropDist);
                float dropY = y + (float)(Math.sin(dropAngle) * dropDist);
                float dropR = r * (0.1f + random.nextFloat() * 0.2f);
                path.addCircle(dropX, dropY, dropR, Path.Direction.CW);
            }
            
            yOffset = 0;
        }
    }

    public HangmanView(Context context) {
        super(context);
        init();
    }

    public HangmanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HangmanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bloodPaint = new Paint();
        bloodPaint.setColor(Color.parseColor("#E60000")); // Brighter horror red
        bloodPaint.setStyle(Paint.Style.STROKE);
        bloodPaint.setStrokeWidth(12f);
        bloodPaint.setStrokeCap(Paint.Cap.ROUND);
        bloodPaint.setStrokeJoin(Paint.Join.ROUND);
        bloodPaint.setAntiAlias(true);
        bloodPaint.setShadowLayer(15f, 0f, 0f, Color.RED);

        spatterPaint = new Paint();
        spatterPaint.setColor(Color.parseColor("#FF0000"));
        spatterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        spatterPaint.setStrokeWidth(4f);
        spatterPaint.setStrokeCap(Paint.Cap.ROUND);
        spatterPaint.setAntiAlias(true);
        spatterPaint.setAlpha(204); // 0.8 opacity
        spatterPaint.setShadowLayer(12f, 0f, 0f, Color.RED);

        freedPaint = new Paint();
        freedPaint.setColor(Color.parseColor("#F5F5F5")); // Relieved pale white with faint aura
        freedPaint.setStyle(Paint.Style.STROKE);
        freedPaint.setStrokeWidth(12f);
        freedPaint.setStrokeCap(Paint.Cap.ROUND);
        freedPaint.setStrokeJoin(Paint.Join.ROUND);
        freedPaint.setAntiAlias(true);
        freedPaint.setShadowLayer(18f, 0f, 0f, Color.parseColor("#E60000"));

        rescuePaint = new Paint();
        rescuePaint.setStyle(Paint.Style.STROKE);
        rescuePaint.setStrokeWidth(12f);
        rescuePaint.setStrokeCap(Paint.Cap.ROUND);
        rescuePaint.setStrokeJoin(Paint.Join.ROUND);
        rescuePaint.setAntiAlias(true);
        rescuePaint.setShadowLayer(16f, 0f, 0f, Color.RED);
    }

    public void setDifficulty(String diff) {
        this.difficulty = diff;
        invalidate();
    }

    public void setFreed(boolean freed) {
        this.isFreed = freed;
        invalidate();
    }

    public boolean isFreed() {
        return this.isFreed;
    }

    public void startRescueAnimation(final Runnable onComplete) {
        if (rescueAnimator != null) {
            rescueAnimator.cancel();
        }
        rescueProgress = 0f;
        rescueAnimator = ValueAnimator.ofFloat(0f, 1f);
        rescueAnimator.setDuration(2600);
        rescueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rescueAnimator.addUpdateListener(animation -> {
            rescueProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        rescueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isFreed = true;
                rescueProgress = -1f;
                invalidate();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        rescueAnimator.start();
    }

    public void resetState() {
        if (rescueAnimator != null) {
            rescueAnimator.cancel();
        }
        this.rescueProgress = -1f;
        this.mistakes = 0;
        this.isFreed = false;
        this.spatters.clear();
        invalidate();
    }

    public void setMistakes(int mistakes) {
        if (!isFreed && mistakes > this.mistakes && mistakes <= 6) {
            // Generate realistic blood spatters using Path
            int numSpatters = 3 + random.nextInt(4);
            for (int i = 0; i < numSpatters; i++) {
                spatters.add(new Spatter(
                        (float)(getWidth() * 0.2 + random.nextFloat() * getWidth() * 0.6),
                        (float)(getHeight() * 0.2 + random.nextFloat() * getHeight() * 0.6),
                        8f + random.nextFloat() * 20f
                ));
            }
        }
        this.mistakes = mistakes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        timeOffset += 0.2f;
        float twitch = 0;
        if (!isFreed && mistakes > 0 && mistakes < 6) {
            twitch = (float) Math.sin(timeOffset) * (mistakes * 1.5f); // Oscillating twitch based on pain
        }

        int width = getWidth();
        int height = getHeight();

        // Draw Scaffold base, pole, top beam
        float padding = 40f;
        float scaffoldBaseY = height - padding;
        float scaffoldTopY = padding;
        float scaffoldLeftX = padding * 2;
        float scaffoldRightX = width - padding * 2;
        float ropeX = width / 2f;
        float ropeBottomY = scaffoldTopY + 80f;

        canvas.drawLine(scaffoldLeftX, scaffoldBaseY, scaffoldRightX, scaffoldBaseY, bloodPaint); // Base
        canvas.drawLine(scaffoldLeftX + 40f, scaffoldBaseY, scaffoldLeftX + 40f, scaffoldTopY, bloodPaint); // Pole
        canvas.drawLine(scaffoldLeftX + 40f, scaffoldTopY, ropeX, scaffoldTopY, bloodPaint); // Top Beam

        // Draw spatters if any
        for (Spatter s : spatters) {
            canvas.save();
            canvas.translate(0, s.yOffset);
            canvas.drawPath(s.path, spatterPaint);
            canvas.restore();
            if (!isFreed) {
                s.yOffset += 0.3f; // Slower, thicker dripping effect
            }
        }

        if (rescueProgress >= 0f) {
            drawRescueSequence(canvas, ropeX, scaffoldTopY, ropeBottomY, scaffoldBaseY, rescueProgress);
        } else if (isFreed) {
            // ROPE IS CUT / SEVERED!
            canvas.drawLine(ropeX, scaffoldTopY, ropeX, scaffoldTopY + 25f, bloodPaint);
            canvas.drawLine(ropeX, scaffoldTopY + 25f, ropeX - 6f, scaffoldTopY + 36f, bloodPaint);
            canvas.drawLine(ropeX, scaffoldTopY + 25f, ropeX + 6f, scaffoldTopY + 36f, bloodPaint);

            // Severed noose fragment fallen on platform
            canvas.drawLine(ropeX + 60f, scaffoldBaseY - 2f, ropeX + 85f, scaffoldBaseY - 12f, bloodPaint);
            canvas.drawCircle(ropeX + 90f, scaffoldBaseY - 12f, 8f, bloodPaint);

            // The character is standing free on the platform (no longer hanging)
            float freedX = ropeX + 15f;
            if ("HARD".equals(difficulty)) {
                drawFreedChild(canvas, freedX, scaffoldBaseY);
            } else {
                drawFreedAdult(canvas, freedX, scaffoldBaseY);
            }
        } else {
            // Normal hanging rope
            canvas.drawLine(ropeX, scaffoldTopY, ropeX, ropeBottomY, bloodPaint);

            if ("HARD".equals(difficulty)) {
                drawChild(canvas, ropeX, ropeBottomY, twitch);
            } else {
                drawAdult(canvas, ropeX, ropeBottomY, twitch);
            }

            // Loop animation if suffering (but not fully dead yet)
            if (mistakes > 0 && mistakes < 6) {
                postInvalidateDelayed(30);
            }
        }
    }

    private void drawRescueSequence(Canvas canvas, float ropeX, float scaffoldTopY, float ropeBottomY, float scaffoldBaseY, float p) {
        // 1. Severed Top Rope with snap recoil
        float snapFraction = Math.min(1f, p / 0.15f);
        canvas.drawLine(ropeX, scaffoldTopY, ropeX, scaffoldTopY + 25f - (4f * snapFraction), bloodPaint);
        canvas.drawLine(ropeX, scaffoldTopY + 25f - (4f * snapFraction), ropeX - 6f, scaffoldTopY + 34f - (4f * snapFraction), bloodPaint);
        canvas.drawLine(ropeX, scaffoldTopY + 25f - (4f * snapFraction), ropeX + 6f, scaffoldTopY + 34f - (4f * snapFraction), bloodPaint);

        // 2. Color transition from trapped horror red to pure relieved white
        int color;
        if (p < 0.45f) {
            color = Color.parseColor("#E60000");
        } else {
            float blend = (p - 0.45f) / 0.55f;
            int r = (int) (0xE6 + (0xFF - 0xE6) * blend);
            int g = (int) (0x00 + (0xFF - 0x00) * blend);
            int b = (int) (0x00 + (0xFF - 0x00) * blend);
            color = Color.rgb(r, g, b);
        }
        rescuePaint.setColor(color);
        if (p > 0.75f) {
            rescuePaint.setShadowLayer(22f, 0f, 0f, Color.WHITE);
        } else {
            rescuePaint.setShadowLayer(15f, 0f, 0f, Color.RED);
        }

        // 3. Falling severed noose
        float nooseGroundY = scaffoldBaseY - 12f;
        float nooseDrop = Math.min(1f, p / 0.50f);
        float nooseY = ropeBottomY + (nooseGroundY - ropeBottomY) * nooseDrop;
        float nooseX = ropeX + (75f * nooseDrop);
        canvas.drawLine(nooseX, nooseY - 14f, nooseX + 18f, nooseY, bloodPaint);
        canvas.drawCircle(nooseX + 22f, nooseY, 8f, bloodPaint);

        // 4. Character descent physics
        if ("HARD".equals(difficulty)) {
            drawRescuingChild(canvas, ropeX, ropeBottomY, scaffoldBaseY, p);
        } else {
            drawRescuingAdult(canvas, ropeX, ropeBottomY, scaffoldBaseY, p);
        }
    }

    private void drawRescuingAdult(Canvas canvas, float ropeX, float ropeBottomY, float scaffoldBaseY, float p) {
        float totalDrop = scaffoldBaseY - (ropeBottomY + 230f);
        float currentDrop;
        if (p < 0.15f) {
            currentDrop = 0f;
        } else if (p < 0.70f) {
            float t = (p - 0.15f) / 0.55f;
            float eased = (float) (1.0 - Math.pow(1.0 - t, 2.0));
            currentDrop = totalDrop * eased;
        } else if (p < 0.85f) {
            float t = (p - 0.70f) / 0.15f;
            float squat = (float) Math.sin(t * Math.PI) * 14f;
            currentDrop = totalDrop + squat;
        } else {
            currentDrop = totalDrop;
        }

        float charX = ropeX + (15f * Math.min(1f, p / 0.70f));
        float headCenterY = (ropeBottomY + 30f) + currentDrop;
        float bodyTopY = headCenterY + 30f;
        float bodyBottomY = bodyTopY + 90f;
        float feetY = bodyBottomY + 80f;

        // Head
        canvas.drawCircle(charX, headCenterY, 28f, rescuePaint);
        // Spine / torso
        canvas.drawLine(charX, bodyTopY, charX, bodyBottomY, rescuePaint);

        // Arms
        float armTopY = bodyTopY + 15f;
        if (p < 0.20f) {
            canvas.drawLine(charX, armTopY, charX - 25f, armTopY + 15f, rescuePaint);
            canvas.drawLine(charX, armTopY, charX + 25f, armTopY + 15f, rescuePaint);
        } else if (p < 0.75f) {
            float armSpread = 32f + 16f * (p / 0.75f);
            canvas.drawLine(charX, armTopY, charX - armSpread, armTopY + 45f, rescuePaint);
            canvas.drawLine(charX, armTopY, charX + armSpread, armTopY + 45f, rescuePaint);
        } else {
            float reliefT = (p - 0.75f) / 0.25f;
            float armEndY = (armTopY + 45f) - (65f * reliefT);
            float armEndX = 32f + (10f * reliefT);
            canvas.drawLine(charX, armTopY, charX - armEndX, armEndY, rescuePaint);
            canvas.drawLine(charX, armTopY, charX + armEndX, armEndY, rescuePaint);
        }

        // Legs
        if (p < 0.70f) {
            canvas.drawLine(charX, bodyBottomY, charX - 25f, feetY, rescuePaint);
            canvas.drawLine(charX, bodyBottomY, charX + 25f, feetY, rescuePaint);
        } else {
            canvas.drawLine(charX, bodyBottomY, charX - 25f, scaffoldBaseY, rescuePaint);
            canvas.drawLine(charX, bodyBottomY, charX + 25f, scaffoldBaseY, rescuePaint);
        }
    }

    private void drawRescuingChild(Canvas canvas, float ropeX, float ropeBottomY, float scaffoldBaseY, float p) {
        float headRadius = 22f;
        float totalDrop = scaffoldBaseY - (ropeBottomY + 139f);
        float currentDrop;
        if (p < 0.15f) {
            currentDrop = 0f;
        } else if (p < 0.70f) {
            float t = (p - 0.15f) / 0.55f;
            float eased = (float) (1.0 - Math.pow(1.0 - t, 2.0));
            currentDrop = totalDrop * eased;
        } else if (p < 0.85f) {
            float t = (p - 0.70f) / 0.15f;
            float squat = (float) Math.sin(t * Math.PI) * 10f;
            currentDrop = totalDrop + squat;
        } else {
            currentDrop = totalDrop;
        }

        float charX = ropeX + (12f * Math.min(1f, p / 0.70f));
        float headCenterY = (ropeBottomY + headRadius) + currentDrop;
        float bodyTopY = headCenterY + headRadius;
        float bodyBottomY = bodyTopY + 50f;
        float feetY = bodyBottomY + 45f;

        // Head
        canvas.drawCircle(charX, headCenterY, headRadius, rescuePaint);
        // Torso
        canvas.drawLine(charX, bodyTopY, charX, bodyBottomY, rescuePaint);

        // Arms
        float armTopY = bodyTopY + 10f;
        if (p < 0.20f) {
            canvas.drawLine(charX, armTopY, charX - 18f, armTopY + 12f, rescuePaint);
            canvas.drawLine(charX, armTopY, charX + 18f, armTopY + 12f, rescuePaint);
        } else if (p < 0.75f) {
            float spread = 22f + 10f * (p / 0.75f);
            canvas.drawLine(charX, armTopY, charX - spread, armTopY + 30f, rescuePaint);
            canvas.drawLine(charX, armTopY, charX + spread, armTopY + 30f, rescuePaint);
        } else {
            float reliefT = (p - 0.75f) / 0.25f;
            float armEndY = (armTopY + 30f) - (45f * reliefT);
            float armEndX = 22f + (8f * reliefT);
            canvas.drawLine(charX, armTopY, charX - armEndX, armEndY, rescuePaint);
            canvas.drawLine(charX, armTopY, charX + armEndX, armEndY, rescuePaint);
        }

        // Legs
        if (p < 0.70f) {
            canvas.drawLine(charX, bodyBottomY, charX - 18f, feetY, rescuePaint);
            canvas.drawLine(charX, bodyBottomY, charX + 18f, feetY, rescuePaint);
        } else {
            canvas.drawLine(charX, bodyBottomY, charX - 18f, scaffoldBaseY, rescuePaint);
            canvas.drawLine(charX, bodyBottomY, charX + 18f, scaffoldBaseY, rescuePaint);
        }
    }

    private void drawFreedAdult(Canvas canvas, float charX, float groundY) {
        // Legs firmly planted on platform
        canvas.drawLine(charX, groundY - 70f, charX - 25f, groundY, freedPaint);
        canvas.drawLine(charX, groundY - 70f, charX + 25f, groundY, freedPaint);

        // Body upright
        canvas.drawLine(charX, groundY - 70f, charX, groundY - 150f, freedPaint);

        // Arms raised slightly in relief / freedom
        canvas.drawLine(charX, groundY - 135f, charX - 42f, groundY - 165f, freedPaint);
        canvas.drawLine(charX, groundY - 135f, charX + 42f, groundY - 165f, freedPaint);

        // Head looking forward, clear of the noose
        canvas.drawCircle(charX, groundY - 175f, 25f, freedPaint);
    }

    private void drawFreedChild(Canvas canvas, float charX, float groundY) {
        // Legs planted on platform
        canvas.drawLine(charX, groundY - 45f, charX - 18f, groundY, freedPaint);
        canvas.drawLine(charX, groundY - 45f, charX + 18f, groundY, freedPaint);

        // Body upright
        canvas.drawLine(charX, groundY - 45f, charX, groundY - 100f, freedPaint);

        // Arms reached out in relief
        canvas.drawLine(charX, groundY - 90f, charX - 30f, groundY - 115f, freedPaint);
        canvas.drawLine(charX, groundY - 90f, charX + 30f, groundY - 115f, freedPaint);

        // Head safe and free
        canvas.drawCircle(charX, groundY - 120f, 18f, freedPaint);
    }

    private void drawAdult(Canvas canvas, float ropeX, float ropeBottomY, float twitch) {
        float headRadius = 30f;
        float headCenterY = ropeBottomY + headRadius;
        if (mistakes >= 1) canvas.drawCircle(ropeX + (twitch * 0.2f), headCenterY, headRadius, bloodPaint);
        
        float bodyTopY = headCenterY + headRadius;
        float bodyBottomY = bodyTopY + 90f;
        if (mistakes >= 2) canvas.drawLine(ropeX, bodyTopY, ropeX + twitch, bodyBottomY, bloodPaint);
        
        float armTopY = bodyTopY + 15f;
        if (mistakes >= 3) canvas.drawLine(ropeX, armTopY, ropeX - 45f + twitch, armTopY + 50f + twitch, bloodPaint);
        if (mistakes >= 4) canvas.drawLine(ropeX, armTopY, ropeX + 45f + twitch, armTopY + 50f - twitch, bloodPaint);
        
        float legEndY = bodyBottomY + 80f;
        if (mistakes >= 5) canvas.drawLine(ropeX + twitch, bodyBottomY, ropeX - 35f + twitch, legEndY, bloodPaint);
        if (mistakes >= 6) canvas.drawLine(ropeX + twitch, bodyBottomY, ropeX + 35f - twitch, legEndY, bloodPaint);
    }

    private void drawChild(Canvas canvas, float ropeX, float ropeBottomY, float twitch) {
        float headRadius = 22f;
        float headCenterY = ropeBottomY + headRadius;
        if (mistakes >= 1) canvas.drawCircle(ropeX + (twitch * 0.3f), headCenterY, headRadius, bloodPaint);
        
        float bodyTopY = headCenterY + headRadius;
        float bodyBottomY = bodyTopY + 50f;
        if (mistakes >= 2) canvas.drawLine(ropeX, bodyTopY, ropeX + twitch, bodyBottomY, bloodPaint);
        
        float armTopY = bodyTopY + 10f;
        if (mistakes >= 3) canvas.drawLine(ropeX, armTopY, ropeX - 30f + twitch, armTopY + 35f + twitch, bloodPaint);
        if (mistakes >= 4) canvas.drawLine(ropeX, armTopY, ropeX + 30f + twitch, armTopY + 35f - twitch, bloodPaint);
        
        float legEndY = bodyBottomY + 45f;
        if (mistakes >= 5) canvas.drawLine(ropeX + twitch, bodyBottomY, ropeX - 25f + twitch, legEndY, bloodPaint);
        if (mistakes >= 6) canvas.drawLine(ropeX + twitch, bodyBottomY, ropeX + 25f - twitch, legEndY, bloodPaint);
    }
}
