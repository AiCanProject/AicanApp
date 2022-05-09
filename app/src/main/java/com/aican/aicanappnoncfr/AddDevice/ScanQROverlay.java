package com.aican.aicanappnoncfr.AddDevice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aican.aicanappnoncfr.R;


public class ScanQROverlay extends View {

    private static final String TAG = "ScanQROverlay";
    int outerFillColor = 0xbe000000;//b2//be//cd

    int centerX;
    int centerY;

    Bitmap cutoutBitmap;
    Paint cutoutPaint;
    float cutoutWidth = dp(300);
    float cutoutHeight = dp(300);
    float cutoutCornerRadius = dp(20);
    float verticalOffset = -dp(80);

    Paint textPaint;
    Paint scanLinePaint;


    long startTime;
    int animationDuration = 2000;
    int fps = 60;
    int repeatDelay = 500;

    public ScanQROverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2;
        centerY = h / 2;

        cutoutBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas auxCanvas = new Canvas(cutoutBitmap);

        cutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cutoutPaint.setColor(outerFillColor);
        cutoutPaint.setStyle(Paint.Style.FILL);
        auxCanvas.drawPaint(cutoutPaint);

        RectF rect = new RectF(
                centerX - (cutoutWidth / 2),
                centerY - (cutoutHeight / 2) + verticalOffset,
                centerX + (cutoutWidth / 2),
                centerY + (cutoutHeight / 2) + verticalOffset
        );

        cutoutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        auxCanvas.drawRoundRect(rect, cutoutCornerRadius, cutoutCornerRadius, cutoutPaint);

        cutoutPaint.setXfermode(null);
        cutoutPaint.setColor(getColor(R.color.blue));
        cutoutPaint.setStyle(Paint.Style.STROKE);
        cutoutPaint.setStrokeWidth(dp(2));
        auxCanvas.drawRoundRect(rect, cutoutCornerRadius, cutoutCornerRadius, cutoutPaint);

        textPaint = new Paint();
        textPaint.setColor(getColor(R.color.white));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(sp(24));

        scanLinePaint = new Paint();
        scanLinePaint.setStyle(Paint.Style.FILL);
        scanLinePaint.setStrokeWidth(dp(2));
        scanLinePaint.setColor(getColor(R.color.red));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawScanAnimation(canvas);
        canvas.drawBitmap(cutoutBitmap, 0f, 0f, cutoutPaint);
        drawText(canvas);
    }

    private void drawScanAnimation(Canvas canvas) {

        long elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime > animationDuration + repeatDelay) {
            elapsedTime = 0;
            startTime = System.currentTimeMillis();
        }
        float distFromTop = elapsedTime * cutoutHeight / animationDuration;
        float y = (centerY - cutoutWidth / 2 + verticalOffset) + distFromTop;

        float cornerOffset = 0;
        float distFromBottom = centerY + cutoutHeight / 2 + verticalOffset - y;
        if (distFromTop < cutoutCornerRadius) {
            float dy = cutoutCornerRadius - distFromTop;
            cornerOffset = cutoutCornerRadius - (float) Math.sqrt(cutoutCornerRadius * cutoutCornerRadius - dy * dy);
        } else if (distFromBottom < cutoutCornerRadius) {
            float dy = cutoutCornerRadius - distFromBottom;
            cornerOffset = cutoutCornerRadius - (float) Math.sqrt(cutoutCornerRadius * cutoutCornerRadius - dy * dy);
        }

        if (elapsedTime <= animationDuration)
            canvas.drawLine(
                    centerX - cutoutWidth / 2 + cornerOffset,
                    y,
                    centerX + cutoutWidth / 2 - cornerOffset,
                    y,
                    scanLinePaint
            );


        this.postInvalidateDelayed(1000 / fps);
    }

    private void drawText(Canvas canvas) {
        String text = "Scan QR Code";
        float x = centerX - textPaint.measureText(text) / 2;
        float y = centerY - cutoutHeight / 2 - dp(24) + verticalOffset;
        canvas.drawText(text, x, y, textPaint);
    }

    private int getColor(int color) {
        return ContextCompat.getColor(getContext(), color);
    }

    private float dp(int x) {
        return (float) x * getResources().getDisplayMetrics().density;
    }

    private float sp(int x) {
        return (float) x * getResources().getDisplayMetrics().scaledDensity;
    }

}
