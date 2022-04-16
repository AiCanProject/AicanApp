package com.aican.aicanapp.pumpController

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.aican.aicanapp.R
import com.aican.aicanapp.utils.adjustAlpha
import com.aican.aicanapp.utils.getColorCompat
import com.aican.aicanapp.utils.toPx
import kotlin.math.roundToInt

class HorizontalSlider(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var onProgressChangeListener: OnProgressChangeListener? = null

    private var progress = 0.0F

    private var progressX = 0F
    private var progressY = 0F
    private var boxHeight = 0F
    private var boxWidth = 0F
    private var labelPadding = 0F
    var disabled = false
        set(value) {
            field = value
            setColors()
        }

    private var minRange = 0.0F
    private var maxRange = 50.0F
    private val strokeWidth = 4F.toPx()
    private val labelTextSize = 16F.toPx()
    private val currentLabelTextSize = 18F.toPx()
    private val textPadding = 14F.toPx()
    private val labelPaddingFactor = 0.1F
    private val sliderPaddingFactor = 0.15F
    private val scaleWidth = 8F.toPx()
    private val distBwScaleAndSlider = 8F.toPx()
    private val distBwScaleAndLabel = 8F.toPx()
    private val boxCornerRadius = 4F.toPx()

    private val sliderColor = context.getColorCompat(R.color.colorAccent)
    private val textColor = context.getColorCompat(R.color.grey_dark)

    private val boxPath = Path()

    private val sliderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = this@HorizontalSlider.strokeWidth
        color = sliderColor
        strokeCap = Paint.Cap.ROUND
    }
    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = this@HorizontalSlider.strokeWidth
        color = sliderColor
        strokeCap = Paint.Cap.ROUND
    }
    private val labelPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = this@HorizontalSlider.labelTextSize
        color = textColor
        strokeCap = Paint.Cap.ROUND
    }
    private val currentLabelPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = this@HorizontalSlider.currentLabelTextSize
        color = textColor
        strokeCap = Paint.Cap.ROUND
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val scalePaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        color = context.getColorCompat(R.color.grey_light)
        style = Paint.Style.STROKE
        strokeWidth = 1F.toPx()
    }
    private val whiteFillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColorCompat(R.color.white)
    }

    private fun setColors() {
        if (!disabled) {
            sliderPaint.color = sliderColor
            boxPaint.color = sliderColor
        } else {
            sliderPaint.color = sliderColor.adjustAlpha(0.7F)
            boxPaint.color = sliderColor.adjustAlpha(0.7F)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        labelPadding = labelPaddingFactor * w
        progressX = calcProgressX()
        progressY = h / 2.0F
        val bounds = Rect()
        labelPaint.getTextBounds("999", 0, 3, bounds)
        boxHeight = 2 * textPadding + bounds.height()
        boxWidth = 2 * textPadding + bounds.width()
        sliderPaint.shader = LinearGradient(
            0F,
            progressY,
            w.toFloat(),
            progressY,
            intArrayOf(
                sliderColor.adjustAlpha(0.0F),
                sliderColor,
                sliderColor,
                sliderColor.adjustAlpha(0.0F)
            ),
            floatArrayOf(
                0.0F,
                sliderPaddingFactor,
                1.0F - sliderPaddingFactor,
                1.0F
            ),
            Shader.TileMode.CLAMP
        )

        calcBoxPath()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawSlider(this)
            drawLabels(this)
            drawCurrentLabel(this)
        }
    }

    private fun drawCurrentLabel(canvas: Canvas) {
        canvas.drawPath(boxPath, whiteFillPaint)
        canvas.drawPath(boxPath, boxPaint)

        val text = String.format("%.2f", progress)
        val bounds = Rect()
        currentLabelPaint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(
            text,
            progressX - bounds.width() / 2,
            progressY + bounds.height() / 2,
            currentLabelPaint
        )
    }

    private fun drawLabels(canvas: Canvas) {
        canvas.drawLine(
            labelPadding,
            progressY + distBwScaleAndSlider,
            labelPadding,
            progressY + distBwScaleAndSlider + scaleWidth,
            scalePaint
        )
        canvas.drawLine(
            width - labelPadding,
            progressY + distBwScaleAndSlider,
            width - labelPadding,
            progressY + distBwScaleAndSlider + scaleWidth,
            scalePaint
        )

        var text = String.format("%.2f", minRange)
        val bounds = Rect()
        labelPaint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(
            text,
            labelPadding - bounds.width() / 2,
            progressY + distBwScaleAndSlider + scaleWidth + distBwScaleAndLabel + bounds.height(),
            labelPaint
        )

        text = String.format("%.2f", maxRange)
        labelPaint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(
            text,
            width - (labelPadding + bounds.width() / 2),
            progressY + distBwScaleAndSlider + scaleWidth + distBwScaleAndLabel + bounds.height(),
            labelPaint
        )

        text = "(mL/min)"
        labelPaint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(
            text,
            width / 2F - (bounds.width() / 2F),
            progressY + distBwScaleAndSlider + scaleWidth + distBwScaleAndLabel + bounds.height(),
            labelPaint
        )
    }

    private fun drawSlider(canvas: Canvas) {
        canvas.drawLine(0F, progressY, width.toFloat(), progressY, sliderPaint)
    }

    private fun calcBoxPath() {
        val left = progressX - boxWidth / 2
        val top = progressY - boxHeight / 2
        val right = progressX + boxWidth / 2
        val bottom = progressY + boxHeight / 2
        val corner = boxCornerRadius

        boxPath.apply {
            reset()
            moveTo(left + corner, top)
            lineTo(right - corner, top)
            quadTo(right, top, right, top + corner)
            lineTo(right, bottom - corner)
            quadTo(right, bottom, right - corner, bottom)
            lineTo(left + corner, bottom)
            quadTo(left, bottom, left, bottom - corner)
            lineTo(left, top + corner)
            quadTo(left, top, left + corner, top)
            close()
        }
    }

    private var isSliderSelected = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (disabled) return false
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (x > progressX - boxWidth / 2 &&
                    x < progressX + boxWidth / 2 &&
                    y > progressY - boxWidth / 2 &&
                    y < progressY + boxHeight / 2
                ) {
                    isSliderSelected = true
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSliderSelected) {
                    onMove(x)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isSliderSelected) {
                    isSliderSelected = false
                    return true
                }
            }
        }
        return false
    }

    private fun onMove(x: Float) {
        val prev = progress
        progressX = x
        progress = calcProgress()
        if (progress < minRange) {
            progress = minRange
            progressX = labelPadding
        } else if (progress > maxRange) {
            progress = maxRange
            progressX = width - labelPadding
        }

        calcBoxPath()
        invalidate()
        if (progress == prev) {
            return
        }
        onProgressChangeListener?.onProgressChange(progress)
    }
    //private var progress = minRange
    fun setProgress(p: Float) {

        var progress = p
        if (progress < minRange) {
            progress = minRange.toFloat()
        } else if (progress > maxRange) {
            maxRange = progress
//            progress = maxRange.toInt()
        }
        this.progress = progress
//        this.prevProgress = progress
        if (height != 0) {
            setProgressDelayed()
        }
    }
    private fun setProgressDelayed() {
        progressY =
            (maxRange - progress) * (height - 2 * labelPadding) / (maxRange - minRange) + labelPadding
//        prevProgressY = progressY
        calcBoxPath()
//        calcPrevLabelPath()
        invalidate()
    }



    fun getProgress() = progress

    interface OnProgressChangeListener {
        fun onProgressChange(progress: Float)
    }

    private fun calcProgressX(): Float {
        return labelPadding + (progress - minRange) * (width.toFloat() - 2 * labelPadding) / (maxRange - minRange)
    }

    private fun calcProgress(): Float {
        return ((progressX - labelPadding) * (maxRange - minRange) / (width.toFloat() - 2 * labelPadding) + minRange)
    }
}