package com.kotobyte.search;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.TextureView;

import com.kotobyte.R;
import com.kotobyte.utils.vector.VectorPath;
import com.kotobyte.utils.vector.VectorPathCommand;

import java.util.List;


public class KanjiStrokesTextureView extends TextureView {

    private static final String TAG = KanjiStrokesTextureView.class.getSimpleName();

    // Assuming supplied Kanji strokes are drawn on 109x109 SVG canvas (KanjiVG format).
    private static final int DOCUMENT_SIZE = 109;

    private List<VectorPath> mVectorPaths;

    private int mBackgroundColor;
    private float mGridSize;
    private float mGridSpacing;
    private float mMarkRadius;

    private Path mGuidePath = new Path();
    private Path mStrokePath = new Path();
    private Path mMarkPath = new Path();

    private Paint mGuidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private HandlerThread mDrawingThread;
    private Handler mDrawingHandler;

    public KanjiStrokesTextureView(Context context) {
        super(context);

        initialize();
    }

    public KanjiStrokesTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public KanjiStrokesTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
    }

    public void setVectorPaths(List<VectorPath> vectorPaths) {
        mVectorPaths = vectorPaths;

        invalidate();
    }

    private void initialize() {
        mGridSize = getResources().getDimension(R.dimen.kanji_stroke_grid_size);
        mGridSpacing = getResources().getDimension(R.dimen.kanji_stroke_grid_spacing);
        mMarkRadius = getResources().getDimension(R.dimen.kanji_stroke_mark_radius);
        mBackgroundColor = ContextCompat.getColor(getContext(), R.color.background);

        float strokeWidth = getResources().getDimension(R.dimen.kanji_stroke_line_width);
        int strokeColor = ContextCompat.getColor(getContext(), R.color.primary_text);
        int guideColor = ContextCompat.getColor(getContext(), R.color.divider);
        int markColor = ContextCompat.getColor(getContext(), R.color.primary);

        mGuidePaint.setStrokeWidth(strokeWidth);
        mGuidePaint.setStyle(Paint.Style.STROKE);
        mGuidePaint.setColor(guideColor);

        mStrokePaint.setStrokeWidth(strokeWidth);
        mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(strokeColor);

        mMarkPaint.setColor(markColor);
        mMarkPaint.setStyle(Paint.Style.FILL);

        setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mVectorPaths == null || mVectorPaths.size() == 0) {
            setMeasuredDimension(0, 0);

        } else {
            int parentWidth = MeasureSpec.getSize(widthMeasureSpec);

            int numberOfColumns = calculateNumberOfColumnsForContainerWidth(parentWidth);
            int numberOfRows = (int) Math.ceil(mVectorPaths.size() / (double) numberOfColumns);
            int suitableHeight = (int) Math.ceil(numberOfRows * (mGridSize + mGridSpacing));

            setMeasuredDimension(parentWidth, suitableHeight);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mDrawingThread != null) {
            mDrawingThread.quitSafely();
        }
    }

    private int calculateNumberOfColumnsForContainerWidth(int width) {
        return Math.max((int) (width / (mGridSize + mGridSpacing)), 1);
    }

    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            mDrawingThread = new HandlerThread(TAG);
            mDrawingThread.start();

            mDrawingHandler = new Handler(mDrawingThread.getLooper());
            mDrawingHandler.post(mDrawingAction);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mDrawingHandler.post(mDrawingAction);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private Runnable mDrawingAction = new Runnable() {

        @Override
        public void run() {

            Canvas canvas = lockCanvas();

            if (canvas == null) {
                return;
            }

            float drawingScale = mGridSize / DOCUMENT_SIZE;
            int numberOfColumns = calculateNumberOfColumnsForContainerWidth(canvas.getWidth());

            mStrokePath.rewind();
            mMarkPath.rewind();
            mGuidePath.rewind();

            // Draw N number of grid cells for N number of vector paths.
            for (int i = 0; i < mVectorPaths.size(); i++) {

                float cellMinX = (i % numberOfColumns) * (mGridSize + mGridSpacing);
                float cellMinY = (i / numberOfColumns) * (mGridSize + mGridSpacing);
                float cellMidX = cellMinX + mGridSize / 2;
                float cellMidY = cellMinY + mGridSize / 2;
                float cellMaxX = cellMinX + mGridSize;
                float cellMaxY = cellMinY + mGridSize;

                // Draw guide line for this grid cell.
                mGuidePath.moveTo(cellMidX, cellMinY);
                mGuidePath.lineTo(cellMidX, cellMaxY);
                mGuidePath.moveTo(cellMinX, cellMidY);
                mGuidePath.lineTo(cellMaxX, cellMidY);

                // Draw strokes for this grid cell.
                for (int j = 0; j <= i; j++) {
                    VectorPath vectorPath = mVectorPaths.get(j);

                    // Draw according to the commands from stroke vector path.
                    for (int k = 0; k < vectorPath.getNumberOfCommands(); k++) {
                        VectorPathCommand command = vectorPath.getCommand(k);

                        if (command instanceof VectorPathCommand.Move) {
                            VectorPathCommand.Move moveCommand = (VectorPathCommand.Move) command;

                            float x = moveCommand.getX() * drawingScale;
                            float y = moveCommand.getY() * drawingScale;

                            if (moveCommand.isRelative()) {
                                mStrokePath.rMoveTo(x, y);

                            } else {
                                x += cellMinX;
                                y += cellMinY;

                                mStrokePath.moveTo(x, y);
                            }

                            if (j == i) {
                                mMarkPath.addCircle(x, y, mMarkRadius, Path.Direction.CW);
                            }

                        } else if (command instanceof VectorPathCommand.Cubic) {
                            VectorPathCommand.Cubic cubicCommand = (VectorPathCommand.Cubic) command;

                            float x1 = cubicCommand.getX1() * drawingScale;
                            float y1 = cubicCommand.getY1() * drawingScale;
                            float x2 = cubicCommand.getX2() * drawingScale;
                            float y2 = cubicCommand.getY2() * drawingScale;
                            float x3 = cubicCommand.getX3() * drawingScale;
                            float y3 = cubicCommand.getY3() * drawingScale;

                            if (cubicCommand.isRelative()) {
                                mStrokePath.rCubicTo(x1, y1, x2, y2, x3, y3);

                            } else {
                                x1 += cellMinX;
                                y1 += cellMinY;
                                x2 += cellMinX;
                                y2 += cellMinY;
                                x3 += cellMinX;
                                y3 += cellMinY;

                                mStrokePath.cubicTo(x1, y1, x2, y2, x3, y3);
                            }
                        }
                    }
                }
            }

            canvas.drawColor(mBackgroundColor, PorterDuff.Mode.SRC);
            canvas.drawPath(mGuidePath, mGuidePaint);
            canvas.drawPath(mStrokePath, mStrokePaint);
            canvas.drawPath(mMarkPath, mMarkPaint);

            unlockCanvasAndPost(canvas);
        }
    };
}
