package com.example.mealmate2.ui.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class MacroPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(35, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val bounds = RectF()

    private var carbsG = 0f
    private var fatG = 0f
    private var proteinG = 0f

    fun setMacros(carbsG: Float, fatG: Float, proteinG: Float) {
        this.carbsG = carbsG.coerceAtLeast(0f)
        this.fatG = fatG.coerceAtLeast(0f)
        this.proteinG = proteinG.coerceAtLeast(0f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val inset = paddingLeft.coerceAtLeast(paddingTop).toFloat()
        val left = (width - size) / 2f + inset
        val top = (height - size) / 2f + inset
        bounds.set(left, top, left + size - inset * 2f, top + size - inset * 2f)

        val total = carbsG + fatG + proteinG
        if (total <= 0f) {
            canvas.drawOval(bounds, emptyPaint)
            return
        }

        var startAngle = -90f
        listOf(
            carbsG to Color.rgb(76, 175, 80),
            fatG to Color.rgb(255, 152, 0),
            proteinG to Color.rgb(33, 150, 243)
        ).forEach { (value, color) ->
            if (value > 0f) {
                val sweep = value / total * 360f
                segmentPaint.color = color
                canvas.drawArc(bounds, startAngle, sweep, true, segmentPaint)
                startAngle += sweep
            }
        }
    }
}
