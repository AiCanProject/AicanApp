package com.aican.aicanapp.pumpController

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.aican.aicanapp.R
import com.aican.aicanapp.utils.getColorCompat
import com.aican.aicanapp.utils.toPx

class ProgressBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    var percent = 0

    private val progressBarHeight = 36F.toPx()
    private val cornerRadius = progressBarHeight / 2F
    private val horizontalPadding = 8F.toPx()

    //    private val animationDuration = 300
//    private val animationRepeatDelay = 300
    private val progressTextSize = 24F.toPx()

    private var centerX = 0
    private var centerY = 0

    private val activeColor = context.getColorCompat(R.color.colorPrimary)
    private val inactiveStrokeColor = context.getColorCompat(R.color.grey_light)
    private val inactiveFillColor = context.getColorCompat(R.color.white)
    private val textColor = context.getColorCompat(R.color.blueDarkAlpha)

    private val activePaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = activeColor
        strokeWidth = 1F.toPx()
        isAntiAlias = true
    }
    private val inactiveStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = inactiveStrokeColor
        strokeWidth = 1F.toPx()
        isAntiAlias = true
    }
    private val inactiveFillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = inactiveFillColor
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        color = textColor
        textSize = progressTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }

    private val inactivePath = Path()
    private val activePath = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerX = w / 2
        centerY = h / 2

        calcInactivePath()
        calcActivePath()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawBackground(this)
            drawProgress(this)
            drawProgressLabel(this)
        }
    }


    private fun drawProgressLabel(canvas: Canvas) {
        val progressText = "$percent%"
        val bounds = Rect()
        textPaint.getTextBounds(progressText, 0, progressText.length, bounds)
        canvas.drawText(
            progressText,
            centerX - bounds.width() / 2F,
            centerY + bounds.height() / 2F,
            textPaint
        )
    }

    private fun drawProgress(canvas: Canvas) {
        canvas.drawPath(activePath, activePaint)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawPath(inactivePath, inactiveStrokePaint)
        canvas.drawPath(inactivePath, inactiveFillPaint)
    }

    private fun calcInactivePath() {
        inactivePath.apply {
            reset()
            addRoundRect(
                horizontalPadding,
                centerY - progressBarHeight / 2,
                width - horizontalPadding,
                centerY + progressBarHeight / 2,
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
            close()
        }
    }

    private fun calcActivePath() {
        val activeWidth = (percent / 100F) * (width - 2 * horizontalPadding - 2 * cornerRadius)
        val left = horizontalPadding
        val right = horizontalPadding + 2 * cornerRadius + activeWidth
        val top = centerY - progressBarHeight / 2
        val bottom = centerY + progressBarHeight / 2
        activePath.apply {
            reset()
//            moveTo(horizontalPadding+cornerRadius, centerY-progressBarHeight/2)
//            lineTo(, centerY-progressBarHeight/2)
//            quadTo(activeWidth-horizontalPadding, centerY.toFloat()-progressBarHeight/2, activeWidth-horizontalPadding, centerY.toFloat())
//            quadTo(activeWidth-horizontalPadding, centerY+progressBarHeight/2, activeWidth-horizontalPadding-cornerRadius, centerY+progressBarHeight/2)
//            lineTo(horizontalPadding+cornerRadius, centerY+progressBarHeight/2)
//            quadTo(horizontalPadding, centerY.toFloat()+progressBarHeight/2, horizontalPadding, centerY.toFloat())
//            quadTo(horizontalPadding, centerY-progressBarHeight/2, horizontalPadding+cornerRadius, centerY-progressBarHeight/2)
            addRoundRect(
                left,
                top,
                right,
                bottom,
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
            close()
        }
    }

    fun setProgress(progress: Int) {
        this.percent = progress
        calcActivePath()
        invalidate()
    }
}