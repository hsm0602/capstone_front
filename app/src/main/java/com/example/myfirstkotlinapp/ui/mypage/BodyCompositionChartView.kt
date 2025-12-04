package com.example.myfirstkotlinapp.ui.mypage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BodyCompositionChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    enum class Metric {
        WEIGHT, BODY_FAT
    }

    data class Point(
        val dayIndex: Int,            // 0~6
        val weight: Float?,           // kg
        val bodyFatPercentage: Float? // %
    )

    private var metric: Metric = Metric.WEIGHT
    private var points: List<Point> = emptyList()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2260FF")
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#332260FF") // 20% 투명도 정도
        style = Paint.Style.FILL
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2260FF")
        style = Paint.Style.FILL
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D0D4DA")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val chartPaddingStart = 24f
    private val chartPaddingEnd = 16f
    private val chartPaddingTop = 16f
    private val chartPaddingBottom = 24f

    fun setMetric(metric: Metric) {
        this.metric = metric
        invalidate()
    }

    fun setData(points: List<Point>) {
        this.points = points.sortedBy { it.dayIndex }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()

        val chartLeft = chartPaddingStart
        val chartRight = width - chartPaddingEnd
        val chartTop = chartPaddingTop
        val chartBottom = height - chartPaddingBottom

        // 축
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint)
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        val values = points.mapNotNull {
            when (metric) {
                Metric.WEIGHT -> it.weight
                Metric.BODY_FAT -> it.bodyFatPercentage
            }
        }
        if (values.isEmpty()) return

        var minVal = values.minOrNull() ?: 0f
        var maxVal = values.maxOrNull() ?: 0f
        if (minVal == maxVal) {
            minVal -= 1f
            maxVal += 1f
        }

        val valueRange = max(0.1f, maxVal - minVal)

        val pointCount = 7
        val stepX = (chartRight - chartLeft) / (pointCount - 1).coerceAtLeast(1)

        val linePath = Path()
        val fillPath = Path()
        var first = true

        points.forEach { p ->
            val value = when (metric) {
                Metric.WEIGHT -> p.weight
                Metric.BODY_FAT -> p.bodyFatPercentage
            } ?: return@forEach

            val x = chartLeft + p.dayIndex * stepX
            val ratio = (value - minVal) / valueRange
            val y = chartBottom - (chartBottom - chartTop) * ratio

            if (first) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, chartBottom)
                fillPath.lineTo(x, y)
                first = false
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(chartRight, chartBottom)
        fillPath.close()
        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)

        points.forEach { p ->
            val value = when (metric) {
                Metric.WEIGHT -> p.weight
                Metric.BODY_FAT -> p.bodyFatPercentage
            } ?: return@forEach
            val x = chartLeft + p.dayIndex * stepX
            val ratio = (value - minVal) / valueRange
            val y = chartBottom - (chartBottom - chartTop) * ratio
            canvas.drawCircle(x, y, 6f, pointPaint)
        }
    }
}
