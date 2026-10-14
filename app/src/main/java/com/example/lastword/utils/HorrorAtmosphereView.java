package com.example.lastword.utils;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Atmospheric horror effects:
 * 1. Menacing, highly visible glowing demonic eyes peering from the dark void,
 *    blinking realistically with eyelids, pupil slits, specular glints, and eerie glowing auras.
 * 2. Realistic silhouetted bats swooping across the screen with flapping membranous wings
 *    and glowing red eye glints.
 */
public class HorrorAtmosphereView extends View {
    private Paint eyeAuraPaint;
    private Paint eyeCorePaint;
    private Paint pupilPaint;
    private Paint glintPaint;
    private Paint batPaint;
    private Paint batEyePaint;

    private List<GlowingEyes> eyesList = new ArrayList<>();
    private List<FlyingBat> batsList = new ArrayList<>();

    private Random random = new Random();
    private long lastEyeSpawnTime = 0;
    private long nextEyeDelay = 1200;
    private long lastBatSpawnTime = 0;
    private long nextBatDelay = 2200;
    private boolean isRunning = true;
    private boolean initialSpawned = false;

    private static class GlowingEyes {
        float x, y;
        float eyeDistance;
        float eyeWidth, eyeHeight;
        float alpha = 0f;
        int state = 0; // 0: fading/opening, 1: staring & blinking, 2: fading/closing
        long stateStartTime;
        long stareDuration;
        long blinkTime;
        int color;
        int innerGlowColor;

        GlowingEyes(float x, float y, float eyeDistance, float eyeWidth, float eyeHeight, int color, int innerGlowColor, Random rand) {
            this.x = x;
            this.y = y;
            this.eyeDistance = eyeDistance;
            this.eyeWidth = eyeWidth;
            this.eyeHeight = eyeHeight;
            this.color = color;
            this.innerGlowColor = innerGlowColor;
            this.stateStartTime = System.currentTimeMillis();
            this.stareDuration = 2400 + rand.nextInt(2600);
            this.blinkTime = 900 + rand.nextInt(1200); // trigger blink during stare
        }
    }

    private static class FlyingBat {
        float x, y;
        float vx, vy;
        float scale;
        float wingCycle = 0f;
        float wingSpeed;
        float flutterPhase = 0f;
        float flutterSpeed;
        float flutterAmp;
        Path batPath = new Path();

        FlyingBat(float startX, float startY, float vx, float vy, float scale, Random rand) {
            this.x = startX;
            this.y = startY;
            this.vx = vx;
            this.vy = vy;
            this.scale = scale;
            this.wingSpeed = 0.28f + rand.nextFloat() * 0.16f;
            this.flutterPhase = rand.nextFloat() * 6.28f;
            this.flutterSpeed = 0.08f + rand.nextFloat() * 0.06f;
            this.flutterAmp = 1.2f + rand.nextFloat() * 1.8f;
        }

        void update() {
            float speed = (float) Math.hypot(vx, vy);
            if (speed > 0.01f) {
                float perpX = -vy / speed;
                float perpY = vx / speed;
                float flutter = (float) Math.sin(flutterPhase) * flutterAmp;
                x += vx + perpX * flutter;
                y += vy + perpY * flutter;
            } else {
                x += vx;
                y += vy;
            }
            flutterPhase += flutterSpeed;
            wingCycle += wingSpeed;
        }
    }

    public HorrorAtmosphereView(Context context) {
        super(context);
        init();
    }

    public HorrorAtmosphereView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorrorAtmosphereView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Demonic mist aura surrounding the eyes
        eyeAuraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeAuraPaint.setStyle(Paint.Style.FILL);
        eyeAuraPaint.setMaskFilter(new BlurMaskFilter(22f, BlurMaskFilter.Blur.NORMAL));

        // Luminous eye iris/sclera
        eyeCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeCorePaint.setStyle(Paint.Style.FILL);

        // Deep vertical slit pupil
        pupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pupilPaint.setStyle(Paint.Style.FILL);
        pupilPaint.setColor(Color.BLACK);

        // Wet specular reflection highlight
        glintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glintPaint.setStyle(Paint.Style.FILL);
        glintPaint.setColor(Color.WHITE);

        // Vampiric bat body silhouette (visibly distinct from pitch black background)
        batPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        batPaint.setStyle(Paint.Style.FILL);
        batPaint.setColor(Color.parseColor("#44161E"));
        batPaint.setShadowLayer(8f, 0, 0, Color.parseColor("#80FF0000"));

        // Glowing red eye points on bats
        batEyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        batEyePaint.setStyle(Paint.Style.FILL);
        batEyePaint.setColor(Color.parseColor("#FF2222"));
    }

    public void start() {
        isRunning = true;
        postInvalidateOnAnimation();
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && !initialSpawned) {
            initialSpawned = true;
            // Spawn 2-3 eye pairs immediately so darkness is alive from frame 1
            for (int i = 0; i < 3; i++) {
                spawnEyes(w, h);
            }
            spawnBatGroup(w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        long now = System.currentTimeMillis();

        // 1. Maintain multiple menacing eye pairs across the screen
        if (now - lastEyeSpawnTime > nextEyeDelay && eyesList.size() < 5) {
            spawnEyes(w, h);
            lastEyeSpawnTime = now;
            nextEyeDelay = 1400 + random.nextInt(1800);
        }

        // 2. Spawn periodic flying bat swarms
        if (now - lastBatSpawnTime > nextBatDelay && batsList.size() < 6) {
            spawnBatGroup(w, h);
            lastBatSpawnTime = now;
            nextBatDelay = 2600 + random.nextInt(3200);
        }

        // 3. Draw and update glowing eyes
        Iterator<GlowingEyes> eyeIter = eyesList.iterator();
        while (eyeIter.hasNext()) {
            GlowingEyes eye = eyeIter.next();
            long elapsed = now - eye.stateStartTime;
            float blinkFactor = 1.0f; // 1.0 = fully open, 0.05 = closed

            if (eye.state == 0) { // Fading in / opening lids
                eye.alpha = Math.min(1.0f, elapsed / 800f);
                if (elapsed >= 800) {
                    eye.state = 1;
                    eye.stateStartTime = now;
                }
            } else if (eye.state == 1) { // Staring intently with occasional realistic blink
                eye.alpha = 1.0f;
                if (elapsed >= eye.blinkTime && elapsed <= eye.blinkTime + 180) {
                    float blinkProgress = (elapsed - eye.blinkTime) / 180f;
                    // V-shaped blink dip (1 -> 0.05 -> 1)
                    blinkFactor = Math.abs(blinkProgress - 0.5f) * 2f;
                    blinkFactor = Math.max(0.08f, blinkFactor);
                }
                if (elapsed > eye.stareDuration) {
                    eye.state = 2;
                    eye.stateStartTime = now;
                }
            } else { // Fading out / dissolving back into void
                eye.alpha = Math.max(0f, 1.0f - (elapsed / 800f));
                if (elapsed >= 800) {
                    eyeIter.remove();
                    continue;
                }
            }

            drawSingleEyePair(canvas, eye, eye.alpha, blinkFactor);
        }

        // 4. Draw and update flying bats
        Iterator<FlyingBat> batIter = batsList.iterator();
        while (batIter.hasNext()) {
            FlyingBat bat = batIter.next();
            bat.update();

            // 360 degree offscreen bounds check
            if (bat.x < -180 || bat.x > w + 180 || bat.y < -180 || bat.y > h + 180) {
                batIter.remove();
                continue;
            }

            drawBat(canvas, bat);
        }

        if (isRunning) {
            postInvalidateOnAnimation();
        }
    }

    private void drawSingleEyePair(Canvas canvas, GlowingEyes eye, float alpha, float blinkFactor) {
        int alphaInt = (int) (alpha * 255);
        if (alphaInt <= 0) return;

        float effectiveH = eye.eyeHeight * blinkFactor;
        if (effectiveH < 2f) effectiveH = 2f;

        float halfDist = eye.eyeDistance / 2f;
        float leftCenterX = eye.x - halfDist;
        float rightCenterX = eye.x + halfDist;
        float centerY = eye.y;

        // A) Draw Demonic Glowing Aura behind both eyes
        eyeAuraPaint.setColor(eye.color);
        eyeAuraPaint.setAlpha((int) (alpha * 180));
        canvas.drawOval(leftCenterX - eye.eyeWidth * 0.75f, centerY - effectiveH * 1.5f,
                leftCenterX + eye.eyeWidth * 0.75f, centerY + effectiveH * 1.5f, eyeAuraPaint);
        canvas.drawOval(rightCenterX - eye.eyeWidth * 0.75f, centerY - effectiveH * 1.5f,
                rightCenterX + eye.eyeWidth * 0.75f, centerY + effectiveH * 1.5f, eyeAuraPaint);

        // B) Draw Crisp Luminous Eyeballs
        eyeCorePaint.setColor(eye.color);
        eyeCorePaint.setAlpha(alphaInt);
        drawAlmondEye(canvas, leftCenterX, centerY, eye.eyeWidth, effectiveH, eyeCorePaint);
        drawAlmondEye(canvas, rightCenterX, centerY, eye.eyeWidth, effectiveH, eyeCorePaint);

        // C) Draw Inner Bright Core Glow
        eyeCorePaint.setColor(eye.innerGlowColor);
        eyeCorePaint.setAlpha((int) (alpha * 220));
        canvas.drawOval(leftCenterX - eye.eyeWidth * 0.35f, centerY - effectiveH * 0.45f,
                leftCenterX + eye.eyeWidth * 0.35f, centerY + effectiveH * 0.45f, eyeCorePaint);
        canvas.drawOval(rightCenterX - eye.eyeWidth * 0.35f, centerY - effectiveH * 0.45f,
                rightCenterX + eye.eyeWidth * 0.35f, centerY + effectiveH * 0.45f, eyeCorePaint);

        // D) Predatory Vertical Slit Pupil
        if (blinkFactor > 0.25f) {
            pupilPaint.setAlpha((int) (alpha * 240));
            float pupilW = Math.max(3f, eye.eyeWidth * 0.16f);
            float pupilH = effectiveH * 0.85f;
            canvas.drawOval(leftCenterX - pupilW / 2f, centerY - pupilH / 2f,
                    leftCenterX + pupilW / 2f, centerY + pupilH / 2f, pupilPaint);
            canvas.drawOval(rightCenterX - pupilW / 2f, centerY - pupilH / 2f,
                    rightCenterX + pupilW / 2f, centerY + pupilH / 2f, pupilPaint);

            // E) Specular Wet Glint
            glintPaint.setAlpha((int) (alpha * 230));
            float glintR = Math.max(2f, eye.eyeWidth * 0.08f);
            canvas.drawCircle(leftCenterX - eye.eyeWidth * 0.18f, centerY - effectiveH * 0.25f, glintR, glintPaint);
            canvas.drawCircle(rightCenterX - eye.eyeWidth * 0.18f, centerY - effectiveH * 0.25f, glintR, glintPaint);
        }
    }

    private void drawAlmondEye(Canvas canvas, float cx, float cy, float w, float h, Paint paint) {
        Path path = new Path();
        float left = cx - w / 2f;
        float right = cx + w / 2f;
        path.moveTo(left, cy);
        path.quadTo(cx, cy - h, right, cy);
        path.quadTo(cx, cy + h, left, cy);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void spawnEyes(int w, int h) {
        // Generous spread across quadrants, including corners and margins
        float x = w * (0.08f + random.nextFloat() * 0.84f);
        float y = h * (0.08f + random.nextFloat() * 0.84f);
        float dist = 55f + random.nextFloat() * 40f;
        float eyeW = 44f + random.nextFloat() * 26f;
        float eyeH = 16f + random.nextFloat() * 12f;

        // Sinister Color palette: Burning Crimson vs Predatory Golden-Amber
        int color;
        int innerGlow;
        if (random.nextBoolean()) {
            color = Color.parseColor("#FF1E1E");
            innerGlow = Color.parseColor("#FFA0A0");
        } else {
            color = Color.parseColor("#FFB000");
            innerGlow = Color.parseColor("#FFE899");
        }

        eyesList.add(new GlowingEyes(x, y, dist, eyeW, eyeH, color, innerGlow, random));
    }

    private void spawnBatGroup(int w, int h) {
        int edge = random.nextInt(4); // 0: Left, 1: Right, 2: Top, 3: Bottom
        float startX, startY;
        float targetX, targetY;

        switch (edge) {
            case 0: // Left edge -> fly across diagonally/right
                startX = -70f;
                startY = h * (0.05f + random.nextFloat() * 0.90f);
                targetX = w + 80f;
                targetY = h * (0.05f + random.nextFloat() * 0.90f);
                break;
            case 1: // Right edge -> fly across diagonally/left
                startX = w + 70f;
                startY = h * (0.05f + random.nextFloat() * 0.90f);
                targetX = -80f;
                targetY = h * (0.05f + random.nextFloat() * 0.90f);
                break;
            case 2: // Top edge -> swoop down diagonally
                startX = w * (0.05f + random.nextFloat() * 0.90f);
                startY = -70f;
                targetX = w * (0.05f + random.nextFloat() * 0.90f);
                targetY = h + 80f;
                break;
            default: // Bottom edge -> climb upwards diagonally
                startX = w * (0.05f + random.nextFloat() * 0.90f);
                startY = h + 70f;
                targetX = w * (0.05f + random.nextFloat() * 0.90f);
                targetY = -80f;
                break;
        }

        float dx = targetX - startX;
        float dy = targetY - startY;
        float dist = (float) Math.hypot(dx, dy);
        float baseSpeed = 8f + random.nextFloat() * 6f;
        float vx = (dx / dist) * baseSpeed;
        float vy = (dy / dist) * baseSpeed;

        int count = 2 + random.nextInt(3); // Swarm of 2-4 bats
        for (int i = 0; i < count; i++) {
            float offsetX = startX - (vx * (i * 3.5f)) + (random.nextFloat() * 40f - 20f);
            float offsetY = startY - (vy * (i * 3.5f)) + (random.nextFloat() * 40f - 20f);
            float scale = 1.1f + random.nextFloat() * 0.65f; // Generously visible wingspan
            batsList.add(new FlyingBat(offsetX, offsetY, vx, vy, scale, random));
        }
    }

    private void drawBat(Canvas canvas, FlyingBat bat) {
        canvas.save();
        canvas.translate(bat.x, bat.y);

        // Dynamically rotate bat body along actual flight direction
        float travelAngle = (float) Math.toDegrees(Math.atan2(bat.vy, bat.vx));
        canvas.rotate(travelAngle + 90f);
        canvas.scale(bat.scale, bat.scale);

        // Sinusoidal wing flap angle (-1 to 1)
        float flap = (float) Math.sin(bat.wingCycle);
        float wingY = flap * 24f;

        bat.batPath.reset();
        // Bat Head & Ears
        bat.batPath.moveTo(0, -6);
        bat.batPath.lineTo(4, -16); // right ear
        bat.batPath.lineTo(8, -6);
        // Right Wing Bone & Membrane
        bat.batPath.quadTo(28, -20 + wingY, 56, -10 + wingY * 1.5f);
        bat.batPath.quadTo(42, 8 + wingY, 32, 14);
        bat.batPath.quadTo(20, 20, 0, 10); // tail/belly
        // Left Wing Bone & Membrane
        bat.batPath.quadTo(-20, 20, -32, 14);
        bat.batPath.quadTo(-42, 8 + wingY, -56, -10 + wingY * 1.5f);
        bat.batPath.quadTo(-28, -20 + wingY, -8, -6);
        bat.batPath.lineTo(-4, -16); // left ear
        bat.batPath.lineTo(0, -6);
        bat.batPath.close();

        canvas.drawPath(bat.batPath, batPaint);

        // Glowing red predator eyes on the bat
        canvas.drawCircle(3, -7, 2.2f, batEyePaint);
        canvas.drawCircle(-3, -7, 2.2f, batEyePaint);

        canvas.restore();
    }
}
