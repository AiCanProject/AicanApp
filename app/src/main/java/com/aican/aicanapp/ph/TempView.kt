package com.aican.aicanapp.ph

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.PathParser
import com.aican.aicanapp.R
import com.aican.aicanapp.utils.getColorCompat

class TempView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val scale = 10F
    private val iconHeight = 64
    private val iconWidth = 64

    private val maxRange = 100F
    private val minRange = 0F

    private val startColor = context.getColorCompat(R.color.blue)
    private val endColor = context.getColorCompat(R.color.orange)

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = startColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas?.apply {
            drawIcon(this)
        }
    }

    private fun drawIcon(canvas: Canvas) {
        val paths = listOf(
            PathParser.createPathFromPathData("m36.11,39.44l0,-28.7a6.71,6.71 0 0 0 -13.41,0l0,28.7c-8.82,6.29 -4.14,20.62 6.71,20.56c10.84,0.06 15.53,-14.27 6.7,-20.56zm-6.7,18.56c-9.18,0.07 -12.86,-12.21 -5.15,-17.19a1.15,1.15 0 0 0 0.44,-0.84l0,-29.23a4.71,4.71 0 0 1 9.41,0l0,29.19a1.22,1.22 0 0 0 0.45,0.84c7.7,4.97 4.02,17.23 -5.15,17.23z"),
            PathParser.createPathFromPathData("m30.41,41.45l0,-29.15a1,1 0 0 0 -2,0l0,29.15c-8.78,1.42 -7.95,14.14 1,14.36c8.95,-0.22 9.78,-12.94 1,-14.36zm-1,12.36c-6.9,-0.22 -6.9,-10.22 0,-10.44c6.9,0.22 6.9,10.23 0,10.44z")
        )
        val matrix = Matrix().apply {
            preTranslate(
                width / 2F - (iconWidth * scale) / 2F+28,
                height / 2F - (iconHeight * scale) / 2F
            )
            preScale(scale, scale)
        }

        for (path in paths) {
            path.transform(matrix)
            canvas.drawPath(path, paint)
        }
    }

    fun setTemp(temp: Int) {
        val fraction = when {
            temp.toFloat()<minRange -> {
                0F
            }
            temp.toFloat()>maxRange -> {
                1F
            }
            else -> {
                (temp.toFloat()-minRange)/(maxRange-minRange)
            }
        }
        paint.color = ArgbEvaluator().evaluate(
            fraction,
            startColor,
            endColor
        ) as Int
        invalidate()
    }
}