package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ChartPoint(
    val label: String, // e.g. "Aug 12", "Mon"
    val value: Float,
    val date: String = ""
)

@Composable
fun FitLineChart(
    points: List<ChartPoint>,
    unit: String,
    lineColor: Color = FitCyan,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val animatedFraction = remember { Animatable(0f) }

    LaunchedEffect(points) {
        selectedPointIndex = null
        animatedFraction.snapTo(0f)
        animatedFraction.animateTo(1f, animationSpec = tween(750))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FitCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header stats & selected point inspector
            val selected = selectedPointIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selected != null) "${selected.value} $unit" else "No Data",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTextPrimary
                    )
                    Text(
                        text = selected?.label ?: "Tap points to inspect",
                        fontSize = 12.sp,
                        color = FitCyan
                    )
                }

                if (points.size >= 2) {
                    val first = points.first().value
                    val last = points.last().value
                    val diff = last - first
                    val diffStr = if (diff >= 0) "+${String.format("%.1f", diff)}" else String.format("%.1f", diff)
                    val isPositive = diff >= 0

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isPositive) Color(0xFF104832) else Color(0xFF4A1515),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$diffStr $unit",
                            color = if (isPositive) FitGreen else FitRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data entries recorded yet", color = FitTextSecondary, fontSize = 13.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { tapOffset ->
                                    val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                    val closestIndex = ((tapOffset.x) / stepX)
                                        .roundToInt()
                                        .coerceIn(0, points.size - 1)
                                    selectedPointIndex = closestIndex
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height - 24.dp.toPx()
                        val paddingBottom = 24.dp.toPx()

                        val values = points.map { it.value }
                        val minVal = (values.minOrNull() ?: 0f) * 0.95f
                        val maxVal = ((values.maxOrNull() ?: 100f) * 1.05f).coerceAtLeast(minVal + 1f)
                        val valRange = maxVal - minVal

                        val stepX = if (points.size > 1) w / (points.size - 1) else w / 2

                        // Draw Grid Horizontal Lines
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val y = h - (h * (i.toFloat() / gridCount))
                            drawLine(
                                color = FitCardBorder.copy(alpha = 0.6f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        }

                        // Coordinates for points
                        val coords = points.mapIndexed { idx, p ->
                            val x = if (points.size > 1) idx * stepX else w / 2
                            val normalized = ((p.value - minVal) / valRange).coerceIn(0f, 1f)
                            val y = h - (normalized * h * animatedFraction.value)
                            Offset(x, y)
                        }

                        // Path for Line & Area
                        val linePath = Path()
                        val areaPath = Path()

                        if (coords.isNotEmpty()) {
                            linePath.moveTo(coords[0].x, coords[0].y)
                            areaPath.moveTo(coords[0].x, h)
                            areaPath.lineTo(coords[0].x, coords[0].y)

                            for (i in 0 until coords.size - 1) {
                                val p0 = coords[i]
                                val p1 = coords[i + 1]
                                val controlX1 = p0.x + (p1.x - p0.x) / 2
                                val controlY1 = p0.y
                                val controlX2 = p0.x + (p1.x - p0.x) / 2
                                val controlY2 = p1.y

                                linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                                areaPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                            }

                            areaPath.lineTo(coords.last().x, h)
                            areaPath.close()

                            // Draw Gradient Area under curve
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        lineColor.copy(alpha = 0.35f),
                                        lineColor.copy(alpha = 0.0f)
                                    ),
                                    startY = 0f,
                                    endY = h
                                )
                            )

                            // Draw Glowing Stroke Line
                            drawPath(
                                path = linePath,
                                color = lineColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Draw Data Points
                            coords.forEachIndexed { idx, pt ->
                                val isSel = (idx == selectedPointIndex)
                                drawCircle(
                                    color = if (isSel) Color.White else lineColor,
                                    radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
                                    center = pt
                                )
                                if (isSel) {
                                    drawCircle(
                                        color = lineColor,
                                        radius = 9.dp.toPx(),
                                        center = pt,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }

                // X-Axis Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayed = if (points.size <= 6) points else listOf(points.first(), points[points.size / 2], points.last())
                    displayed.forEach { p ->
                        Text(
                            text = p.label,
                            fontSize = 11.sp,
                            color = FitTextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun Float.roundToInt(): Int = (this + 0.5f).toInt()
