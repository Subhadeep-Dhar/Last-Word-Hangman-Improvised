package com.example.lastword.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 100% Procedural Liquid Blood Splatter, Screen-Length Dripping, and Red Mistake Flash Engine.
 * - Zero static images: Splashes and rivulets are mathematically simulated on Canvas with fluid Bezier curves.
 * - Dynamic Splashing Animation: Blood impacts and bursts outward in real time (tendrils, satellite droplets).
 * - Low Translucent Opacity: Gameplay content (word blanks, clues, keyboard keys) remains 100% visible beneath.
 * - Red Mistake Effect: Intense full-screen crimson shockwave and radial flash when wrong letters are spelled.
 * - Slow Screen-Length Dripping: Viscous streams crawl slowly all the way past the bottom of the screen.
 */
public class BloodSplashOverlayView extends View {

    private Paint arterialPaint;    // Outer rich liquid crimson (#A80505)
    private Paint clotPaint;        // Inner dark clotted venous core (#580000)
    private Paint highlightPaint;   // Wet specular gloss highlights (#80FFFFFF)
    private Paint redFlashPaint;    // Full-screen red mistake shockwave & vignette
    private Paint dripArterialPaint;
    private Paint dripClotPaint;
    private Paint dripHighlightPaint;

    private List<ProceduralSplatter> splatters = new ArrayList<>();
    private List<RealisticDrip> drips = new ArrayList<>();

    private float redFlashAlpha = 0f;
    private float persistentRedAlpha = 0f;
    private boolean isBackgroundAtmosphere = false;
    private boolean isFailureMode = false;
    private float globalOpacity = 0.38f; // Low translucent opacity so content beneath is clearly visible
    private Random random = new Random();
    private ValueAnimator dripLoopAnimator;
    private ValueAnimator redFlashAnimator;

    private static class Tendril {
        float angle;
        float length;
        float baseWidth;
        float headRadius;
        float curve;

        Tendril(float angle, float length, float baseWidth, float headRadius, float curve) {
            this.angle = angle;
            this.length = length;
            this.baseWidth = baseWidth;
            this.headRadius = headRadius;
            this.curve = curve;
        }
    }

    private static class Droplet {
        float angle;
        float distFactor;
        float radius;

        Droplet(float angle, float distFactor, float radius) {
            this.angle = angle;
            this.distFactor = distFactor;
            this.radius = radius;
        }
    }

    private static class Glint {
        float angle;
        float dist;
        float radius;

        Glint(float angle, float dist, float radius) {
            this.angle = angle;
            this.dist = dist;
            this.radius = radius;
        }
    }

    private static class ProceduralSplatter {
        float cx, cy;
        float baseRadius;
        float animProgress; // 0.05f -> 1.0f (dynamic animated splash burst)
        float opacity;

        float[] angles;
        float[] radii;

        List<Tendril> tendrils = new ArrayList<>();
        List<Droplet> droplets = new ArrayList<>();
        List<Glint> glints = new ArrayList<>();

        ProceduralSplatter(float cx, float cy, float baseRadius, float opacity, Random rand) {
            this.cx = cx;
            this.cy = cy;
            this.baseRadius = baseRadius;
            this.animProgress = 0.05f;
            this.opacity = opacity;
            generateGeometry(rand);
        }

        private void generateGeometry(Random rand) {
            int numPoints = 16;
            angles = new float[numPoints];
            radii = new float[numPoints];
            for (int i = 0; i < numPoints; i++) {
                float baseAngle = (float) (i * 2 * Math.PI / numPoints);
                angles[i] = baseAngle + (rand.nextFloat() * 0.16f - 0.08f);
                radii[i] = 0.60f + rand.nextFloat() * 0.55f;
            }

            int tendrilCount = 6 + rand.nextInt(4);
            for (int i = 0; i < tendrilCount; i++) {
                float angle = (float) (i * 2 * Math.PI / tendrilCount) + (rand.nextFloat() * 0.4f - 0.2f);
                float length = 1.5f + rand.nextFloat() * 1.6f;
                float baseW = 0.16f + rand.nextFloat() * 0.10f;
                float headR = 0.07f + rand.nextFloat() * 0.08f;
                float curve = rand.nextFloat() * 0.25f - 0.125f;
                tendrils.add(new Tendril(angle, length, baseW, headR, curve));
            }

            int dropCount = 14 + rand.nextInt(8);
            for (int i = 0; i < dropCount; i++) {
                float angle = (float) (rand.nextFloat() * 2 * Math.PI);
                float dist = 1.3f + rand.nextFloat() * 2.2f;
                float r = 1.8f + rand.nextFloat() * 3.2f;
                droplets.add(new Droplet(angle, dist, r));
            }

            int glintCount = 3 + rand.nextInt(3);
            for (int i = 0; i < glintCount; i++) {
                float gAngle = (float) (rand.nextFloat() * 2 * Math.PI);
                float gDist = (0.2f + rand.nextFloat() * 0.45f) * baseRadius;
                glints.add(new Glint(gAngle, gDist, 1.6f + rand.nextFloat() * 2.0f));
            }
        }
    }

    private static class RealisticDrip {
        float startX, startY;
        float currentY;
        float streamWidth;
        float maxDropY;
        float crawlSpeed;
        float meanderFreq;
        float meanderAmp;
        float opacity;
        List<FallingBead> trailingBeads = new ArrayList<>();

        RealisticDrip(float startX, float startY, float streamWidth, float maxDropY, float crawlSpeed, float opacity, Random rand) {
            this.startX = startX;
            this.startY = startY;
            this.currentY = startY;
            this.streamWidth = streamWidth;
            this.maxDropY = maxDropY;
            this.crawlSpeed = crawlSpeed;
            this.opacity = opacity;
            this.meanderFreq = 0.015f + rand.nextFloat() * 0.015f;
            this.meanderAmp = 2.5f + rand.nextFloat() * 3.5f;

            int beadCount = 1 + rand.nextInt(3);
            for (int b = 0; b < beadCount; b++) {
                trailingBeads.add(new FallingBead(25f + b * 35f + rand.nextFloat() * 15f, 2.5f + rand.nextFloat() * 2f));
            }
        }
    }

    private static class FallingBead {
        float lagOffset;
        float radius;

        FallingBead(float lagOffset, float radius) {
            this.lagOffset = lagOffset;
            this.radius = radius;
        }
    }

    public BloodSplashOverlayView(Context context) {
        super(context);
        init();
    }

    public BloodSplashOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BloodSplashOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arterialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arterialPaint.setColor(Color.parseColor("#9E0505"));
        arterialPaint.setStyle(Paint.Style.FILL);

        clotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clotPaint.setColor(Color.parseColor("#5A0000"));
        clotPaint.setStyle(Paint.Style.FILL);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#70FFFFFF"));
        highlightPaint.setStyle(Paint.Style.FILL);

        redFlashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redFlashPaint.setStyle(Paint.Style.FILL);

        dripArterialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dripArterialPaint.setColor(Color.parseColor("#9E0505"));
        dripArterialPaint.setStyle(Paint.Style.STROKE);
        dripArterialPaint.setStrokeCap(Paint.Cap.ROUND);

        dripClotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dripClotPaint.setColor(Color.parseColor("#5A0000"));
        dripClotPaint.setStyle(Paint.Style.STROKE);
        dripClotPaint.setStrokeCap(Paint.Cap.ROUND);

        dripHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dripHighlightPaint.setColor(Color.parseColor("#80FFFFFF"));
        dripHighlightPaint.setStyle(Paint.Style.STROKE);
        dripHighlightPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Triggers the intense Red Mistake Screen Flash when a wrong letter is guessed.
     * Instantly floods the screen with a crimson radial shockwave and decays into
     * an escalating persistent red ambient glow that deepens with each mistake.
     */
    public void triggerRedMistakeFlash() {
        triggerRedMistakeFlash(1);
    }

    public void triggerRedMistakeFlash(int mistakeCount) {
        setVisibility(VISIBLE);
        setAlpha(1.0f);

        // Persistent ambient redness that deepens with each wrong guess (up to 40% intensity)
        persistentRedAlpha = Math.min(0.40f, 0.08f * Math.max(1, mistakeCount));

        if (redFlashAnimator != null) redFlashAnimator.cancel();
        // High-visibility red flash peak (up to 0.90)
        redFlashAlpha = Math.min(0.90f, 0.68f + 0.04f * mistakeCount);
        invalidate();

        redFlashAnimator = ValueAnimator.ofFloat(redFlashAlpha, persistentRedAlpha);
        redFlashAnimator.setDuration(480);
        redFlashAnimator.setInterpolator(new DecelerateInterpolator());
        redFlashAnimator.addUpdateListener(anim -> {
            redFlashAlpha = (float) anim.getAnimatedValue();
            invalidate();
        });
        redFlashAnimator.start();
    }

    public void setAsBackgroundAtmosphere(boolean enable) {
        this.isBackgroundAtmosphere = enable;
        this.isFailureMode = false;
        this.globalOpacity = 0.50f;

        if (enable) {
            setVisibility(VISIBLE);
            setAlpha(1.0f);
            if (getWidth() > 0 && getHeight() > 0) {
                setupAtmosphericBloods(getWidth(), getHeight());
                startContinuousDripLoop();
            } else {
                post(() -> {
                    if (getWidth() > 0 && getHeight() > 0) {
                        setupAtmosphericBloods(getWidth(), getHeight());
                        startContinuousDripLoop();
                    }
                });
            }
        } else {
            if (dripLoopAnimator != null) dripLoopAnimator.cancel();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (isBackgroundAtmosphere && w > 0 && h > 0 && splatters.isEmpty()) {
            setupAtmosphericBloods(w, h);
            startContinuousDripLoop();
        }
    }

    private void setupAtmosphericBloods(int w, int h) {
        splatters.clear();
        drips.clear();

        int count = 4 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            float x = w * (0.08f + random.nextFloat() * 0.84f);
            float y = h * (0.05f + random.nextFloat() * 0.70f);
            float baseRadius = 38f + random.nextFloat() * 32f;

            ProceduralSplatter s = new ProceduralSplatter(x, y, baseRadius, 0.45f, random);
            s.animProgress = 1.0f; // Static atmospheric splashes
            splatters.add(s);

            // Drips flowing all the way down past the bottom of the screen
            int streamCount = 1 + random.nextInt(2);
            for (int sc = 0; sc < streamCount; sc++) {
                float ox = x + (random.nextFloat() * 30f - 15f);
                float oy = y + 20f + random.nextFloat() * 20f;
                float maxDropY = h + 160f; // Extends past screen bottom
                float crawlSpeed = 0.8f + random.nextFloat() * 1.3f;
                float streamW = 4f + random.nextFloat() * 3.5f;
                RealisticDrip d = new RealisticDrip(ox, oy, streamW, maxDropY, crawlSpeed, 0.45f, random);
                d.currentY = oy + random.nextFloat() * (h * 0.3f);
                drips.add(d);
            }
        }
    }

    /**
     * Wrong guess in-game splatter:
     * 1. Fires full-screen Red Mistake Flash.
     * 2. Dynamically animates procedural blood splash hitting and bursting outwards onto the screen.
     * 3. Keeps opacity low (0.38f) so word blanks and keyboard keys beneath remain 100% visible.
     * 4. Rivulets crawl slowly all the way past the bottom of the screen.
     */
    public void splashBlood(int intensity) {
        this.isFailureMode = false;
        this.globalOpacity = 0.38f; // Low translucent opacity so player clearly sees words and keys
        setVisibility(VISIBLE);
        setAlpha(1.0f);

        // Instant red shockwave pulse on wrong spell
        triggerRedMistakeFlash();

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            post(() -> splashBlood(intensity));
            return;
        }

        // Keep active count lightweight and readable
        if (splatters.size() > 5) splatters.subList(0, splatters.size() - 3).clear();
        if (drips.size() > 8) drips.subList(0, drips.size() - 5).clear();

        int count = 1 + Math.min(2, intensity / 2);
        for (int i = 0; i < count; i++) {
            // Distribute hits around screen margins/edges so center word and keyboard keys remain unobscured
            float x, y;
            if (random.nextBoolean()) {
                x = w * (random.nextBoolean() ? 0.10f + random.nextFloat() * 0.18f : 0.72f + random.nextFloat() * 0.18f);
                y = h * (0.08f + random.nextFloat() * 0.65f);
            } else {
                x = w * (0.15f + random.nextFloat() * 0.70f);
                y = h * (random.nextBoolean() ? 0.08f + random.nextFloat() * 0.16f : 0.65f + random.nextFloat() * 0.18f);
            }

            float baseRadius = 45f + random.nextFloat() * 30f + (intensity * 4f);
            ProceduralSplatter s = new ProceduralSplatter(x, y, baseRadius, 0.40f, random);
            splatters.add(s);

            // Real-time splashing animation: fluid impacts and bursts outward (0.05 -> 1.0)
            ValueAnimator splatAnim = ValueAnimator.ofFloat(0.05f, 1.0f);
            splatAnim.setDuration(260);
            splatAnim.setInterpolator(new OvershootInterpolator(1.25f));
            splatAnim.addUpdateListener(anim -> {
                s.animProgress = (float) anim.getAnimatedValue();
                invalidate();
            });
            splatAnim.start();

            // Viscous rivulets creeping slowly down till the very end of the screen (and beyond)
            int dripCount = 1 + random.nextInt(2);
            for (int dc = 0; dc < dripCount; dc++) {
                float ox = x + (random.nextFloat() * 24f - 12f);
                float oy = y + 16f + random.nextFloat() * 16f;
                float maxDropY = h + 180f; // All the way down past the display edge
                float crawlSpeed = 0.9f + random.nextFloat() * 1.4f; // Slow, viscous crawl
                float streamW = 4f + random.nextFloat() * 3f;
                drips.add(new RealisticDrip(ox, oy, streamW, maxDropY, crawlSpeed, 0.40f, random));
            }
        }

        startContinuousDripLoop();
    }

    /**
     * Failure Camera Burst:
     * Full-screen camera lens explosion with intense red shockwave and heavy blood running down past screen bottom.
     */
    public void splashTowardsCamera() {
        this.isFailureMode = true;
        this.globalOpacity = 0.38f; // Lower translucent opacity matching in-game so TOO LATE & Word remain clearly visible
        setVisibility(VISIBLE);
        setAlpha(1.0f);
        bringToFront();

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            post(this::splashTowardsCamera);
            return;
        }

        splatters.clear();
        drips.clear();

        // Atmospheric camera hits with low opacity
        int count = 5;
        for (int i = 0; i < count; i++) {
            float x = w * (0.10f + random.nextFloat() * 0.80f);
            float y = h * (0.08f + random.nextFloat() * 0.72f);
            float baseRadius = 55f + random.nextFloat() * 38f;

            ProceduralSplatter s = new ProceduralSplatter(x, y, baseRadius, 0.40f, random);
            splatters.add(s);

            ValueAnimator splatAnim = ValueAnimator.ofFloat(0.05f, 1.0f);
            splatAnim.setDuration(280 + i * 35);
            splatAnim.setInterpolator(new OvershootInterpolator(1.25f));
            splatAnim.addUpdateListener(anim -> {
                s.animProgress = (float) anim.getAnimatedValue();
                invalidate();
            });
            splatAnim.start();

            int dripCount = 2 + random.nextInt(2);
            for (int dc = 0; dc < dripCount; dc++) {
                float ox = x + (random.nextFloat() * 36f - 18f);
                float oy = y + 20f + random.nextFloat() * 20f;
                float maxDropY = h + 200f;
                float crawlSpeed = 1.1f + random.nextFloat() * 1.8f;
                float streamW = 5f + random.nextFloat() * 4f;
                drips.add(new RealisticDrip(ox, oy, streamW, maxDropY, crawlSpeed, 0.40f, random));
            }
        }

        triggerRedMistakeFlash();
        startContinuousDripLoop();
    }

    private void startContinuousDripLoop() {
        if (dripLoopAnimator != null && dripLoopAnimator.isRunning()) return;

        dripLoopAnimator = ValueAnimator.ofFloat(0f, 1f);
        dripLoopAnimator.setDuration(16);
        dripLoopAnimator.setRepeatCount(ValueAnimator.INFINITE);
        dripLoopAnimator.addUpdateListener(anim -> {
            boolean active = false;
            for (RealisticDrip drip : drips) {
                if (drip.currentY < drip.maxDropY) {
                    drip.currentY += drip.crawlSpeed;
                    active = true;
                }
            }

            if (active) {
                invalidate();
            } else if (!isBackgroundAtmosphere) {
                dripLoopAnimator.cancel();
            }
        });
        dripLoopAnimator.start();
    }

    public void clearBlood() {
        if (dripLoopAnimator != null) dripLoopAnimator.cancel();
        if (redFlashAnimator != null) redFlashAnimator.cancel();
        splatters.clear();
        drips.clear();
        redFlashAlpha = 0f;
        persistentRedAlpha = 0f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. High-Impact Red Mistake Shockwave & Persistent Red Screen Vignette
        float currentRed = Math.max(redFlashAlpha, persistentRedAlpha);
        if (currentRed > 0.01f) {
            int flashAlphaInt = (int) (currentRed * 255);
            redFlashPaint.setShader(new RadialGradient(
                    w / 2f, h / 2f, Math.max(w, h) * 0.72f,
                    Color.argb((int) (flashAlphaInt * 0.32f), 220, 0, 0),
                    Color.argb(flashAlphaInt, 180, 0, 0),
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h, redFlashPaint);
        }

        // 2. Procedural Fluid Blood Splatters with Dynamic Splashing Animation
        for (ProceduralSplatter s : splatters) {
            drawProceduralSplatter(canvas, s);
        }

        // 3. Procedural Fluid Drips Crawling All the Way Down Past Screen Bottom
        for (RealisticDrip drip : drips) {
            drawRealisticDrip(canvas, drip);
        }
    }

    private void drawProceduralSplatter(Canvas canvas, ProceduralSplatter s) {
        float currentR = s.baseRadius * s.animProgress;
        if (currentR < 2f) return;

        int alphaInt = (int) (s.opacity * 255);
        arterialPaint.setAlpha(alphaInt);
        clotPaint.setAlpha((int) (alphaInt * 0.85f));
        highlightPaint.setAlpha((int) (alphaInt * 0.65f));

        // A) Central Multi-Pointed Organic Fluid Pool (Smooth cubic Bezier)
        Path poolPath = new Path();
        int n = s.angles.length;
        float[] px = new float[n];
        float[] py = new float[n];
        for (int i = 0; i < n; i++) {
            float dist = s.radii[i] * currentR;
            px[i] = s.cx + (float) Math.cos(s.angles[i]) * dist;
            py[i] = s.cy + (float) Math.sin(s.angles[i]) * dist;
        }

        poolPath.moveTo((px[0] + px[n - 1]) / 2f, (py[0] + py[n - 1]) / 2f);
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            poolPath.quadTo(px[i], py[i], (px[i] + px[next]) / 2f, (py[i] + py[next]) / 2f);
        }
        poolPath.close();
        canvas.drawPath(poolPath, arterialPaint);

        // Inner dark clotted venous core
        Path corePath = new Path();
        float coreR = currentR * 0.62f;
        float[] cpx = new float[n];
        float[] cpy = new float[n];
        for (int i = 0; i < n; i++) {
            float dist = s.radii[i] * coreR;
            cpx[i] = s.cx + (float) Math.cos(s.angles[i]) * dist;
            cpy[i] = s.cy + (float) Math.sin(s.angles[i]) * dist;
        }
        corePath.moveTo((cpx[0] + cpx[n - 1]) / 2f, (cpy[0] + cpy[n - 1]) / 2f);
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            corePath.quadTo(cpx[i], cpy[i], (cpx[i] + cpx[next]) / 2f, (cpy[i] + cpy[next]) / 2f);
        }
        corePath.close();
        canvas.drawPath(corePath, clotPaint);

        // B) Radiating Spikes and Tendrils Shooting Outward
        for (Tendril t : s.tendrils) {
            float tLen = t.length * currentR;
            float rootDist = currentR * 0.75f;
            float tipAngle = t.angle + t.curve;

            float tipX = s.cx + (float) Math.cos(tipAngle) * tLen;
            float tipY = s.cy + (float) Math.sin(tipAngle) * tLen;

            float perpAngle = t.angle + (float) (Math.PI / 2.0);
            float halfBase = t.baseWidth * currentR;
            float leftBaseX = s.cx + (float) Math.cos(t.angle) * rootDist + (float) Math.cos(perpAngle) * halfBase;
            float leftBaseY = s.cy + (float) Math.sin(t.angle) * rootDist + (float) Math.sin(perpAngle) * halfBase;
            float rightBaseX = s.cx + (float) Math.cos(t.angle) * rootDist - (float) Math.cos(perpAngle) * halfBase;
            float rightBaseY = s.cy + (float) Math.sin(t.angle) * rootDist - (float) Math.sin(perpAngle) * halfBase;

            Path tendrilPath = new Path();
            tendrilPath.moveTo(leftBaseX, leftBaseY);
            tendrilPath.quadTo(
                    s.cx + (float) Math.cos(t.angle + t.curve * 0.5f) * (tLen * 0.6f),
                    s.cy + (float) Math.sin(t.angle + t.curve * 0.5f) * (tLen * 0.6f),
                    tipX, tipY
            );
            tendrilPath.quadTo(
                    s.cx + (float) Math.cos(t.angle + t.curve * 0.5f) * (tLen * 0.5f),
                    s.cy + (float) Math.sin(t.angle + t.curve * 0.5f) * (tLen * 0.5f),
                    rightBaseX, rightBaseY
            );
            tendrilPath.close();
            canvas.drawPath(tendrilPath, arterialPaint);

            // Teardrop droplet head at tip of tendril
            float headR = t.headRadius * currentR;
            if (headR > 1.5f) {
                canvas.drawCircle(tipX, tipY, headR, arterialPaint);
                canvas.drawCircle(tipX, tipY, headR * 0.6f, clotPaint);
                canvas.drawCircle(tipX - headR * 0.25f, tipY - headR * 0.25f, Math.max(1.2f, headR * 0.3f), highlightPaint);
            }
        }

        // C) Radiating Satellite Micro-Droplets
        for (Droplet d : s.droplets) {
            float dropDist = d.distFactor * currentR;
            float dx = s.cx + (float) Math.cos(d.angle) * dropDist;
            float dy = s.cy + (float) Math.sin(d.angle) * dropDist;
            float r = d.radius * Math.min(1.0f, s.animProgress * 1.2f);

            if (r > 1f) {
                canvas.drawCircle(dx, dy, r, arterialPaint);
                canvas.drawCircle(dx, dy, r * 0.55f, clotPaint);
                if (r > 2f) {
                    canvas.drawCircle(dx - r * 0.25f, dy - r * 0.25f, r * 0.35f, highlightPaint);
                }
            }
        }

        // D) Wet Specular Glints on Central Nucleus
        for (Glint g : s.glints) {
            float gx = s.cx + (float) Math.cos(g.angle) * (g.dist * s.animProgress);
            float gy = s.cy + (float) Math.sin(g.angle) * (g.dist * s.animProgress);
            canvas.drawCircle(gx, gy, g.radius, highlightPaint);
        }
    }

    private void drawRealisticDrip(Canvas canvas, RealisticDrip drip) {
        if (drip.currentY <= drip.startY) return;

        float currentLength = drip.currentY - drip.startY;
        float startX = drip.startX;
        float startY = drip.startY;
        float endY = drip.currentY;
        float endX = startX + (float) Math.sin(currentLength * drip.meanderFreq) * drip.meanderAmp;

        int alphaInt = (int) (drip.opacity * 255);
        arterialPaint.setAlpha(alphaInt);
        clotPaint.setAlpha((int) (alphaInt * 0.85f));
        highlightPaint.setAlpha((int) (alphaInt * 0.65f));

        dripArterialPaint.setAlpha(alphaInt);
        dripClotPaint.setAlpha((int) (alphaInt * 0.85f));
        dripHighlightPaint.setAlpha((int) (alphaInt * 0.65f));

        // A) Origin Meniscus
        Path originMeniscus = new Path();
        float topW = drip.streamWidth * 1.8f;
        originMeniscus.moveTo(startX - topW, startY);
        originMeniscus.quadTo(startX, startY + 12f, endX - drip.streamWidth / 2f, startY + 14f);
        originMeniscus.lineTo(endX + drip.streamWidth / 2f, startY + 14f);
        originMeniscus.quadTo(startX, startY + 12f, startX + topW, startY);
        originMeniscus.close();
        canvas.drawPath(originMeniscus, arterialPaint);

        // B) Viscous Meandering Rivulet Body
        Path streamPath = new Path();
        streamPath.moveTo(startX, startY + 10f);
        int segments = Math.max(3, (int) (currentLength / 20f));
        for (int seg = 1; seg <= segments; seg++) {
            float segY = startY + 10f + (currentLength - 10f) * ((float) seg / segments);
            float segX = startX + (float) Math.sin((segY - startY) * drip.meanderFreq) * drip.meanderAmp;
            streamPath.lineTo(segX, segY);
        }

        dripArterialPaint.setStrokeWidth(drip.streamWidth);
        canvas.drawPath(streamPath, dripArterialPaint);

        dripClotPaint.setStrokeWidth(drip.streamWidth * 0.58f);
        canvas.drawPath(streamPath, dripClotPaint);

        dripHighlightPaint.setStrokeWidth(Math.max(1.2f, drip.streamWidth * 0.20f));
        canvas.drawPath(streamPath, dripHighlightPaint);

        // C) Bulbous Teardrop Liquid Head at leading tip
        float hr = drip.streamWidth * 2.5f;
        Path bulbPath = new Path();
        bulbPath.moveTo(endX - drip.streamWidth / 2f, endY - hr * 0.5f);
        bulbPath.quadTo(endX - hr, endY, endX, endY + hr);
        bulbPath.quadTo(endX + hr, endY, endX + drip.streamWidth / 2f, endY - hr * 0.5f);
        bulbPath.close();

        canvas.drawPath(bulbPath, arterialPaint);
        canvas.drawCircle(endX, endY, hr * 0.70f, clotPaint);
        canvas.drawCircle(endX - hr * 0.25f, endY - hr * 0.25f, Math.max(1.6f, hr * 0.28f), highlightPaint);

        // D) Trailing pinch-off beads trailing beneath the drip
        if (currentLength > 70f) {
            for (FallingBead bead : drip.trailingBeads) {
                float beadY = endY + bead.lagOffset;
                if (beadY < drip.maxDropY) {
                    float beadX = endX + (float) Math.sin(beadY * drip.meanderFreq) * 2f;
                    canvas.drawCircle(beadX, beadY, bead.radius, arterialPaint);
                    canvas.drawCircle(beadX, beadY, bead.radius * 0.6f, clotPaint);
                    canvas.drawCircle(beadX - bead.radius * 0.2f, beadY - bead.radius * 0.2f, bead.radius * 0.35f, highlightPaint);
                }
            }
        }
    }
}
