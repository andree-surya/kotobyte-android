package com.kotobyte.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.text.style.ReplacementSpan;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/08.
 */
class LiteralSpan extends ReplacementSpan {

    private Paint mUnderlinePaint;
    private Paint mHighlightPaint;

    @Dimension
    private int mUnderlineMargin;

    private List<HighlightInterval> mHighlightIntervals;

    LiteralSpan(
            List<HighlightInterval> highlightIntervals,
            @Dimension int underlineMargin,
            @Dimension int underlineThickness,
            @ColorInt int underlineColor,
            @ColorInt int highlightColor) {

        mUnderlinePaint = new Paint();
        mHighlightPaint = new Paint();
        mUnderlineMargin = underlineMargin;
        mHighlightIntervals = highlightIntervals;

        mUnderlinePaint.setColor(underlineColor);
        mUnderlinePaint.setStrokeWidth(underlineThickness);
        mHighlightPaint.setColor(highlightColor);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        paint.getFontMetricsInt(fm);

        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        for (HighlightInterval highlightInterval : mHighlightIntervals) {

            int highlightStart = start + highlightInterval.mStart;
            int highlightEnd = start + highlightInterval.mEnd;

            float highlightStopX = x + paint.measureText(text, highlightStart, highlightEnd);
            canvas.drawRect(x, top, highlightStopX, bottom, mHighlightPaint);
        }

        float underlineCenterY = y + mUnderlineMargin + mUnderlinePaint.getStrokeWidth() / 2f;
        float underlineStopX = x + paint.measureText(text, start, end);

        canvas.drawLine(x, underlineCenterY, underlineStopX, underlineCenterY, mUnderlinePaint);
        canvas.drawText(text, start, end, x, y, paint);
    }

    static class HighlightInterval {

        private int mStart;
        private int mEnd;

        void setStart(int start) {
            mStart = start;
        }

        void setEnd(int end) {
            mEnd = end;
        }
    }
}
