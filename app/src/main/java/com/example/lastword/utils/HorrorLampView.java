package com.example.lastword.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class HorrorLampView extends View {
    public interface OnLampFlickerListener {
        void onLampFlicker(boolean isLightOn);
    }

    private Paint wirePaint;
    private Paint hoodPaint;
    private Paint bulbPaint;
    private Paint filamentPaint;
    private Paint lightConePaint;
    private Paint glowPaint;

    private float lightIntensity = 0f; // 0 = dark, 1 = fully lit
    private float swingAngle = 0f;
    private ValueAnimator swingAnimator;
    private OnLampFlickerListener flickerListener;

    public HorrorLampView(Context context) {
        super(context);
        init();
    }

    public HorrorLampView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorrorLampView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wirePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wirePaint.setColor(Color.parseColor("#333333"));
        wirePaint.setStrokeWidth(3.5f);

        hoodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hoodPaint.setColor(Color.parseColor("#1A1A1A"));
        hoodPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        bulbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bulbPaint.setStyle(Paint.Style.FILL);

        filamentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filamentPaint.setColor(Color.parseColor("#FFE4A0"));
        filamentPaint.setStrokeWidth(2.5f);
        filamentPaint.setStyle(Paint.Style.STROKE);

        lightConePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightConePaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);

        startAmbientSway();
    }

    private void startAmbientSway() {
        swingAnimator = ValueAnimator.ofFloat(-2.5f, 2.5f);
        swingAnimator.setDuration(3200);
        swingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        swingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        swingAnimator.setInterpolator(new LinearInterpolator());
        swingAnimator.addUpdateListener(anim -> {
            swingAngle = (float) anim.getAnimatedValue();
            invalidate();
        });
        swingAnimator.start();
    }

    public void setLightIntensity(float intensity) {
        this.lightIntensity = Math.max(0f, Math.min(1f, intensity));
        invalidate();
    }

    public float getLightIntensity() {
        return lightIntensity;
    }

    /**
     * Executes an erratic, terrifying electrical blinking / flicker sequence.
     */
    public void startFlickerSequence(final OnLampFlickerListener listener) {
        this.flickerListener = listener;
        final Handler handler = new Handler(Looper.getMainLooper());

        // 1. Initial complete blackout (0ms)
        setLightIntensity(0f);
        if (listener != null) listener.onLampFlicker(false);

        // 2. First faint electrical spark (700ms)
        handler.postDelayed(() -> {
            setLightIntensity(0.25f);
            if (listener != null) listener.onLampFlicker(true);

            // 3. Drop back into darkness (780ms)
            handler.postDelayed(() -> {
                setLightIntensity(0f);
                if (listener != null) listener.onLampFlicker(false);

                // 4. Violent double flicker (1300ms)
                handler.postDelayed(() -> {
                    setLightIntensity(0.7f);
                    if (listener != null) listener.onLampFlicker(true);

                    handler.postDelayed(() -> {
                        setLightIntensity(0.1f);
                        if (listener != null) listener.onLampFlicker(false);

                        handler.postDelayed(() -> {
                            setLightIntensity(0.85f);
                            if (listener != null) listener.onLampFlicker(true);

                            // 5. Brief stutter before full eerie illumination (2000ms)
                            handler.postDelayed(() -> {
                                setLightIntensity(0.35f);

                                handler.postDelayed(() -> {
                                    setLightIntensity(1.0f);
                                    if (listener != null) listener.onLampFlicker(true);
                                }, 120);

                            }, 250);

                        }, 60);

                    }, 80);

                }, 520);

            }, 80);

        }, 700);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float pivotX = w / 2f;
        float pivotY = 0f;

        canvas.save();
        // Subtle sway in the horror draft
        canvas.rotate(swingAngle, pivotX, pivotY);

        float wireLength = h * 0.18f;
        float lampCenterX = pivotX;
        float lampCenterY = wireLength;

        // 1. Light Cone Beam (Shining down through the abyss when lit)
        if (lightIntensity > 0.02f) {
            float coneTopY = lampCenterY + 15f;
            float coneBottomY = h * 1.1f;
            float coneTopWidth = 35f;
            float coneBottomWidth = w * 0.85f;

            Path conePath = new Path();
            conePath.moveTo(lampCenterX - coneTopWidth, coneTopY);
            conePath.lineTo(lampCenterX + coneTopWidth, coneTopY);
            conePath.lineTo(lampCenterX + coneBottomWidth, coneBottomY);
            conePath.lineTo(lampCenterX - coneBottomWidth, coneBottomY);
            conePath.close();

            int coneAlpha = (int) (lightIntensity * 125);
            lightConePaint.setShader(new LinearGradient(
                    lampCenterX, coneTopY,
                    lampCenterX, coneBottomY,
                    new int[]{
                            Color.argb(coneAlpha, 255, 230, 160),
                            Color.argb((int) (coneAlpha * 0.5f), 220, 150, 80),
                            Color.TRANSPARENT
                    },
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawPath(conePath, lightConePaint);

            // Radial bulb aura glow
            int glowAlpha = (int) (lightIntensity * 180);
            glowPaint.setShader(new RadialGradient(
                    lampCenterX, lampCenterY + 22f, 90f,
                    Color.argb(glowAlpha, 255, 240, 180),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(lampCenterX, lampCenterY + 22f, 90f, glowPaint);
        }

        // 2. Wire from top ceiling
        canvas.drawLine(pivotX, pivotY, lampCenterX, lampCenterY, wirePaint);

        // 3. Vintage Hood / Shade
        Path hoodPath = new Path();
        float hoodWidth = 50f;
        float hoodHeight = 18f;
        hoodPath.moveTo(lampCenterX - hoodWidth, lampCenterY + hoodHeight);
        hoodPath.lineTo(lampCenterX + hoodWidth, lampCenterY + hoodHeight);
        hoodPath.lineTo(lampCenterX + (hoodWidth * 0.4f), lampCenterY);
        hoodPath.lineTo(lampCenterX - (hoodWidth * 0.4f), lampCenterY);
        hoodPath.close();
        canvas.drawPath(hoodPath, hoodPaint);

        // 4. Glass bulb and filament
        float bulbRadius = 14f;
        float bulbCenterY = lampCenterY + hoodHeight + bulbRadius - 2f;
        int bulbColor = (lightIntensity > 0.1f)
                ? Color.argb((int) (180 + lightIntensity * 75), 255, (int)(220 + lightIntensity * 35), (int)(140 + lightIntensity * 100))
                : Color.argb(80, 80, 80, 80);
        bulbPaint.setColor(bulbColor);
        canvas.drawCircle(lampCenterX, bulbCenterY, bulbRadius, bulbPaint);

        // Filament inside
        if (lightIntensity > 0.05f) {
            filamentPaint.setAlpha((int) (lightIntensity * 255));
            canvas.drawLine(lampCenterX - 4f, bulbCenterY - 4f, lampCenterX, bulbCenterY + 4f, filamentPaint);
            canvas.drawLine(lampCenterX, bulbCenterY + 4f, lampCenterX + 4f, bulbCenterY - 4f, filamentPaint);
        }

        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (swingAnimator != null) swingAnimator.cancel();
    }
}
