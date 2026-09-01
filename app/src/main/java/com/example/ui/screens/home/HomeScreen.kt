package com.example.ui.screens.home

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FitnessCenter
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
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: FitViewModel,
    onStartWorkout: (Long) -> Unit,
    onNavigateToPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeData by viewModel.homeData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    val data = homeData

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FitDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Top Greeting Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Good Morning, ${data?.userName ?: "Mohib"}! 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Let's crush your goals today.",
                    fontSize = 14.sp,
                    color = FitTextSecondary
                )
            }
        }

        // 2. Row: Streak & Progress Card + Week Summary Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Card: STREAK & PROGRESS
                Card(
                    modifier = Modifier
                        .weight(0.42f)
                        .height(188.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STREAK & PROGRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTextSecondary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        StreakRing(
                            streakDays = data?.streakDays ?: 21,
                            modifier = Modifier.size(92.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔥", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${data?.streakDays ?: 21}-Day Streak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FitTextPrimary
                            )
                        }
                    }
                }

                // Right Card: WEEK SUMMARY
                Card(
                    modifier = Modifier
                        .weight(0.58f)
                        .height(188.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "WEEK SUMMARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTextSecondary
                        )

                        if (data != null && data.weekDays.isNotEmpty()) {
                            WeekSummaryView(weekDays = data.weekDays)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Loading week...", color = FitTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. TODAY'S WORKOUT PLAN Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        data?.sessionId?.let { onStartWorkout(it) }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Muscle icon inside circular badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, FitCyan, CircleShape)
                            .background(Color(0xFF0C243B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = "Workout",
                            tint = FitCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S WORKOUT PLAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = data?.todayWorkoutName ?: "Full Body A",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱ ${data?.estimatedDurationMin ?: 20} min",
                                fontSize = 12.sp,
                                color = FitTextSecondary
                            )
                        }
                    }

                    // Progress info & Bar on right
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${data?.completedExercisesCount ?: 3} / ${data?.totalExercisesCount ?: 5} exercises",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val progressFraction = if ((data?.totalExercisesCount ?: 5) > 0) {
                            (data?.completedExercisesCount ?: 3).toFloat() / (data?.totalExercisesCount ?: 5)
                        } else 0.6f

                        val animatedProg by animateFloatAsState(targetValue = progressFraction.coerceIn(0f, 1f), label = "prog")

                        Box(
                            modifier = Modifier
                                .width(120.dp)
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
                    }
                }
            }
        }

        // 4. Section Header: WORKOUT PLAN
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKOUT PLAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTextPrimary,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Tap set to log",
                    fontSize = 11.sp,
                    color = FitCyan
                )
            }
        }

        // 5. Exercise Cards List
        if (data != null && data.exercises.isNotEmpty()) {
            items(data.exercises, key = { it.exerciseName }) { exercise ->
                FitExerciseCard(
                    exercise = exercise,
                    onUpdateSet = { updatedSet ->
                        viewModel.updateLoggedSet(updatedSet)
                    }
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No workout plan scheduled for today.",
                            color = FitTextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToPlans,
                            colors = ButtonDefaults.buttonColors(containerColor = FitCyan)
                        ) {
                            Text("Choose or Create Plan", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Bottom Card: WORKOUT PROGRESS
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("workout_progress_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trophy icon in green
                    Text(
                        text = "🏆",
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WORKOUT PROGRESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${data?.completedExercisesCount ?: 3} / ${data?.totalExercisesCount ?: 5} exercises  •  ${data?.completedSetsCount ?: 12} / ${data?.totalSetsCount ?: 16} sets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = FitTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val setProg = if ((data?.totalSetsCount ?: 16) > 0) {
                            (data?.completedSetsCount ?: 12).toFloat() / (data?.totalSetsCount ?: 16)
                        } else 0.75f

                        val animatedSetProg by animateFloatAsState(targetValue = setProg.coerceIn(0f, 1f), label = "setProg")

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1E2D48))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedSetProg)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(FitCyan, FitGreen)
                                        )
                                    )
                            )
                        }
                    }

                    // Calories badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${data?.estimatedCalories ?: 12} kcal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTextPrimary
                            )
                        }
                        Text(
                            text = "(est.)",
                            fontSize = 10.sp,
                            color = FitTextSecondary
                        )
                    }
                }
            }
        }
    }
}
