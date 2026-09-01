package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FitCyan
import com.example.ui.theme.FitGreen

@Composable
fun ExerciseFigureIcon(
    exerciseName: String,
    modifier: Modifier = Modifier.size(44.dp),
    tint: Color = FitGreen
) {
    val nameLower = exerciseName.lowercase()
    Canvas(modifier = modifier) {
        when {
            nameLower.contains("squat") -> drawSquatFigure(tint)
            nameLower.contains("push") -> drawPushUpFigure(tint)
            nameLower.contains("plank") -> drawPlankFigure(tint)
            nameLower.contains("press") || nameLower.contains("dumbbell") || nameLower.contains("curl") -> drawDumbbellFigure(tint)
            nameLower.contains("pull") -> drawPullUpFigure(tint)
            else -> drawGeneralFigure(tint)
        }
    }
}

private fun DrawScope.drawSquatFigure(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Head
    drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.65f, h * 0.22f))

    // Torso, hips, legs in squatting angle
    val bodyPath = Path().apply {
        // Torso leaned forward
        moveTo(w * 0.65f, h * 0.32f)
        lineTo(w * 0.52f, h * 0.52f) // Hips back
        // Thighs horizontal
        lineTo(w * 0.36f, h * 0.56f) // Knees forward
        // Shin down
        lineTo(w * 0.38f, h * 0.82f) // Feet
    }
    drawPath(bodyPath, color = color, style = stroke)

    // Arms stretched forward
    val armPath = Path().apply {
        moveTo(w * 0.60f, h * 0.38f)
        lineTo(w * 0.78f, h * 0.44f)
        lineTo(w * 0.86f, h * 0.48f)
    }
    drawPath(armPath, color = color, style = stroke)
}

private fun DrawScope.drawPushUpFigure(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Head
    drawCircle(color = color, radius = 3.8.dp.toPx(), center = Offset(w * 0.78f, h * 0.42f))

    // Body plank angle
    val bodyPath = Path().apply {
        moveTo(w * 0.72f, h * 0.48f) // Shoulders
        lineTo(w * 0.45f, h * 0.60f) // Hips
        lineTo(w * 0.18f, h * 0.72f) // Feet
    }
    drawPath(bodyPath, color = color, style = stroke)

    // Arm bent pushing
    val armPath = Path().apply {
        moveTo(w * 0.68f, h * 0.50f)
        lineTo(w * 0.70f, h * 0.65f)
        lineTo(w * 0.62f, h * 0.78f)
    }
    drawPath(armPath, color = color, style = stroke)
}

private fun DrawScope.drawPlankFigure(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Head
    drawCircle(color = color, radius = 3.8.dp.toPx(), center = Offset(w * 0.80f, h * 0.48f))

    // Straight Body
    val bodyPath = Path().apply {
        moveTo(w * 0.74f, h * 0.54f) // Shoulders
        lineTo(w * 0.45f, h * 0.58f) // Hips
        lineTo(w * 0.16f, h * 0.66f) // Toes
    }
    drawPath(bodyPath, color = color, style = stroke)

    // Forearm on floor
    val armPath = Path().apply {
        moveTo(w * 0.70f, h * 0.56f)
        lineTo(w * 0.68f, h * 0.72f)
        lineTo(w * 0.82f, h * 0.72f)
    }
    drawPath(armPath, color = color, style = stroke)
}

private fun DrawScope.drawDumbbellFigure(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)

    // Center Bar
    drawLine(
        color = color,
        start = Offset(w * 0.28f, h * 0.50f),
        end = Offset(w * 0.72f, h * 0.50f),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Left outer plate
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.14f, h * 0.28f),
        size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.44f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
    )

    // Left inner plate
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.24f, h * 0.35f),
        size = androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
    )

    // Right inner plate
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.70f, h * 0.35f),
        size = androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
    )

    // Right outer plate
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.76f, h * 0.28f),
        size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.44f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
    )
}

private fun DrawScope.drawPullUpFigure(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)

    // Pull up bar
    drawLine(
        color = color,
        start = Offset(w * 0.15f, h * 0.20f),
        end = Offset(w * 0.85f, h * 0.20f),
        strokeWidth = 3.5.dp.toPx()
    )

    // Head
    drawCircle(color = color, radius = 3.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.28f))

    // Body hanging
    val body = Path().apply {
        moveTo(w * 0.50f, h * 0.36f)
        lineTo(w * 0.50f, h * 0.62f)
        lineTo(w * 0.46f, h * 0.82f)
    }
    drawPath(body, color = color, style = stroke)

    // Arms up to bar
    val arms = Path().apply {
        moveTo(w * 0.34f, h * 0.20f)
        lineTo(w * 0.42f, h * 0.35f)
        lineTo(w * 0.50f, h * 0.38f)
        lineTo(w * 0.58f, h * 0.35f)
        lineTo(w * 0.66f, h * 0.20f)
    }
    drawPath(arms, color = color, style = stroke)
}

private fun DrawScope.drawGeneralFigure(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.25f))
    val p = Path().apply {
        moveTo(w * 0.5f, h * 0.35f)
        lineTo(w * 0.5f, h * 0.65f)
        lineTo(w * 0.35f, h * 0.85f)
        moveTo(w * 0.5f, h * 0.65f)
        lineTo(w * 0.65f, h * 0.85f)
        moveTo(w * 0.3f, h * 0.45f)
        lineTo(w * 0.7f, h * 0.45f)
    }
    drawPath(p, color = color, style = stroke)
}
