package com.aican.aicanapp.pumpController

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.aican.aicanapp.R
import com.aican.aicanapp.tempController.*
import kotlin.math.roundToInt

class VerticalSlider(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var sliderDrawable = context.getDrawableCompat(R.drawable.ic_slider)

    private val circleRadius = 16F.toPx()
    private val curveStrokeWidth = 4F.toPx()

    private var progressX = 0.0F
    private var progressY = 150.0F.toPx()
    private var labelX = 0.0F

    private val verticalPaddingLine = 0.15F
    private val labelVerticalPaddingFactor = 0.1F
    private var labelVerticalPadding = 0.0F
    private val labelTextSize = 18F.toPx()

    private var maxRange = 300F
    private var minRange = 20F

    private val lineColor = context.getColorCompat(R.color.colorAccent)

    private var invert = false

    private val linePaint: Paint = Paint().apply {
        color = lineColor
        style = Paint.Style.STROKE
        strokeWidth = curveStrokeWidth
        isAntiAlias = true
    }
    private val circlePaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColorCompat(R.color.white)
    }
    private val circleBorderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = lineColor
        strokeWidth = curveStrokeWidth
    }
    private val labelPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        color = context.getColorCompat(R.color.grey)
        textSize = labelTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VolController)
        invert = typedArray.getBoolean(R.styleable.VolController_invert, false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (!invert) {
            progressX = w - circleRadius - curveStrokeWidth
            labelX = w - (2 * circleRadius + 2 * curveStrokeWidth + 24F.toPx())
        } else {
            progressX = circleRadius + curveStrokeWidth
            labelX = (2 * circleRadius + 2 * curveStrokeWidth + 24F.toPx())
        }
        labelVerticalPadding = labelVerticalPaddingFactor * h

        val bounds = Rect()
        labelPaint.getTextBounds("999", 0, 3, bounds)
        scalePaddingVertical = labelVerticalPadding + bounds.height() / 2

        linePaint.shader = LinearGradient(
            progressX,
            0F,
            progressX,
            h.toFloat(),
            intArrayOf(
                lineColor.adjustAlpha(0.0F),
                lineColor,
                lineColor,
                lineColor.adjustAlpha(0.0F)
            ),
            floatArrayOf(
                0.0F,
                verticalPaddingLine,
                1.0F - verticalPaddingLine,
                1.0F
            ),
            Shader.TileMode.CLAMP
        )
        calcPrevLabelPath()
        calcCurrentLabelPath()
        setProgressDelayed()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawScale(this)
            drawLine(progressX, 0F, progressX, height.toFloat(), linePaint)
            drawCircle(progressX, progressY, circleRadius + curveStrokeWidth / 2, circleBorderPaint)
            drawCircle(progressX, progressY, circleRadius, circlePaint)
            sliderDrawable?.setBounds(
                (progressX - circleRadius).toInt(),
                (progressY - circleRadius).toInt(),
                (progressX + circleRadius).toInt(),
                (progressY + circleRadius).toInt()
            )
            sliderDrawable?.draw(this)
            drawLabels(this)
            drawPrevLabel(this)
            drawCurrentLabel(this)
        }
    }
    

    //Scale--------------------------------------------------------------------------
    private val bigLineWidth = 16F.toPx()
    private val distBwScaleAndLine = 14F.toPx()
    private val scalePaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        color = context.getColorCompat(R.color.grey_light)
        style = Paint.Style.STROKE
        strokeWidth = 1F.toPx()
    }
    private var scalePaddingVertical = 0.0F
    private fun drawScale(canvas: Canvas) {
        var left = 0F
        var right = 0F
        if (!invert) {
            left = width - circleRadius - curveStrokeWidth - distBwScaleAndLine
            right = left - bigLineWidth
        } else {
            left = circleRadius + curveStrokeWidth + distBwScaleAndLine
            right = left + bigLineWidth
        }
        canvas.drawLine(left, scalePaddingVertical, right, scalePaddingVertical, scalePaint)
        canvas.drawLine(
            left,
            height - scalePaddingVertical,
            right,
            height - scalePaddingVertical,
            scalePaint
        )

    }

    private fun drawLabels(canvas: Canvas) {
        var maxTextStart = labelX
        var minTextStart = labelX
        val textBounds = Rect()
        val maxString = maxRange.toInt().toString()
        val minString = minRange.toInt().toString()

        if (!invert) {
            val maxTextWidth = labelPaint.measureText(maxString)
            maxTextStart -= maxTextWidth
            val minTextWidth = labelPaint.measureText(minString)
            minTextStart -= minTextWidth
        }

        labelPaint.getTextBounds(maxString, 0, maxString.length, textBounds)
        canvas.drawText(
            maxString,
            maxTextStart,
            labelVerticalPadding + textBounds.height(),
            labelPaint
        )
        canvas.drawText(minString, minTextStart, height - labelVerticalPadding, labelPaint)
    }

    
    //Current Label-----------------------------------------------------------------
    private val currentLabelPadding = 12F.toPx()
    private val currentLabelTextSize = 22F.toPx()
    private val currentLabelTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        color = context.getColorCompat(R.color.black)
        textSize = currentLabelTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val currentLabelBoxPaint = Paint().apply {
        color = lineColor
        style = Paint.Style.STROKE
        strokeWidth = curveStrokeWidth
        isAntiAlias = true
    }
    private val whiteFillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColorCompat(R.color.white)
    }
    private val currentLabelBoxCornerRadius = 8F.toPx()
    private val currentLabelArrowLength = 12F.toPx()
    private val currentLabelBoxPath = Path()
    private val distBetweenBoxAndCircle = 8f.toPx()

    private fun calcCurrentLabelPath() {
        val textBounds = Rect()
        currentLabelTextPaint.getTextBounds("999", 0, 3, textBounds)
        val boxHeight = currentLabelPadding * 2 + textBounds.height()
        val boxWidth = currentLabelPadding * 2 + textBounds.width()
        val top = progressY - boxHeight / 2
        val bottom = progressY + boxHeight / 2

        currentLabelBoxPath.reset()
        if (!invert) {
            val left =
                width - (2 * circleRadius + 2 * curveStrokeWidth + boxWidth + currentLabelArrowLength + distBetweenBoxAndCircle)
            val right = left + boxWidth
            currentLabelBoxPath.apply {
                moveTo(left + currentLabelBoxCornerRadius, top)
                lineTo(right - currentLabelBoxCornerRadius, top)
                quadTo(right, top, right, top + currentLabelBoxCornerRadius)
                lineTo(right, progressY - currentLabelArrowLength / 2)
                lineTo(right + currentLabelArrowLength, progressY)
                lineTo(right, progressY + currentLabelArrowLength / 2)
                lineTo(right, bottom - currentLabelBoxCornerRadius)
                quadTo(
                    right,
                    bottom,
                    right - currentLabelBoxCornerRadius,
                    bottom
                )
                lineTo(left + currentLabelBoxCornerRadius, bottom)
                quadTo(left, bottom, left, bottom - currentLabelBoxCornerRadius)
                lineTo(left, top + currentLabelBoxCornerRadius)
                quadTo(left, top, left + currentLabelBoxCornerRadius, top)
                close()
            }
        } else {
            val left =
                2 * circleRadius + 2 * curveStrokeWidth + distBetweenBoxAndCircle + currentLabelArrowLength
            val right = left + boxWidth
            currentLabelBoxPath.apply {
                moveTo(left + currentLabelBoxCornerRadius, top)
                lineTo(right - currentLabelBoxCornerRadius, top)
                quadTo(right, top, right, top + currentLabelBoxCornerRadius)
                lineTo(right, bottom - currentLabelBoxCornerRadius)
                quadTo(right, bottom, right - currentLabelBoxCornerRadius, bottom)
                lineTo(left + currentLabelBoxCornerRadius, bottom)
                quadTo(left, bottom, left, bottom - currentLabelBoxCornerRadius)
                lineTo(left, progressY + currentLabelArrowLength / 2)
                lineTo(left - currentLabelArrowLength, progressY)
                lineTo(left, progressY - currentLabelArrowLength / 2)
                lineTo(left, top + currentLabelBoxCornerRadius)
                quadTo(left, top, left + currentLabelBoxCornerRadius, top)
                close()

            }
        }
    }

    
    //Prev Label-----------------------------------------------------------------------
    private val prevLabelPadding = 12F.toPx()
    private val prevLabelTextSize = 22F.toPx()
    private val prevLabelTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        color = context.getColorCompat(R.color.black)
        textSize = prevLabelTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val prevLabelBoxPaint = Paint().apply {
        color = context.getColorCompat(R.color.green)
        style = Paint.Style.STROKE
        strokeWidth = curveStrokeWidth
        isAntiAlias = true
    }
    private val prevLabelBoxCornerRadius = 8F.toPx()
    private val prevLabelArrowLength = 12F.toPx()
    private val prevLabelBoxPath = Path()
    private var prevProgress = maxRange.toInt()
    private var prevProgressY = 100F
    private fun calcPrevLabelPath() {
        val textBounds = Rect()
        prevLabelTextPaint.getTextBounds("999", 0, 3, textBounds)
        val boxHeight = prevLabelPadding * 2 + textBounds.height()
        val boxWidth = prevLabelPadding * 2 + textBounds.width()
        val top = prevProgressY - boxHeight / 2
        val bottom = prevProgressY + boxHeight / 2

        prevLabelBoxPath.reset()
        if (!invert) {
            val left =
                width - (2 * circleRadius + 2 * curveStrokeWidth + boxWidth + prevLabelArrowLength + distBetweenBoxAndCircle)
            val right = left + boxWidth
            prevLabelBoxPath.apply {
                moveTo(left + prevLabelBoxCornerRadius, top)
                lineTo(right - prevLabelBoxCornerRadius, top)
                quadTo(right, top, right, top + prevLabelBoxCornerRadius)
                lineTo(right, prevProgressY - prevLabelArrowLength / 2)
                lineTo(right + prevLabelArrowLength, prevProgressY)
                lineTo(right, prevProgressY + prevLabelArrowLength / 2)
                lineTo(right, bottom - prevLabelBoxCornerRadius)
                quadTo(
                    right,
                    bottom,
                    right - prevLabelBoxCornerRadius,
                    bottom
                )
                lineTo(left + prevLabelBoxCornerRadius, bottom)
                quadTo(left, bottom, left, bottom - prevLabelBoxCornerRadius)
                lineTo(left, top + prevLabelBoxCornerRadius)
                quadTo(left, top, left + prevLabelBoxCornerRadius, top)
                close()
            }
        } else {
            val left =
                2 * circleRadius + 2 * curveStrokeWidth + distBetweenBoxAndCircle + prevLabelArrowLength
            val right = left + boxWidth
            prevLabelBoxPath.apply {
                moveTo(left + prevLabelBoxCornerRadius, top)
                lineTo(right - prevLabelBoxCornerRadius, top)
                quadTo(right, top, right, top + prevLabelBoxCornerRadius)
                lineTo(right, bottom - prevLabelBoxCornerRadius)
                quadTo(right, bottom, right - prevLabelBoxCornerRadius, bottom)
                lineTo(left + prevLabelBoxCornerRadius, bottom)
                quadTo(left, bottom, left, bottom - prevLabelBoxCornerRadius)
                lineTo(left, prevProgressY + prevLabelArrowLength / 2)
                lineTo(left - prevLabelArrowLength, prevProgressY)
                lineTo(left, prevProgressY - prevLabelArrowLength / 2)
                lineTo(left, top + prevLabelBoxCornerRadius)
                quadTo(left, top, left + prevLabelBoxCornerRadius, top)
                close()

            }
        }
    }

    private fun drawCurrentLabel(canvas: Canvas) {

        canvas.drawPath(currentLabelBoxPath, whiteFillPaint)
        canvas.drawPath(currentLabelBoxPath, currentLabelBoxPaint)
        val currentLabel = progress.toString()
        val textBounds = Rect()
        currentLabelTextPaint.getTextBounds(currentLabel, 0, currentLabel.length, textBounds)
        val textStart = if (!invert) {
            val textWidth = textBounds.width()
            width - (2 * circleRadius + 2 * curveStrokeWidth + currentLabelArrowLength + currentLabelPadding + textWidth + distBetweenBoxAndCircle)
        } else {
            2 * circleRadius + 2 * curveStrokeWidth + distBetweenBoxAndCircle + currentLabelArrowLength + currentLabelPadding
        }
        canvas.drawText(
            currentLabel,
            textStart,
            progressY + textBounds.height() / 2,
            currentLabelTextPaint
        )
    }

    private fun drawPrevLabel(canvas: Canvas) {
        canvas.drawPath(prevLabelBoxPath, whiteFillPaint)
        canvas.drawPath(prevLabelBoxPath, prevLabelBoxPaint)
        val prevLabel = prevProgress.toString()
        val textBounds = Rect()
        prevLabelTextPaint.getTextBounds(prevLabel, 0, prevLabel.length, textBounds)
        val textStart = if (!invert) {
            val textWidth = textBounds.width()
            width - (2 * circleRadius + 2 * curveStrokeWidth + prevLabelArrowLength + prevLabelPadding + textWidth + distBetweenBoxAndCircle)
        } else {
            2 * circleRadius + 2 * curveStrokeWidth + distBetweenBoxAndCircle + prevLabelArrowLength + prevLabelPadding
        }
        canvas.drawText(
            prevLabel,
            textStart,
            prevProgressY + textBounds.height() / 2,
            prevLabelTextPaint
        )
    }

    private fun getCurrentLabel(): Int =
        (maxRange - (progressY - scalePaddingVertical) * (maxRange - minRange) / (height - 2 * scalePaddingVertical)).roundToInt()


    private var isCircleSelected = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (x < progressX + circleRadius &&
                    x > progressX - circleRadius &&
                    y < progressY + circleRadius &&
                    y > progressY - circleRadius
                ) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    isCircleSelected = true
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isCircleSelected) {
                    onMove(y)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isCircleSelected) {
                    isCircleSelected = false
                    return true
                }
            }
        }
        return false
    }

    private fun onMove(y: Float) {
        progressY = y
        var currentLabel = getCurrentLabel()
        if (currentLabel < minRange) {
            currentLabel = minRange.toInt()
            progressY = height - scalePaddingVertical
        }
        if (currentLabel > maxRange) {
            currentLabel = maxRange.toInt()
            progressY = scalePaddingVertical
        }

        calcCurrentLabelPath()
        invalidate()

        if (currentLabel == progress) {
            return
        }
        progress = currentLabel
        onProgressChangeListener?.onProgressChange(currentLabel)
    }

    private var progress = minRange.toInt()
    fun setProgress(progress: Int) {
        this.progress = progress
        this.prevProgress = progress
        if (height != 0) {
            setProgressDelayed()
        }
    }

    private fun setProgressDelayed() {
        progressY =
            (maxRange - progress) * (height - 2 * scalePaddingVertical) / (maxRange - minRange) + scalePaddingVertical
        prevProgressY = progressY
        calcCurrentLabelPath()
        calcPrevLabelPath()
        invalidate()
    }

    fun getProgress() = progress

    var onProgressChangeListener: OnProgressChangeListener? = null

    interface OnProgressChangeListener {
        fun onProgressChange(progress: Int)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = CurveSeekView.SavedState(superState)
        ss.progress = progress.toFloat()
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        setProgress(savedState.progress.roundToInt())
    }

    internal class SavedState : BaseSavedState {
        var progress: Float = 0.0F

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            progress = `in`.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(progress)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}