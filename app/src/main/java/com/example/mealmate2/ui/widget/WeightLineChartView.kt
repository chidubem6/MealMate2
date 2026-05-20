package com.example.mealmate2.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.google.android.material.R as MaterialR

class WeightLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val dotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    // List of (epochDay, weightKg)
    var entries: List<Pair<Long, Float>> = emptyList()
        set(value) { field = value; invalidate() }

    private fun resolveAttrColor(attr: Int): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(attr, tv, true)
        return tv.data
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val primary = resolveAttrColor(android.R.attr.colorPrimary)
        val onSurfaceVariant = resolveAttrColor(MaterialR.attr.colorOnSurfaceVariant)
        val surface = resolveAttrColor(MaterialR.attr.colorSurface)
        linePaint.color = primary
        dotPaint.color = primary
        dotBorderPaint.color = surface
        gridPaint.color = resolveAttrColor(MaterialR.attr.colorOutlineVariant)
        labelPaint.color = onSurfaceVariant
    }

    override fun onDraw(canvas: Canvas) {
        if (entries.size < 2) {
            drawEmptyState(canvas)
            return
        }

        val padLeft = 8f
        val padRight = 8f
        val padTop = 16f
        val padBottom = 32f

        val chartW = width - padLeft - padRight
        val chartH = height - padTop - padBottom

        val minWeight = entries.minOf { it.second }
        val maxWeight = entries.maxOf { it.second }
        val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)
        val minDay = entries.minOf { it.first }
        val maxDay = entries.maxOf { it.first }
        val dayRange = (maxDay - minDay).coerceAtLeast(1L)

        labelPaint.textSize = 28f
        linePaint.strokeWidth = width * 0.012f
        val dotRadius = width * 0.018f

        // Draw horizontal grid lines at min and max
        listOf(minWeight, maxWeight).forEach { w ->
            val y = padTop + chartH * (1f - (w - minWeight) / weightRange)
            canvas.drawLine(padLeft, y, padLeft + chartW, y, gridPaint)
            canvas.drawText("%.1f".format(w), padLeft + chartW + 4f, y + 4f, labelPaint.apply { textAlign = Paint.Align.LEFT })
        }
        labelPaint.textAlign = Paint.Align.CENTER

        // Build path
        val path = Path()
        entries.forEachIndexed { i, (day, kg) ->
            val x = padLeft + chartW * ((day - minDay).toFloat() / dayRange)
            val y = padTop + chartH * (1f - (kg - minWeight) / weightRange)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)

        // Draw dots
        entries.forEach { (day, kg) ->
            val x = padLeft + chartW * ((day - minDay).toFloat() / dayRange)
            val y = padTop + chartH * (1f - (kg - minWeight) / weightRange)
            canvas.drawCircle(x, y, dotRadius + 3f, dotBorderPaint)
            canvas.drawCircle(x, y, dotRadius, dotPaint)
        }
    }

    private fun drawEmptyState(canvas: Canvas) {
        labelPaint.textSize = 36f
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            if (entries.isEmpty()) "No weight data yet" else "Log on more days to see a trend",
            width / 2f,
            height / 2f + 12f,
            labelPaint
        )
    }
}
