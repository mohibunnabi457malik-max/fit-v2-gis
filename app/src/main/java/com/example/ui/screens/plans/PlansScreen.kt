package com.example.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.ExerciseEntity
import com.example.data.model.WorkoutPlanEntity
import com.example.data.repository.DaySetupConfig
import com.example.data.repository.ExerciseSetupConfig
import com.example.ui.FitViewModel
import com.example.ui.theme.*

@Composable
fun PlansScreen(
    viewModel: FitViewModel,
    onNavigateToAiCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlan by viewModel.activePlan.collectAsState()
    val allPlans by viewModel.allPlans.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()

    var showPresetDialog by remember { mutableStateOf(false) }
    var showManualBuilderDialog by remember { mutableStateOf(false) }
    var showCustomExerciseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FitDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Title
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Workout Plans & Routines",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTextPrimary
                )
                Text(
                    text = "Multi-week schedule generator & progressive overload",
                    fontSize = 13.sp,
                    color = FitTextSecondary
                )
            }
        }

        // 2. Three Major Creation Action Cards (Visual Hero Choices)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Choose Preset
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showPresetDialog = true }
                        .testTag("choose_preset_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("CHOOSE\nPRESET", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FitCyan, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("15+ verified", fontSize = 10.sp, color = FitTextSecondary)
                    }
                }

                // 2. Build Manually
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showManualBuilderDialog = true }
                        .testTag("build_manually_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛠️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("BUILD\nMANUALLY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FitGreen, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1-54 Weeks", fontSize = 10.sp, color = FitTextSecondary)
                    }
                }

                // 3. Ask AI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToAiCoach() }
                        .testTag("ask_ai_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitOrange)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("ASK\nAI COACH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FitOrange, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("External Flow", fontSize = 10.sp, color = FitTextSecondary)
                    }
                }
            }
        }

        // 3. Active Plan Details Card
        item {
            Text(
                text = "CURRENT ACTIVE PROGRAM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FitTextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val current = activePlan
            if (current != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FitCyan)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = current.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FitTextPrimary
                                )
                                Text(
                                    text = "Goal: ${current.goal}",
                                    fontSize = 12.sp,
                                    color = FitCyan
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF104832))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("ACTIVE ✓", color = FitGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Duration: ${current.durationWeeks} Weeks", fontSize = 12.sp, color = FitTextSecondary)
                            Text("• Days: ${current.daysPerWeek} / week", fontSize = 12.sp, color = FitTextSecondary)
                            Text("• Overload: ${current.progressiveOverloadRate.toInt()}% ${current.progressiveOverloadType}", fontSize = 12.sp, color = FitGreen)
                        }
                    }
                }
            }
        }

        // 4. Custom Exercise Creator button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXERCISE LIBRARY (${allExercises.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTextSecondary,
                    letterSpacing = 0.5.sp
                )

                TextButton(onClick = { showCustomExerciseDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = FitCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Custom Exercise", color = FitCyan, fontSize = 12.sp)
                }
            }
        }

        // 5. Exercise Library List
        items(allExercises.take(12), key = { it.id }) { ex ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = ex.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FitTextPrimary)
                        Text(text = "${ex.muscleGroup} • ${ex.equipment} • ${ex.trackingType}", fontSize = 11.sp, color = FitTextSecondary)
                    }

                    IconButton(onClick = { viewModel.toggleFocusExercise(ex.id, !ex.isFocus) }) {
                        Icon(
                            imageVector = if (ex.isFocus) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Star",
                            tint = if (ex.isFocus) FitOrangeLight else FitGrey
                        )
                    }
                }
            }
        }
    }

    // Modal: Preset Selection
    if (showPresetDialog) {
        PresetSelectionModal(
            onDismiss = { showPresetDialog = false },
            onSelectPreset = { presetName, goal, days, weeks, daysSetup ->
                viewModel.createPlan(
                    name = presetName,
                    goal = goal,
                    durationWeeks = weeks,
                    daysPerWeek = days,
                    overloadType = "MONTHLY",
                    overloadRate = 5.0f,
                    overloadTarget = "REPS_WEIGHT",
                    daysSetup = daysSetup
                )
                showPresetDialog = false
            },
            exercises = allExercises
        )
    }

    // Modal: Manual Builder
    if (showManualBuilderDialog) {
        ManualPlanBuilderModal(
            onDismiss = { showManualBuilderDialog = false },
            onSave = { name, goal, weeks, daysCount, overloadType, overloadRate, daysSetup ->
                viewModel.createPlan(
                    name = name,
                    goal = goal,
                    durationWeeks = weeks,
                    daysPerWeek = daysCount,
                    overloadType = overloadType,
                    overloadRate = overloadRate,
                    overloadTarget = "REPS_WEIGHT",
                    daysSetup = daysSetup
                )
                showManualBuilderDialog = false
            },
            exercises = allExercises
        )
    }

    // Modal: Custom Exercise
    if (showCustomExerciseDialog) {
        CustomExerciseDialog(
            onDismiss = { showCustomExerciseDialog = false },
            onSave = { name, muscle, equip, track, inst ->
                viewModel.addCustomExercise(name, muscle, equip, track, inst)
                showCustomExerciseDialog = false
            }
        )
    }
}

@Composable
fun PresetSelectionModal(
    onDismiss: () -> Unit,
    onSelectPreset: (String, String, Int, Int, List<DaySetupConfig>) -> Unit,
    exercises: List<ExerciseEntity>
) {
    val presets = listOf(
        "Beginner Home No Equipment" to ("General Fitness" to 3),
        "3-Day Full Body Foundation" to ("Strength & Muscle" to 3),
        "4-Day Upper / Lower Split" to ("Hypertrophy" to 4),
        "15-Minute Daily Express" to ("Fat Loss & Health" to 5),
        "Home Dumbbell Builder" to ("Muscle Building" to 4),
        "Core & Calisthenics Power" to ("Endurance & Strength" to 3)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = {
            Text("Choose a Verified Preset", color = FitTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                items(presets) { (title, meta) ->
                    val (goal, days) = meta
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // build standard daysSetup
                                val squatEx = exercises.firstOrNull { it.name.contains("Squat", true) } ?: exercises.first()
                                val pushEx = exercises.firstOrNull { it.name.contains("Push", true) } ?: exercises.first()
                                val plankEx = exercises.firstOrNull { it.name.contains("Plank", true) } ?: exercises.first()

                                val exConfigs = listOf(
                                    ExerciseSetupConfig(squatEx.id, squatEx.name, squatEx.muscleGroup, squatEx.trackingType, 2, 10, 0f, 0, 0f, 60),
                                    ExerciseSetupConfig(pushEx.id, pushEx.name, pushEx.muscleGroup, pushEx.trackingType, 2, 10, 0f, 0, 0f, 60),
                                    ExerciseSetupConfig(plankEx.id, plankEx.name, plankEx.muscleGroup, plankEx.trackingType, 2, 0, 0f, 30, 0f, 45)
                                )

                                val daysSetup = listOf(
                                    DaySetupConfig(1, "Workout A", false, 20, exConfigs),
                                    DaySetupConfig(2, "Rest Day", true, 0, emptyList()),
                                    DaySetupConfig(3, "Workout B", false, 20, exConfigs),
                                    DaySetupConfig(4, "Rest Day", true, 0, emptyList()),
                                    DaySetupConfig(5, "Workout A", false, 20, exConfigs),
                                    DaySetupConfig(6, "Rest Day", true, 0, emptyList()),
                                    DaySetupConfig(7, "Rest Day", true, 0, emptyList())
                                )

                                onSelectPreset(title, goal, days, 12, daysSetup)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = FitCardSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(title, color = FitTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Goal: $goal • $days days/wk • 12 Weeks with 5% Overload", color = FitTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FitTextSecondary) }
        }
    )
}

@Composable
fun ManualPlanBuilderModal(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int, String, Float, List<DaySetupConfig>) -> Unit,
    exercises: List<ExerciseEntity>
) {
    var planName by remember { mutableStateOf("My Custom 12-Week Routine") }
    var planGoal by remember { mutableStateOf("Strength & Muscle") }
    var weeksCount by remember { mutableStateOf("12") }
    var overloadRate by remember { mutableStateOf("5") }
    var overloadType by remember { mutableStateOf("MONTHLY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = {
            Text("Build Multi-Week Program", color = FitTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Program Name") },
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
                        value = weeksCount,
                        onValueChange = { weeksCount = it },
                        label = { Text("Weeks (1-54)") },
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
                        value = overloadRate,
                        onValueChange = { overloadRate = it },
                        label = { Text("Overload % (e.g. 5)") },
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
                Text("Progression Frequency: Monthly (5% every 4 weeks)", color = FitCyan, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val squatEx = exercises.firstOrNull { it.name.contains("Squat", true) } ?: exercises.first()
                    val pushEx = exercises.firstOrNull { it.name.contains("Push", true) } ?: exercises.first()
                    val plankEx = exercises.firstOrNull { it.name.contains("Plank", true) } ?: exercises.first()

                    val exConfigs = listOf(
                        ExerciseSetupConfig(squatEx.id, squatEx.name, squatEx.muscleGroup, squatEx.trackingType, 2, 10, 0f, 0, 0f, 60),
                        ExerciseSetupConfig(pushEx.id, pushEx.name, pushEx.muscleGroup, pushEx.trackingType, 2, 10, 0f, 0, 0f, 60),
                        ExerciseSetupConfig(plankEx.id, plankEx.name, plankEx.muscleGroup, plankEx.trackingType, 2, 0, 0f, 30, 0f, 45)
                    )

                    val daysSetup = listOf(
                        DaySetupConfig(1, "Full Body A", false, 20, exConfigs),
                        DaySetupConfig(2, "Rest Day", true, 0, emptyList()),
                        DaySetupConfig(3, "Full Body B", false, 20, exConfigs),
                        DaySetupConfig(4, "Rest Day", true, 0, emptyList()),
                        DaySetupConfig(5, "Full Body A", false, 20, exConfigs),
                        DaySetupConfig(6, "Rest Day", true, 0, emptyList()),
                        DaySetupConfig(7, "Rest Day", true, 0, emptyList())
                    )

                    onSave(
                        planName,
                        planGoal,
                        weeksCount.toIntOrNull() ?: 12,
                        3,
                        overloadType,
                        overloadRate.toFloatOrNull() ?: 5f,
                        daysSetup
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FitGreen)
            ) {
                Text("Generate Program", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FitTextSecondary) }
        }
    )
}

@Composable
fun CustomExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf("Chest") }
    var equip by remember { mutableStateOf("Dumbbells") }
    var track by remember { mutableStateOf("REPS") }
    var instructions by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = { Text("Add Custom Exercise", color = FitTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name (e.g. Incline Bench Press)") },
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
                        value = muscle,
                        onValueChange = { muscle = it },
                        label = { Text("Muscle Group") },
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
                        value = track,
                        onValueChange = { track = it },
                        label = { Text("Type (REPS/WEIGHT)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitCyan,
                            unfocusedBorderColor = FitCardBorder,
                            focusedTextColor = FitTextPrimary,
                            unfocusedTextColor = FitTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, muscle, equip, track, instructions) },
                colors = ButtonDefaults.buttonColors(containerColor = FitCyan)
            ) {
                Text("Save Exercise", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FitTextSecondary) }
        }
    )
}
