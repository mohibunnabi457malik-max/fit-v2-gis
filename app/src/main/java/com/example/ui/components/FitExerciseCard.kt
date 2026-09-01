package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoggedSetEntity
import com.example.data.repository.ExerciseCardData
import com.example.data.repository.ExerciseStatus
import com.example.ui.theme.*

@Composable
fun FitExerciseCard(
    exercise: ExerciseCardData,
    onUpdateSet: (LoggedSetEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingSet by remember { mutableStateOf<LoggedSetEntity?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FitCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Order Number
            Text(
                text = "${exercise.orderNumber}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (exercise.status == ExerciseStatus.DONE) FitGreen else if (exercise.status == ExerciseStatus.PARTIAL) FitOrange else FitCyan,
                modifier = Modifier.width(28.dp)
            )

            // 2. Exercise Silhouette Figure
            ExerciseFigureIcon(
                exerciseName = exercise.exerciseName,
                modifier = Modifier
                    .size(46.dp)
                    .padding(horizontal = 2.dp),
                tint = if (exercise.status == ExerciseStatus.DONE) FitGreen else if (exercise.status == ExerciseStatus.PARTIAL) FitOrange else FitCyan
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Middle Section: Title + Planned vs Logged Sets Table
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title Row + Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.exerciseName.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTextPrimary
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Status icon
                    when (exercise.status) {
                        ExerciseStatus.DONE -> {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(FitGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        ExerciseStatus.PARTIAL -> {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(FitOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriorityHigh,
                                    contentDescription = "Partial",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        ExerciseStatus.MISSED -> {
                            Text(text = "✕", color = FitRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        ExerciseStatus.NOT_STARTED -> {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, FitGrey, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table Headers
                Row(
                    modifier = Modifier.fillMaxWidth(0.95f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(42.dp))
                    Text(text = "Planned", fontSize = 10.sp, color = FitTextSecondary)
                    Text(text = "Logged", fontSize = 10.sp, color = FitTextSecondary)
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Set Rows
                exercise.sets.forEach { set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Set ${set.setNumber}",
                            fontSize = 11.sp,
                            color = FitTextSecondary,
                            modifier = Modifier.width(42.dp)
                        )

                        // Planned Value
                        val plannedStr = when (set.trackingType) {
                            "TIME_SECS" -> "${set.plannedDurationSecs}s"
                            "DISTANCE_KM" -> "${set.plannedDistanceKm}km"
                            else -> if (set.plannedWeightKg > 0) "${set.plannedWeightKg.toInt()}k×${set.plannedReps}" else "${set.plannedReps}"
                        }
                        Text(
                            text = plannedStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = FitTextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(44.dp)
                        )

                        // Logged Value Clickable Pill
                        val actualStr = when (set.trackingType) {
                            "TIME_SECS" -> set.actualDurationSecs?.let { "${it}s" } ?: "──"
                            "DISTANCE_KM" -> set.actualDistanceKm?.let { "${it}km" } ?: "──"
                            else -> set.actualReps?.toString() ?: "──"
                        }

                        val pillBg = when {
                            set.isCompleted -> Color(0xFF104832) // deep emerald background
                            set.actualReps != null && set.actualReps > 0 -> Color(0xFF422F0E) // deep amber background
                            else -> Color(0xFF19243B) // dark navy unlogged
                        }
                        val pillTextColor = when {
                            set.isCompleted -> FitGreen
                            set.actualReps != null && set.actualReps > 0 -> FitOrangeLight
                            else -> FitGrey
                        }
                        val pillBorder = when {
                            set.isCompleted -> FitGreen
                            set.actualReps != null && set.actualReps > 0 -> FitOrange
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .width(46.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(pillBg)
                                .border(1.dp, pillBorder, RoundedCornerShape(6.dp))
                                .clickable { editingSet = set },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = actualStr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = pillTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 4. Right Side: Delta summary and status text
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(102.dp)
            ) {
                val deltaColor = when (exercise.status) {
                    ExerciseStatus.DONE -> FitGreen
                    ExerciseStatus.PARTIAL -> FitOrange
                    ExerciseStatus.MISSED -> FitRed
                    ExerciseStatus.NOT_STARTED -> FitTextPrimary
                }

                Text(
                    text = exercise.deltaSummary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = deltaColor,
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = exercise.deltaSubtitle,
                    fontSize = 10.sp,
                    color = if (exercise.status == ExerciseStatus.DONE) FitGreenLight else if (exercise.status == ExerciseStatus.PARTIAL) FitOrangeLight else FitTextSecondary,
                    textAlign = TextAlign.End,
                    lineHeight = 12.sp
                )
            }
        }
    }

    // Quick Data Entry Dialog with Auto-Save
    editingSet?.let { set ->
        QuickDataEntryDialog(
            set = set,
            onDismiss = { editingSet = null },
            onSave = { updated ->
                onUpdateSet(updated)
                editingSet = null
            }
        )
    }
}

@Composable
fun QuickDataEntryDialog(
    set: LoggedSetEntity,
    onDismiss: () -> Unit,
    onSave: (LoggedSetEntity) -> Unit
) {
    var repsText by remember { mutableStateOf(set.actualReps?.toString() ?: "${set.plannedReps}") }
    var weightText by remember { mutableStateOf(set.actualWeightKg?.toString() ?: "${set.plannedWeightKg}") }
    var durationText by remember { mutableStateOf(set.actualDurationSecs?.toString() ?: "${set.plannedDurationSecs}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = {
            Text(
                text = "${set.exerciseName} — Set ${set.setNumber}",
                color = FitTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Data Entry (Auto-Saves)",
                    fontSize = 12.sp,
                    color = FitTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                when (set.trackingType) {
                    "TIME_SECS" -> {
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Duration (Seconds)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FitCyan,
                                unfocusedBorderColor = FitCardBorder,
                                focusedTextColor = FitTextPrimary,
                                unfocusedTextColor = FitTextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "DISTANCE_KM" -> {
                        OutlinedTextField(
                            value = repsText,
                            onValueChange = { repsText = it },
                            label = { Text("Distance (km)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FitCyan,
                                unfocusedBorderColor = FitCardBorder,
                                focusedTextColor = FitTextPrimary,
                                unfocusedTextColor = FitTextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "WEIGHT_REPS" -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Weight (kg)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FitCyan,
                                    unfocusedBorderColor = FitCardBorder,
                                    focusedTextColor = FitTextPrimary,
                                    unfocusedTextColor = FitTextPrimary
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = repsText,
                                onValueChange = { repsText = it },
                                label = { Text("Reps") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FitCyan,
                                    unfocusedBorderColor = FitCardBorder,
                                    focusedTextColor = FitTextPrimary,
                                    unfocusedTextColor = FitTextPrimary
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    else -> {
                        // Reps
                        OutlinedTextField(
                            value = repsText,
                            onValueChange = { repsText = it },
                            label = { Text("Actual Reps (Target: ${set.plannedReps})") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FitCyan,
                                unfocusedBorderColor = FitCardBorder,
                                focusedTextColor = FitTextPrimary,
                                unfocusedTextColor = FitTextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Increment / Decrement Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            val cur = repsText.toIntOrNull() ?: set.plannedReps
                            if (cur > 0) repsText = (cur - 1).toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant)
                    ) {
                        Text("-1", color = FitTextPrimary)
                    }
                    Button(
                        onClick = {
                            repsText = "${set.plannedReps}"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant)
                    ) {
                        Text("Target", color = FitCyan)
                    }
                    Button(
                        onClick = {
                            val cur = repsText.toIntOrNull() ?: set.plannedReps
                            repsText = (cur + 1).toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant)
                    ) {
                        Text("+1", color = FitTextPrimary)
                    }
                    Button(
                        onClick = {
                            val cur = repsText.toIntOrNull() ?: set.plannedReps
                            repsText = (cur + 2).toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant)
                    ) {
                        Text("+2", color = FitGreen)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = repsText.toIntOrNull()
                    val w = weightText.toFloatOrNull()
                    val d = durationText.toIntOrNull()
                    val updated = set.copy(
                        actualReps = r,
                        actualWeightKg = w,
                        actualDurationSecs = d,
                        isCompleted = true
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FitGreen)
            ) {
                Text("Save Log", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = FitTextSecondary)
            }
        }
    )
}
