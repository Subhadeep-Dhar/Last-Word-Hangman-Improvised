package com.example.lastword.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HangmanView extends View {
    private Paint bloodPaint;
    private Paint spatterPaint;
    private int mistakes = 0;
    private String difficulty = "EASY";
    private float timeOffset = 0;
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
    }

    public void setDifficulty(String diff) {
        this.difficulty = diff;
        invalidate();
    }

    public void setMistakes(int mistakes) {
        if (mistakes > this.mistakes && mistakes <= 6) {
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
        if (mistakes > 0 && mistakes < 6) {
            twitch = (float) Math.sin(timeOffset) * (mistakes * 1.5f); // Oscillating twitch based on pain
        }

        int width = getWidth();
        int height = getHeight();

        // Draw Scaffold base, pole, top beam, and rope (Always present)
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
        canvas.drawLine(ropeX, scaffoldTopY, ropeX, ropeBottomY, bloodPaint); // Rope

        // Draw spatters
        for (Spatter s : spatters) {
            canvas.save();
            canvas.translate(0, s.yOffset);
            canvas.drawPath(s.path, spatterPaint);
            canvas.restore();
            s.yOffset += 0.3f; // Slower, thicker dripping effect
        }

        if ("HARD".equals(difficulty)) {
            drawChild(canvas, ropeX, ropeBottomY, twitch);
        } else {
            drawAdult(canvas, ropeX, ropeBottomY, twitch); // Use standard hangman for both adult and animal
        }

        // Loop animation if suffering (but not fully dead yet)
        if (mistakes > 0 && mistakes < 6) {
            postInvalidateDelayed(30);
        }
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

    private void drawAnimal(Canvas canvas, float ropeX, float ropeBottomY, float twitch) {
        Path animalPath = new Path();
        float neckY = ropeBottomY;
        float bodyCenterY = neckY + 40f;
        float bodyBottomY = neckY + 90f;
        
        // Stage 1: Head (Hanging lifelessly down and slightly angled)
        if (mistakes >= 1) {
            animalPath.moveTo(ropeX, neckY); // tied at neck
            animalPath.cubicTo(ropeX - 10, neckY + 10, ropeX - 25, neckY + 25, ropeX - 20, neckY + 40); // snout hanging down
            animalPath.quadTo(ropeX, neckY + 45, ropeX + 15, neckY + 30); // jaw
            animalPath.quadTo(ropeX + 10, neckY + 15, ropeX, neckY); // back of head
        }

        // Stage 2: Spine/Torso (Vertical hanging limp body)
        if (mistakes >= 2) {
            animalPath.moveTo(ropeX, neckY + 10);
            animalPath.cubicTo(ropeX - 15, bodyCenterY, ropeX + 5, bodyBottomY - 20, ropeX - 5, bodyBottomY); 
        }

        // Stage 3: Left Front Leg
        if (mistakes >= 3) {
            float shoulderY = neckY + 25f;
            animalPath.moveTo(ropeX - 10, shoulderY);
            animalPath.quadTo(ropeX - 20, shoulderY + 20, ropeX - 15, shoulderY + 50); // Left front leg
        }

        // Stage 4: Right Front Leg
        if (mistakes >= 4) {
            float shoulderY = neckY + 25f;
            animalPath.moveTo(ropeX + 5, shoulderY);
            animalPath.quadTo(ropeX + 15, shoulderY + 25, ropeX + 10, shoulderY + 45); // Right front leg
        }

        // Stage 5: Left Back Leg
        if (mistakes >= 5) {
            float rumpY = bodyBottomY - 10f;
            animalPath.moveTo(ropeX - 5, rumpY);
            animalPath.quadTo(ropeX - 15, rumpY + 30, ropeX - 10, rumpY + 60); // Left back leg
        }

        // Stage 6: Right Back Leg, Tail, Ears
        if (mistakes >= 6) {
            float rumpY = bodyBottomY - 10f;
            animalPath.moveTo(ropeX + 5, rumpY - 5);
            animalPath.quadTo(ropeX + 15, rumpY + 25, ropeX + 10, rumpY + 50); // Right back leg
            
            // Tail (Hanging limp, straight down)
            animalPath.moveTo(ropeX - 2, bodyBottomY);
            animalPath.quadTo(ropeX, bodyBottomY + 20, ropeX + 5, bodyBottomY + 40);
            
            // Ears (Drooping downwards)
            animalPath.moveTo(ropeX + 5, neckY + 10);
            animalPath.quadTo(ropeX + 25, neckY + 15, ropeX + 20, neckY + 30);
            animalPath.moveTo(ropeX - 5, neckY + 10);
            animalPath.quadTo(ropeX - 20, neckY + 15, ropeX - 15, neckY + 30);
        }

        // Apply twitch offset
        canvas.save();
        canvas.translate(twitch, twitch * 0.2f);
        canvas.drawPath(animalPath, bloodPaint);
        canvas.restore();
    }
}
