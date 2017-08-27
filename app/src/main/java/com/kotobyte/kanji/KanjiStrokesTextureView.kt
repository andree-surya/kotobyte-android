package com.kotobyte.kanji

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.TextureView
import android.view.View

import com.kotobyte.R
import com.kotobyte.utils.vector.VectorPath
import com.kotobyte.utils.vector.VectorPathCommand


class KanjiStrokesTextureView : TextureView {

    private var vectorPaths = listOf<VectorPath>()

    private var gridColor: Int = 0
    private var gridSize: Float = 0.toFloat()
    private var gridSpacing: Float = 0.toFloat()
    private var markRadius: Float = 0.toFloat()

    private val guidePath = Path()
    private val strokePath = Path()
    private val markPath = Path()

    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var drawingThread: HandlerThread? = null
    private var drawingHandler: Handler? = null

    private val textureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

            drawingThread = HandlerThread(TAG).apply { start() }
            drawingHandler = Handler(drawingThread?.looper)

            drawingHandler?.post(drawingAction)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            drawingHandler?.post(drawingAction)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
    }

    private val drawingAction = Runnable {
        val canvas = lockCanvas() ?: return@Runnable

        val drawingScale = gridSize / DOCUMENT_SIZE
        val numberOfColumns = calculateNumberOfColumnsForContainerWidth(canvas.width)

        strokePath.rewind()
        markPath.rewind()
        guidePath.rewind()

        // Draw N number of grid cells for N number of vector paths.
        for (i in vectorPaths.indices) {

            val cellMinX = i % numberOfColumns * (gridSize + gridSpacing)
            val cellMinY = i / numberOfColumns * (gridSize + gridSpacing)
            val cellMidX = cellMinX + gridSize / 2
            val cellMidY = cellMinY + gridSize / 2
            val cellMaxX = cellMinX + gridSize
            val cellMaxY = cellMinY + gridSize

            // Draw guide line for this grid cell.
            guidePath.moveTo(cellMidX, cellMinY)
            guidePath.lineTo(cellMidX, cellMaxY)
            guidePath.moveTo(cellMinX, cellMidY)
            guidePath.lineTo(cellMaxX, cellMidY)

            // Draw strokes for this grid cell.
            for (j in 0..i) {
                val vectorPath = vectorPaths[j]

                // Draw according to the commands from stroke vector path.
                for (k in 0 until vectorPath.numberOfCommands) {
                    val command = vectorPath.getCommand(k)

                    if (command is VectorPathCommand.Move) {
                        val (x1, y1, isRelative) = command

                        var x = x1 * drawingScale
                        var y = y1 * drawingScale

                        if (isRelative) {
                            strokePath.rMoveTo(x, y)

                        } else {
                            x += cellMinX
                            y += cellMinY

                            strokePath.moveTo(x, y)
                        }

                        if (j == i) {
                            markPath.addCircle(x, y, markRadius, Path.Direction.CW)
                        }

                    } else if (command is VectorPathCommand.Cubic) {
                        val (x11, y11, x21, y21, x31, y31, isRelative) = command

                        var x1 = x11 * drawingScale
                        var y1 = y11 * drawingScale
                        var x2 = x21 * drawingScale
                        var y2 = y21 * drawingScale
                        var x3 = x31 * drawingScale
                        var y3 = y31 * drawingScale

                        if (isRelative) {
                            strokePath.rCubicTo(x1, y1, x2, y2, x3, y3)

                        } else {
                            x1 += cellMinX
                            y1 += cellMinY
                            x2 += cellMinX
                            y2 += cellMinY
                            x3 += cellMinX
                            y3 += cellMinY

                            strokePath.cubicTo(x1, y1, x2, y2, x3, y3)
                        }
                    }
                }
            }
        }

        canvas.drawColor(gridColor, PorterDuff.Mode.SRC)
        canvas.drawPath(guidePath, guidePaint)
        canvas.drawPath(strokePath, strokePaint)
        canvas.drawPath(markPath, markPaint)

        unlockCanvasAndPost(canvas)
    }

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    fun setVectorPaths(vectorPaths: List<VectorPath>) {
        this.vectorPaths = vectorPaths

        invalidate()
    }

    private fun initialize() {
        gridSize = resources.getDimension(R.dimen.kanji_stroke_grid_size)
        gridSpacing = resources.getDimension(R.dimen.kanji_stroke_grid_spacing)
        markRadius = resources.getDimension(R.dimen.kanji_stroke_mark_radius)
        gridColor = ContextCompat.getColor(context, R.color.background)

        val strokeWidth = resources.getDimension(R.dimen.kanji_stroke_line_width)
        val strokeColor = ContextCompat.getColor(context, R.color.primary_text)
        val guideColor = ContextCompat.getColor(context, R.color.divider)
        val markColor = ContextCompat.getColor(context, R.color.primary)

        guidePaint.strokeWidth = strokeWidth
        guidePaint.style = Paint.Style.STROKE
        guidePaint.color = guideColor

        strokePaint.strokeWidth = strokeWidth
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = strokeColor

        markPaint.color = markColor
        markPaint.style = Paint.Style.FILL

        surfaceTextureListener = textureListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (vectorPaths.isEmpty()) {
            setMeasuredDimension(0, 0)

        } else {
            val parentWidth = View.MeasureSpec.getSize(widthMeasureSpec)

            val numberOfColumns = calculateNumberOfColumnsForContainerWidth(parentWidth)
            val numberOfRows = Math.ceil(vectorPaths.size / numberOfColumns.toDouble()).toInt()
            val suitableHeight = Math.ceil((numberOfRows * (gridSize + gridSpacing)).toDouble()).toInt()

            setMeasuredDimension(parentWidth, suitableHeight)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        drawingThread?.quitSafely()
    }

    private fun calculateNumberOfColumnsForContainerWidth(width: Int): Int =
            Math.max((width / (gridSize + gridSpacing)).toInt(), 1)

    companion object {

        private val TAG = KanjiStrokesTextureView::class.java.simpleName

        // Assuming supplied Kanji strokes are drawn on 109x109 SVG canvas (KanjiVG format).
        private val DOCUMENT_SIZE = 109
    }
}
