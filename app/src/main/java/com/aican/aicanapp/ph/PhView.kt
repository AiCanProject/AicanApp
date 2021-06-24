package com.aican.aicanapp.ph

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.PathParser
import com.aican.aicanapp.R
import com.aican.aicanapp.utils.getColorCompat
import com.aican.aicanapp.utils.toPx

class PhView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var vertical = true

    private val maxPh = 14
    private val minPh = 0

    private val scaleWidth = 30F.toPx()
    private val gap = (1.0F / (maxPh - minPh + 1)) / 2.0F
    private val arrowScale = 5F
    private val arrowHeight = 11F

    private var currentPh = 14F

    private val phColors = intArrayOf(
        context.getColorCompat(R.color.ph0),
        context.getColorCompat(R.color.ph1),
        context.getColorCompat(R.color.ph2),
        context.getColorCompat(R.color.ph3),
        context.getColorCompat(R.color.ph4),
        context.getColorCompat(R.color.ph5),
        context.getColorCompat(R.color.ph6),
        context.getColorCompat(R.color.ph7),
        context.getColorCompat(R.color.ph8),
        context.getColorCompat(R.color.ph9),
        context.getColorCompat(R.color.ph10),
        context.getColorCompat(R.color.ph11),
        context.getColorCompat(R.color.ph12),
        context.getColorCompat(R.color.ph13),
        context.getColorCompat(R.color.ph14),
    )

    private val scalePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = scaleWidth
        isAntiAlias = true
    }
    private val arrowPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = phColors[2]
    }

    init{
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PhView)
        try {
            vertical = typedArray.getBoolean(R.styleable.PhView_vertical, vertical)
        }finally {
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        var x0 = 0F
        var y0 = 0F
        var x1 = 0F
        var y1 = 0F
        if (vertical) {
            x0 = width / 2F
            y0 = 0F
            x1 = width / 2F
            y1 = height.toFloat()
        } else {
            x0 = 0F
            y0 = height / 2F
            x1 = width.toFloat()
            y1 = height / 2F
        }
        scalePaint.shader = LinearGradient(
            x0,
            y0,
            x1,
            y1,
            intArrayOf(
                phColors[0],
                phColors[0],
                phColors[1],
                phColors[2],
                phColors[3],
                phColors[4],
                phColors[5],
                phColors[6],
                phColors[7],
                phColors[8],
                phColors[9],
                phColors[10],
                phColors[11],
                phColors[12],
                phColors[13],
                phColors[14],
                phColors[14],
            ),
            floatArrayOf(
                0F,
                gap,
                3 * gap,
                5 * gap,
                7 * gap,
                9 * gap,
                11 * gap,
                13 * gap,
                15 * gap,
                17 * gap,
                19 * gap,
                21 * gap,
                23 * gap,
                25 * gap,
                27 * gap,
                29 * gap,
                1F
            ),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawScale(this)
            drawArrow(this)
            if (targetPh != currentPh) {
                moveArrow()
            }
        }
    }

    private fun moveArrow() {
        if (currentPh < targetPh) {
            currentPh = (currentPh + 0.1F).coerceAtMost(targetPh)
        } else if (currentPh > targetPh) {
            currentPh = (currentPh - 0.1F).coerceAtLeast(targetPh)
        }
        postInvalidateDelayed(1000 / 60)
    }


    private fun drawArrow(canvas: Canvas) {
        val path =
            PathParser.createPathFromPathData("m5.5174,1.2315c-0.2011,-0.0062 -0.3898,0.0965 -0.4934,0.2685l-4,6.6598c-0.2227,0.3695 0.0439,0.8401 0.476,0.8402l8,0c0.4321,-0.0001 0.6987,-0.4707 0.476,-0.8402l-4,-6.6598c-0.0973,-0.1614 -0.27,-0.2625 -0.4586,-0.2685z")
        val matrix = Matrix().apply {
            if (vertical) {
                preTranslate(
                    width / 2F + scaleWidth / 2F,
                    (currentPh * 2 + 1) * gap * height + (arrowHeight * arrowScale) / 2F
                )
            } else {
                preTranslate(
                    (currentPh * 2 + 1) * gap * width - (arrowHeight * arrowScale) / 2,
                    height / 2F + scaleWidth / 2F
                )
            }
            preScale(arrowScale, arrowScale)
            if (vertical)
                preRotate(270F)
        }
        path.transform(matrix)
        val colorIndex = currentPh.toInt()
        if (colorIndex.toFloat() != currentPh) {
            val startColor = phColors[colorIndex]
            val endColor = if (colorIndex + 1 < phColors.size) {
                phColors[colorIndex]
            } else {
                startColor
            }
            arrowPaint.color =
                ArgbEvaluator().evaluate(currentPh - colorIndex, startColor, endColor) as Int

        } else {
            arrowPaint.color = phColors[colorIndex]
        }
        canvas.drawPath(path, arrowPaint)
    }

    private fun drawScale(canvas: Canvas) {
        if (vertical) {
            canvas.drawLine(width / 2F, 0F, width / 2F, height.toFloat(), scalePaint)
        } else {
            canvas.drawLine(0F, height / 2F, width.toFloat(), height / 2F, scalePaint)
        }
    }

    private var targetPh = currentPh
    fun moveTo(ph: Float) {
        targetPh = ph
        if (targetPh > 14F) {
            targetPh = 14F
        } else if (targetPh < 0) {
            targetPh = 0F
        }
        invalidate()
    }

    fun setCurrentPh(ph: Float){
        currentPh = ph
        targetPh = ph
        invalidate()
    }

}