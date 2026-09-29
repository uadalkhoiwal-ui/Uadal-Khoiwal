package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CandlePoint
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChartBackground
import com.example.ui.theme.ChartGridLine
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenBullish
import com.example.ui.theme.RedBearish
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun NativeCandleChart(
    candles: List<CandlePoint>,
    currentPrice: Double,
    decimals: Int = 2,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ChartBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading chart data...", color = TextSecondary)
        }
        return
    }

    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    val minPrice = candles.minOfOrNull { it.low } ?: 0f
    val maxPrice = candles.maxOfOrNull { it.high } ?: 100f
    val priceRange = max(0.0001f, maxPrice - minPrice)

    val maxVolume = candles.maxOfOrNull { it.volume } ?: 1f

    val selectedCandle = selectedCandleIndex?.let { idx ->
        candles.getOrNull(idx)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChartBackground)
    ) {
        // Top info bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDarkVariant.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayCandle = selectedCandle ?: candles.last()
            val isGreen = displayCandle.close >= displayCandle.open

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "O: ${formatPrice(displayCandle.open.toDouble(), decimals)}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "H: ${formatPrice(displayCandle.high.toDouble(), decimals)}",
                    color = GreenBullish,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "L: ${formatPrice(displayCandle.low.toDouble(), decimals)}",
                    color = RedBearish,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "C: ${formatPrice(displayCandle.close.toDouble(), decimals)}",
                    color = if (isGreen) GreenBullish else RedBearish,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "EMA(20) • Vol",
                color = CyanAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Chart Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectTapGestures(
                            onPress = { offset ->
                                val step = size.width / candles.size
                                val idx = (offset.x / step).toInt().coerceIn(0, candles.size - 1)
                                selectedCandleIndex = idx
                                tryAwaitRelease()
                                selectedCandleIndex = null
                            }
                        )
                    }
                    .pointerInput(candles) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val step = size.width / candles.size
                                val idx = (offset.x / step).toInt().coerceIn(0, candles.size - 1)
                                selectedCandleIndex = idx
                            },
                            onDrag = { change, _ ->
                                val step = size.width / candles.size
                                val idx = (change.position.x / step).toInt().coerceIn(0, candles.size - 1)
                                selectedCandleIndex = idx
                            },
                            onDragEnd = {
                                selectedCandleIndex = null
                            },
                            onDragCancel = {
                                selectedCandleIndex = null
                            }
                        )
                    }
            ) {
                val width = size.width
                val height = size.height
                val priceChartHeight = height * 0.78f
                val volumeChartHeight = height * 0.22f
                val volumeTop = priceChartHeight

                // Grid lines (Horizontal price levels)
                val gridLevels = 4
                for (i in 0..gridLevels) {
                    val y = (priceChartHeight / gridLevels) * i
                    drawLine(
                        color = ChartGridLine,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    val priceVal = maxPrice - (i.toFloat() / gridLevels) * priceRange
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatPrice(priceVal.toDouble(), decimals),
                        topLeft = Offset(width - 70f, y + 2f),
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                val count = candles.size
                val candleWidth = (width / count) * 0.65f
                val candleStep = width / count

                // Draw EMA 20 line
                val emaPeriod = 12
                val emaPath = Path()
                var emaInitialized = false
                var prevEma = 0f
                val multiplier = 2f / (emaPeriod + 1)

                val points = mutableListOf<Offset>()
                candles.forEachIndexed { i, candle ->
                    val x = i * candleStep + candleStep / 2f
                    if (!emaInitialized) {
                        prevEma = candle.close
                        emaInitialized = true
                    } else {
                        prevEma = (candle.close - prevEma) * multiplier + prevEma
                    }
                    val y = priceChartHeight - ((prevEma - minPrice) / priceRange) * priceChartHeight
                    points.add(Offset(x, y.coerceIn(0f, priceChartHeight)))
                }

                if (points.isNotEmpty()) {
                    emaPath.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        emaPath.lineTo(points[i].x, points[i].y)
                    }
                    drawPath(
                        path = emaPath,
                        color = CyanAccent.copy(alpha = 0.85f),
                        style = Stroke(width = 2.5f)
                    )
                }

                // Draw volume and candles
                candles.forEachIndexed { i, c ->
                    val centerX = i * candleStep + candleStep / 2f
                    val isBullish = c.close >= c.open
                    val color = if (isBullish) GreenBullish else RedBearish

                    // Volume bar
                    val volHeight = (c.volume / maxVolume) * volumeChartHeight
                    val volTop = height - volHeight
                    drawRect(
                        color = color.copy(alpha = 0.35f),
                        topLeft = Offset(centerX - candleWidth / 2f, volTop),
                        size = Size(candleWidth, volHeight)
                    )

                    // Price Wick
                    val highY = priceChartHeight - ((c.high - minPrice) / priceRange) * priceChartHeight
                    val lowY = priceChartHeight - ((c.low - minPrice) / priceRange) * priceChartHeight
                    drawLine(
                        color = color,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 2f
                    )

                    // Candle Body
                    val openY = priceChartHeight - ((c.open - minPrice) / priceRange) * priceChartHeight
                    val closeY = priceChartHeight - ((c.close - minPrice) / priceRange) * priceChartHeight
                    val bodyTop = min(openY, closeY)
                    val bodyHeight = max(2.5f, abs(closeY - openY))

                    drawRect(
                        color = color,
                        topLeft = Offset(centerX - candleWidth / 2f, bodyTop),
                        size = Size(candleWidth, bodyHeight)
                    )
                }

                // Current Price Horizontal Line
                val currentY = priceChartHeight - ((currentPrice.toFloat() - minPrice) / priceRange) * priceChartHeight
                if (currentY in 0f..priceChartHeight) {
                    drawLine(
                        color = CyanAccent,
                        start = Offset(0f, currentY),
                        end = Offset(width, currentY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawRect(
                        color = CyanAccent,
                        topLeft = Offset(width - 76f, currentY - 11f),
                        size = Size(76f, 22f)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatPrice(currentPrice, decimals),
                        topLeft = Offset(width - 72f, currentY - 8f),
                        style = TextStyle(
                            color = BackgroundDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Crosshair cursor if user is dragging/tapping
                selectedCandleIndex?.let { idx ->
                    val crosshairX = idx * candleStep + candleStep / 2f
                    val sel = candles.getOrNull(idx)
                    if (sel != null) {
                        val crosshairY = priceChartHeight - ((sel.close - minPrice) / priceRange) * priceChartHeight
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = Offset(crosshairX, 0f),
                            end = Offset(crosshairX, height),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = Offset(0f, crosshairY),
                            end = Offset(width, crosshairY),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatPrice(price: Double, decimals: Int): String {
    return if (decimals == 4) {
        String.format(Locale.US, "%.4f", price)
    } else if (price >= 1000) {
        String.format(Locale.US, "%,.2f", price)
    } else {
        String.format(Locale.US, "%.2f", price)
    }
}
