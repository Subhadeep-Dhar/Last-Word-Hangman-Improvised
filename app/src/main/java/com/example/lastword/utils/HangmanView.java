package com.example.lastword.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class HangmanView extends View {
    private Paint bloodPaint;
    private int mistakes = 0;

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
        bloodPaint.setColor(Color.parseColor("#8B0000")); // Dark blood red
        bloodPaint.setStyle(Paint.Style.STROKE);
        bloodPaint.setStrokeWidth(12f);
        bloodPaint.setStrokeCap(Paint.Cap.ROUND);
        bloodPaint.setStrokeJoin(Paint.Join.ROUND);
        bloodPaint.setAntiAlias(true);
        // Add a creepy glow effect
        bloodPaint.setShadowLayer(10f, 0f, 0f, Color.RED);
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
        invalidate(); // Request redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Dimensions and anchor points
        float padding = 40f;
        float scaffoldBaseY = height - padding;
        float scaffoldTopY = padding;
        float scaffoldLeftX = padding * 2;
        float scaffoldRightX = width - padding * 2;
        float ropeX = width / 2f;
        float ropeBottomY = scaffoldTopY + 80f;

        // Draw Scaffold base, pole, top beam, and rope (Always present as long as game is active)
        canvas.drawLine(scaffoldLeftX, scaffoldBaseY, scaffoldRightX, scaffoldBaseY, bloodPaint); // Base
        canvas.drawLine(scaffoldLeftX + 40f, scaffoldBaseY, scaffoldLeftX + 40f, scaffoldTopY, bloodPaint); // Pole
        canvas.drawLine(scaffoldLeftX + 40f, scaffoldTopY, ropeX, scaffoldTopY, bloodPaint); // Top Beam
        canvas.drawLine(ropeX, scaffoldTopY, ropeX, ropeBottomY, bloodPaint); // Rope

        // Stage 1: Head
        float headRadius = 30f;
        float headCenterY = ropeBottomY + headRadius;
        if (mistakes >= 1) {
            canvas.drawCircle(ropeX, headCenterY, headRadius, bloodPaint);
        }

        // Stage 2: Body
        float bodyTopY = headCenterY + headRadius;
        float bodyBottomY = bodyTopY + 80f;
        if (mistakes >= 2) {
            canvas.drawLine(ropeX, bodyTopY, ropeX, bodyBottomY, bloodPaint);
        }

        // Stage 3: Left Arm
        float armTopY = bodyTopY + 15f;
        float leftArmEndX = ropeX - 45f;
        float armEndY = armTopY + 50f;
        if (mistakes >= 3) {
            canvas.drawLine(ropeX, armTopY, leftArmEndX, armEndY, bloodPaint);
        }

        // Stage 4: Right Arm
        float rightArmEndX = ropeX + 45f;
        if (mistakes >= 4) {
            canvas.drawLine(ropeX, armTopY, rightArmEndX, armEndY, bloodPaint);
        }

        // Stage 5: Left Leg
        float leftLegEndX = ropeX - 35f;
        float rightLegEndX = ropeX + 35f;
        float legEndY = bodyBottomY + 70f;
        if (mistakes >= 5) {
            canvas.drawLine(ropeX, bodyBottomY, leftLegEndX, legEndY, bloodPaint);
        }

        // Stage 6: Right Leg (Full Death)
        if (mistakes >= 6) {
            canvas.drawLine(ropeX, bodyBottomY, rightLegEndX, legEndY, bloodPaint);
        }
    }
}
