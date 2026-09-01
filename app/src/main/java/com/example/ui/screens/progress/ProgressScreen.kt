package com.example.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BodyMeasurementEntity
import com.example.data.model.PersonalRecordEntity
import com.example.data.model.WorkoutSessionEntity
import com.example.ui.FitViewModel
import com.example.ui.components.ChartPoint
import com.example.ui.components.FitLineChart
import com.example.ui.theme.*

@Composable
fun ProgressScreen(
    viewModel: FitViewModel,
    modifier: Modifier = Modifier
) {
    val measurements by viewModel.bodyMeasurements.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()

    var selectedMetric by remember { mutableStateOf("Weight") }
    var selectedTimeRange by remember { mutableStateOf("1M") }
    var showLogModal by remember { mutableStateOf(false) }

    val metrics = listOf("Weight", "Push-Up", "Squat", "Plank", "Waist", "Chest", "Body Fat %")
    val timeRanges = listOf("7D", "1M", "3M", "6M", "1Y", "ALL")

    // Filter points based on selected metric and time range
    val chartPoints = remember(measurements, selectedMetric, selectedTimeRange) {
        when (selectedMetric) {
            "Weight" -> {
                measurements.filter { it.weightKg != null }.map {
                    ChartPoint(
                        label = it.date.takeLast(5),
                        value = it.weightKg ?: 0f,
                        date = it.date
                    )
                }
            }
            "Waist" -> {
                measurements.filter { it.waistCm != null }.map {
                    ChartPoint(
                        label = it.date.takeLast(5),
                        value = it.waistCm ?: 0f,
                        date = it.date
                    )
                }
            }
            "Chest" -> {
                measurements.filter { it.chestCm != null }.map {
                    ChartPoint(
                        label = it.date.takeLast(5),
                        value = it.chestCm ?: 0f,
                        date = it.date
                    )
                }
            }
            "Body Fat %" -> {
                measurements.filter { it.bodyFatPct != null }.map {
                    ChartPoint(
                        label = it.date.takeLast(5),
                        value = it.bodyFatPct ?: 0f,
                        date = it.date
                    )
                }
            }
            "Push-Up" -> {
                listOf(
                    ChartPoint("W1", 10f),
                    ChartPoint("W2", 12f),
                    ChartPoint("W3", 14f),
                    ChartPoint("W4", 16f),
                    ChartPoint("W5", 18f),
                    ChartPoint("W6", 20f)
                )
            }
            "Squat" -> {
                listOf(
                    ChartPoint("W1", 12f),
                    ChartPoint("W2", 14f),
                    ChartPoint("W3", 16f),
                    ChartPoint("W4", 18f),
                    ChartPoint("W5", 22f)
                )
            }
            "Plank" -> {
                listOf(
                    ChartPoint("W1", 30f),
                    ChartPoint("W2", 40f),
                    ChartPoint("W3", 45f),
                    ChartPoint("W4", 60f),
                    ChartPoint("W5", 75f)
                )
            }
            else -> emptyList()
        }
    }

    val unitStr = when (selectedMetric) {
        "Weight" -> "kg"
        "Waist", "Chest" -> "cm"
        "Body Fat %" -> "%"
        "Plank" -> "sec"
        else -> "reps"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FitDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Screen Title & Quick Log Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Progress & Analytics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTextPrimary
                    )
                    Text(
                        text = "Track your body, strength & volume over time",
                        fontSize = 13.sp,
                        color = FitTextSecondary
                    )
                }

                Button(
                    onClick = { showLogModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = FitCyan),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("log_measurement_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // 2. Metric Selector Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(metrics) { metric ->
                    val isSelected = (metric == selectedMetric)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) FitCyan else FitCardBackground)
                            .border(1.dp, if (isSelected) FitCyan else FitCardBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedMetric = metric }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = metric,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else FitTextSecondary
                        )
                    }
                }
            }
        }

        // 3. Time Range Selector (7D, 1M, 3M, 6M, 1Y, ALL)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                timeRanges.forEach { range ->
                    val isSelected = (range == selectedTimeRange)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF162544) else Color.Transparent)
                            .clickable { selectedTimeRange = range }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = range,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) FitCyan else FitTextSecondary
                        )
                    }
                }
            }
        }

        // 4. Interactive Canvas Line Chart
        item {
            FitLineChart(
                points = chartPoints,
                unit = unitStr,
                lineColor = if (selectedMetric.contains("Push") || selectedMetric.contains("Squat")) FitGreen else FitCyan,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // 5. Personal Records (PR) Section
        item {
            Text(
                text = "🏆 PERSONAL RECORDS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FitTextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PR 1: Max Streak
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Max Streak", fontSize = 11.sp, color = FitTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("21 Days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FitCyan)
                        Text("Active Streak 🔥", fontSize = 10.sp, color = FitGreen)
                    }
                }

                // PR 2: Push-Ups
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Best Push-Up", fontSize = 11.sp, color = FitTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("25 Reps", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FitGreen)
                        Text("Clean form ✓", fontSize = 10.sp, color = FitGreenLight)
                    }
                }

                // PR 3: Max Duration
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Best Plank", fontSize = 11.sp, color = FitTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("90 Secs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FitOrange)
                        Text("Isometric hold", fontSize = 10.sp, color = FitTextSecondary)
                    }
                }
            }
        }

        // 6. Focus Exercises Progress
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐ FOCUS EXERCISES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tap star to toggle",
                    fontSize = 11.sp,
                    color = FitCyan
                )
            }
        }

        val focusExercises = allExercises.filter { it.isFocus }
        if (focusExercises.isNotEmpty()) {
            items(focusExercises, key = { it.id }) { ex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.toggleFocusExercise(ex.id, false) }) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Focus", tint = FitOrangeLight)
                            }
                            Column {
                                Text(text = ex.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FitTextPrimary)
                                Text(text = "${ex.muscleGroup} • ${ex.trackingType}", fontSize = 12.sp, color = FitTextSecondary)
                            }
                        }

                        Text(
                            text = "+15% this month 📈",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitGreen
                        )
                    }
                }
            }
        }

        // 7. Workout Session History Logs
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "📅 RECENT WORKOUT HISTORY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FitTextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        if (allSessions.isNotEmpty()) {
            items(allSessions.take(10), key = { it.id }) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = session.workoutName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FitTextPrimary)
                            Text(text = "${session.date} • ${session.durationSecs / 60} mins", fontSize = 12.sp, color = FitTextSecondary)
                        }

                        val (statusText, statusBg, statusColor) = when (session.status) {
                            "COMPLETED" -> Triple("COMPLETED", Color(0xFF104832), FitGreen)
                            "PARTIAL" -> Triple("PARTIAL", Color(0xFF422F0E), FitOrange)
                            "REST" -> Triple("REST DAY", Color(0xFF1A263D), FitGrey)
                            else -> Triple("MISSED", Color(0xFF4A1515), FitRed)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal to log new Body Measurement
    if (showLogModal) {
        LogMeasurementModal(
            onDismiss = { showLogModal = false },
            onSave = { w, waist, chest, bf, note ->
                viewModel.logMeasurement(w, waist, chest, bf, note)
                showLogModal = false
            }
        )
    }
}

@Composable
fun LogMeasurementModal(
    onDismiss: () -> Unit,
    onSave: (Float?, Float?, Float?, Float?, String) -> Unit
) {
    var weightStr by remember { mutableStateOf("75.0") }
    var waistStr by remember { mutableStateOf("82.0") }
    var chestStr by remember { mutableStateOf("101.0") }
    var bodyFatStr by remember { mutableStateOf("17.5") }
    var noteStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = {
            Text("Log Body Measurement", color = FitTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FitCyan,
                        unfocusedBorderColor = FitCardBorder,
                        focusedTextColor = FitTextPrimary,
                        unfocusedTextColor = FitTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = waistStr,
                        onValueChange = { waistStr = it },
                        label = { Text("Waist (cm)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitCyan,
                            unfocusedBorderColor = FitCardBorder,
                            focusedTextColor = FitTextPrimary,
                            unfocusedTextColor = FitTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = chestStr,
                        onValueChange = { chestStr = it },
                        label = { Text("Chest (cm)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitCyan,
                            unfocusedBorderColor = FitCardBorder,
                            focusedTextColor = FitTextPrimary,
                            unfocusedTextColor = FitTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bodyFatStr,
                    onValueChange = { bodyFatStr = it },
                    label = { Text("Body Fat % (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FitCyan,
                        unfocusedBorderColor = FitCardBorder,
                        focusedTextColor = FitTextPrimary,
                        unfocusedTextColor = FitTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        weightStr.toFloatOrNull(),
                        waistStr.toFloatOrNull(),
                        chestStr.toFloatOrNull(),
                        bodyFatStr.toFloatOrNull(),
                        noteStr
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FitCyan)
            ) {
                Text("Save Entry", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FitTextSecondary)
            }
        }
    )
}
