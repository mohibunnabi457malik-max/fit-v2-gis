package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FitCyan
import com.example.ui.theme.FitGreen
import com.example.ui.theme.FitTextPrimary
import com.example.ui.theme.FitTextSecondary

@Composable
fun StreakRing(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(streakDays) {
        animatedProgress.animateTo(
            targetValue = 0.85f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = Color(0xFF152238),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Gradient Progress Arc
            val gradient = Brush.sweepGradient(
                0.0f to FitCyan,
                0.5f to FitGreen,
                1.0f to FitCyan
            )

            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(FitCyan, FitGreen),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔥",
                fontSize = 18.sp
            )
            Text(
                text = "$streakDays",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = FitTextPrimary,
                lineHeight = 24.sp
            )
            Text(
                text = "Day",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FitTextSecondary
            )
        }
    }
}
