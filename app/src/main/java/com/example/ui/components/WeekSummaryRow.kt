package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DayStatus
import com.example.data.repository.DaySummary
import com.example.ui.theme.*

@Composable
fun WeekSummaryView(
    weekDays: List<DaySummary>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { day ->
                DaySummaryChip(day = day)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Legend matching screenshot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✓",
                    color = FitGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Done",
                    color = FitTextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✕",
                    color = FitRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Missed",
                    color = FitTextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⏳",
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pending",
                    color = FitTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DaySummaryChip(day: DaySummary) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.dayLetter,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (day.isToday) FitCyan else FitTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        val (bgColor, iconText, iconColor) = when (day.status) {
            DayStatus.DONE -> Triple(Color(0xFF10B981), "✓", Color.Black)
            DayStatus.MISSED -> Triple(Color(0xFFEF4444), "✕", Color.White)
            DayStatus.PENDING -> Triple(Color(0xFF243048), "⏳", Color(0xFFFBBF24))
            DayStatus.REST -> Triple(Color(0xFF151F33), "──", Color(0xFF64748B))
        }

        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .then(
                    if (day.isToday) Modifier.border(1.5.dp, FitCyan, RoundedCornerShape(6.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                color = iconColor,
                fontSize = if (iconText == "⏳") 12.sp else 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
