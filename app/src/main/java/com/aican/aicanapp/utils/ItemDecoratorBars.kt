package com.aican.aicanapp.utils

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ItemDecoratorBars(
    private val length: Int,
    private val padding: Int,
    private val indicatorHeight: Int,
    @ColorInt val colorInactive: Int,
    @ColorInt val colorActive: Int
) : RecyclerView.ItemDecoration() {
    private val inactivePaint = Paint()
    private val activePaint = Paint()
    private val interpolator = AccelerateDecelerateInterpolator()
    private val TAG = "ItemDecoratorBar"

    init {
        val strokeWidth = Resources.getSystem().displayMetrics.density * 2

        with(inactivePaint) {
            strokeCap = Paint.Cap.ROUND
            this.strokeWidth = strokeWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = colorInactive
        }
        with(activePaint) {
            strokeCap = Paint.Cap.ROUND
            this.strokeWidth = strokeWidth
            style = Paint.Style.FILL
            isAntiAlias = true
            color = colorActive
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val adapter = parent.adapter ?: return
        val itemCount = adapter.itemCount

        val totalLength = length * itemCount
        val paddingBetweenItems = 0.coerceAtLeast(itemCount - 1) * padding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        val indicatorPosY = parent.height - indicatorHeight / 2f - 30

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)

        var activePosition = if (parent.layoutManager is GridLayoutManager) {
            (parent.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
        } else if (parent.layoutManager is LinearLayoutManager) {
            (parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        } else {
            null
        } ?: return
        val completeActivePosition = if (parent.layoutManager is GridLayoutManager) {
            (parent.layoutManager as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        } else if (parent.layoutManager is LinearLayoutManager) {
            (parent.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        } else {
            null
        } ?: return
        if (completeActivePosition != RecyclerView.NO_POSITION) {
            activePosition = completeActivePosition
        }

        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        val activeChild = parent.layoutManager?.findViewByPosition(activePosition) ?: return
        val left = activeChild.left
        val width = activeChild.width

        val progress = interpolator.getInterpolation(left * -1 / width.toFloat())

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount)

    }

    private fun drawHighlights(
        canvas: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        activePosition: Int,
        progress: Float,
        itemCount: Int
    ) {
        Log.e(TAG, "progress: $progress")
        val itemWidth = length + padding
        if (progress == 0f) {
            val highlightStart = indicatorStartX + itemWidth * activePosition
            canvas.drawLine(
                highlightStart,
                indicatorPosY,
                highlightStart + length,
                indicatorPosY,
                activePaint
            )
        } else {
            val partialLength = (length + padding) * progress
            var highlightStart = indicatorStartX + itemWidth * activePosition + partialLength
            canvas.drawLine(
                highlightStart,
                indicatorPosY,
                highlightStart + length.toFloat(),
                indicatorPosY,
                activePaint
            )

//            if (activePosition < itemCount - 1) {
//                highlightStart += itemWidth
//                canvas.drawLine(
//                    highlightStart,
//                    indicatorPosY,
//                    highlightStart + partialLength,
//                    indicatorPosY,
//                    activePaint
//                )
//            }
        }

    }


    private fun drawInactiveIndicators(
        canvas: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        val itemWidth = length + padding

        var start = indicatorStartX
        for (i in 0 until itemCount) {
            canvas.drawLine(start, indicatorPosY, start + length, indicatorPosY, inactivePaint)
            start += itemWidth
        }
    }


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
//        outRect.bottom = indicatorHeight
    }
}