package com.example.mealmate2.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.google.android.material.R as MaterialR

class CalorieRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val primaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val ovalRect = RectF()

    var ringProgress: Float = 0f
        set(value) { field = value.coerceIn(0f, 1f); invalidate() }

    var centerText: String = "0"
        set(value) { field = value; invalidate() }

    var subText: String = "kcal left"
        set(value) { field = value; invalidate() }

    private fun resolveAttrColor(attr: Int): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(attr, tv, true)
        return tv.data
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // colorPrimary is a platform attr (API 21); Material-specific attrs use MaterialR
        trackPaint.color = resolveAttrColor(MaterialR.attr.colorOutlineVariant)
        progressPaint.color = resolveAttrColor(android.R.attr.colorPrimary)
        primaryTextPaint.color = resolveAttrColor(MaterialR.attr.colorOnSurface)
        secondaryTextPaint.color = resolveAttrColor(MaterialR.attr.colorOnSurfaceVariant)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val stroke = w * 0.1f
        trackPaint.strokeWidth = stroke
        progressPaint.strokeWidth = stroke
        val inset = stroke / 2f
        ovalRect.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        // 270° horseshoe arc, gap at the bottom (starts at 7:30, ends at 4:30)
        canvas.drawArc(ovalRect, 135f, 270f, false, trackPaint)

        val sweep = 270f * ringProgress
        if (sweep > 0f) {
            canvas.drawArc(ovalRect, 135f, sweep, false, progressPaint)
        }

        val cx = width / 2f
        val cy = height / 2f

        primaryTextPaint.textSize = width * 0.2f
        val primaryBaseline = cy + primaryTextPaint.textSize * 0.35f
        canvas.drawText(centerText, cx, primaryBaseline, primaryTextPaint)

        secondaryTextPaint.textSize = width * 0.09f
        canvas.drawText(subText, cx, primaryBaseline + secondaryTextPaint.textSize * 1.5f, secondaryTextPaint)
    }
}
