package com.kotobyte.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.text.style.ReplacementSpan;

/**
 * Created by andree.surya on 2017/01/08.
 */
public class StyleableUnderlineSpan extends ReplacementSpan {

    private Paint mUnderlinePaint;

    @Dimension
    private int mUnderlineMargin;

    public StyleableUnderlineSpan(
            @ColorInt int underlineColor,
            @Dimension int underlineThickness,
            @Dimension int underlineMargin) {

        mUnderlinePaint = new Paint();
        mUnderlineMargin = underlineMargin;

        mUnderlinePaint.setColor(underlineColor);
        mUnderlinePaint.setStrokeWidth(underlineThickness);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        paint.getFontMetricsInt(fm);

        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        float underlineCenterY = y + mUnderlineMargin + mUnderlinePaint.getStrokeWidth() / 2f;
        float underlineEnd = x + paint.measureText(text, start, end);

        canvas.drawLine(x, underlineCenterY, underlineEnd, underlineCenterY, mUnderlinePaint);
        canvas.drawText(text, start, end, x, y, paint);
    }
}
