package com.kotobyte.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.kotobyte.R;
import com.kotobyte.util.ColorUtil;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/21.
 */

@SuppressLint("ViewConstructor")
public class KanjiStrokeView extends View {

    // Assuming supplied Kanji strokes are drawn on 109x109 SVG canvas (KanjiVG format).
    private static final int DOCUMENT_SIZE = 109;

    private int mViewSize;
    private int mMarkRadius;

    private Path mGuidePath = new Path();
    private Path mStrokePath = new Path();
    private Path mMarkPath = new Path();

    private Paint mGuidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public KanjiStrokeView(Context context, List<String> strokes, int drawLimit) {
        super(context);

        mViewSize = context.getResources().getDimensionPixelSize(R.dimen.kanji_stroke_view_size);
        mMarkRadius = context.getResources().getDimensionPixelSize(R.dimen.kanji_stroke_mark_radius);

        int mStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.kanji_stroke_width);
        int mStrokeColor = ColorUtil.getColor(context, R.color.primary_text);
        int mGuideColor = ColorUtil.getColor(context, R.color.divider);
        int mMarkColor = ColorUtil.getColor(context, R.color.primary);

        mGuidePaint.setStrokeWidth(mStrokeWidth);
        mGuidePaint.setStyle(Style.STROKE);
        mGuidePaint.setColor(mGuideColor);

        mStrokePaint.setStrokeWidth(mStrokeWidth);
        mStrokePaint.setStrokeCap(Cap.ROUND);
        mStrokePaint.setStyle(Style.STROKE);
        mStrokePaint.setColor(mStrokeColor);

        mMarkPaint.setColor(mMarkColor);
        mMarkPaint.setStyle(Style.FILL);

        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);

        drawKanjiStrokes(strokes, drawLimit);
    }

    public KanjiStrokeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }


    public void drawKanjiStrokes(List<String> strokes, int drawLimit) {

        mStrokePath.rewind();
        mMarkPath.rewind();

        // Draw guide lines.
        int guideMiddle = Math.round(DOCUMENT_SIZE / 2f);
        mGuidePath.moveTo(guideMiddle, 0);
        mGuidePath.lineTo(guideMiddle, DOCUMENT_SIZE);
        mGuidePath.moveTo(0, guideMiddle);
        mGuidePath.lineTo(DOCUMENT_SIZE, guideMiddle);

        VectorPathTokenizer vectorPathTokenizer = new VectorPathTokenizer();

        // Draw Kanji stroke lines and dot mark.
        for (int i = 0; i < drawLimit; i++) {
            vectorPathTokenizer.setString(strokes.get(i));

            char command = vectorPathTokenizer.nextCommand();

            while (command != 0) {

                // SVG move commands (M = absolute, m = relative)
                if (command == 'M' || command == 'm') {
                    float x = vectorPathTokenizer.nextFloat();
                    float y = vectorPathTokenizer.nextFloat();

                    if (i == drawLimit - 1) { // Last stroke? Draw a mark.
                        mMarkPath.addCircle(x, y, mMarkRadius, Path.Direction.CW);
                    }

                    if (command == 'M') {
                        mStrokePath.moveTo(x, y);
                    }

                    if (command == 'm') {
                        mStrokePath.rMoveTo(x, y);
                    }
                }

                // SVG draw commands (C = absolute, c = relative)
                if (command == 'C' || command == 'c') {
                    float x1 = vectorPathTokenizer.nextFloat();
                    float y1 = vectorPathTokenizer.nextFloat();
                    float x2 = vectorPathTokenizer.nextFloat();
                    float y2 = vectorPathTokenizer.nextFloat();
                    float x = vectorPathTokenizer.nextFloat();
                    float y = vectorPathTokenizer.nextFloat();

                    if (command == 'C') {
                        mStrokePath.cubicTo(x1, y1, x2, y2, x, y);
                    }

                    if (command == 'c') {
                        mStrokePath.rCubicTo(x1, y1, x2, y2, x, y);
                    }
                }

                command = vectorPathTokenizer.nextCommand();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(mViewSize / (float) DOCUMENT_SIZE, mViewSize / (float) DOCUMENT_SIZE);

        canvas.drawPath(mGuidePath, mGuidePaint);
        canvas.drawPath(mStrokePath, mStrokePaint);
        canvas.drawPath(mMarkPath, mMarkPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(mViewSize, mViewSize);
    }
}