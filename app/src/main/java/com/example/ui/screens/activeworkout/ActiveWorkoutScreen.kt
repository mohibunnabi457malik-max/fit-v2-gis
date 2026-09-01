package com.example.ui.screens.activeworkout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FitViewModel
import com.example.ui.components.FitExerciseCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ActiveWorkoutScreen(
    sessionId: Long,
    viewModel: FitViewModel,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeData by viewModel.homeData.collectAsState()

    var elapsedSeconds by remember { mutableStateOf(120) }
    var isTimerRunning by remember { mutableStateOf(true) }

    var restSecondsRemaining by remember { mutableStateOf(0) }
    var isRestTimerRunning by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    // Workout Elapsed Timer
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Rest Timer
    LaunchedEffect(isRestTimerRunning, restSecondsRemaining) {
        while (isRestTimerRunning && restSecondsRemaining > 0) {
            delay(1000)
            restSecondsRemaining--
            if (restSecondsRemaining <= 0) {
                isRestTimerRunning = false
            }
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val data = homeData

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FitDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Top Header with Live Timer and Finish Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE WORKOUT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitCyan,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = data?.todayWorkoutName ?: "Full Body A",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTextPrimary
                            )
                        }

                        // Elapsed Time display
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF13233C))
                                .border(1.dp, FitCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⏱ $timeFormatted",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress info & Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${data?.completedExercisesCount ?: 0} / ${data?.totalExercisesCount ?: 4} exercises completed",
                            fontSize = 12.sp,
                            color = FitTextSecondary
                        )

                        Text(
                            text = "${data?.completedSetsCount ?: 0} / ${data?.totalSetsCount ?: 8} sets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val frac = if ((data?.totalSetsCount ?: 8) > 0) {
                        (data?.completedSetsCount ?: 0).toFloat() / (data?.totalSetsCount ?: 8)
                    } else 0f
                    val animatedProg by animateFloatAsState(targetValue = frac.coerceIn(0f, 1f), label = "prog")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E2D48))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProg)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(FitCyan, FitGreen)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Pause/Resume & Finish
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isTimerRunning = !isTimerRunning },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Timer toggle",
                                tint = FitTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTimerRunning) "Pause" else "Resume",
                                color = FitTextPrimary,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { showFinishDialog = true },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("finish_workout_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FitGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Finish",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Finish Workout",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Rest Timer Card (if active)
        if (restSecondsRemaining > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10263E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCyan)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("REST TIMER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitCyan)
                            Text(
                                text = "${restSecondsRemaining}s",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { restSecondsRemaining += 15 },
                                colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+15s", color = FitCyan, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    restSecondsRemaining = 0
                                    isRestTimerRunning = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Skip", color = FitTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Exercises Section
        if (data != null && data.exercises.isNotEmpty()) {
            items(data.exercises, key = { it.exerciseName }) { exercise ->
                FitExerciseCard(
                    exercise = exercise,
                    onUpdateSet = { updatedSet ->
                        viewModel.updateLoggedSet(updatedSet)
                        // Trigger 60s rest timer on completing a set
                        if (updatedSet.isCompleted && restSecondsRemaining == 0) {
                            restSecondsRemaining = 60
                            isRestTimerRunning = true
                        }
                    }
                )
            }
        }
    }

    // Finish Workout Modal
    if (showFinishDialog) {
        val allDone = data?.completedExercisesCount == data?.totalExercisesCount
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = FitCardBackground,
            title = {
                Text(
                    text = if (allDone) "WORKOUT COMPLETE 🎉" else "WORKOUT PARTIAL ⚠️",
                    color = FitTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (allDone)
                            "Great job! You achieved your planned targets across all ${data?.totalExercisesCount} exercises."
                        else
                            "${data?.completedExercisesCount} of ${data?.totalExercisesCount} exercises logged. Would you like to finish and save anyway?",
                        color = FitTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Elapsed time: $timeFormatted",
                        color = FitTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Estimated burn: +${data?.estimatedCalories} kcal",
                        color = FitGreen,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Historical plan vs actual saved locally.",
                        color = FitCyan,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeSession(sessionId, elapsedSeconds)
                        showFinishDialog = false
                        onFinishWorkout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FitGreen)
                ) {
                    Text("Save & Finish", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Continue Workout", color = FitTextSecondary)
                }
            }
        )
    }
}
