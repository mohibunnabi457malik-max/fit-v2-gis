package com.example.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.data.model.WorkoutReminderEntity
import com.example.ui.FitViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: FitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val reminders by viewModel.workoutReminders.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showBackupModal by remember { mutableStateOf(false) }
    var showRestoreModal by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }

    val profile = userProfile ?: UserProfileEntity()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FitDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Header
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Profile & Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTextPrimary
                )
                Text(
                    text = "Workout alarms, profile data & local backup",
                    fontSize = 13.sp,
                    color = FitTextSecondary
                )
            }
        }

        // 2. User Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF132A44))
                            .border(2.dp, FitCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTextPrimary
                        )
                        Text(
                            text = "${profile.age} yrs • ${profile.weightKg} kg • ${profile.heightCm} cm",
                            fontSize = 12.sp,
                            color = FitTextSecondary
                        )
                        Text(
                            text = "Goal: ${profile.goals}",
                            fontSize = 12.sp,
                            color = FitCyan
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = FitCyan)
                    }
                }
            }
        }

        // 3. Offline Workout Alarm Reminders (Requested by user)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Alarm, contentDescription = "Alarm", tint = FitOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WORKOUT ALARM REMINDERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                TextButton(onClick = { showAddReminderDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = FitCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Alarm", color = FitCyan, fontSize = 12.sp)
                }
            }
        }

        // Reminders List
        if (reminders.isNotEmpty()) {
            items(reminders, key = { it.id }) { reminder ->
                val dayName = when (reminder.dayOfWeek) {
                    1 -> "Monday"
                    2 -> "Tuesday"
                    3 -> "Wednesday"
                    4 -> "Thursday"
                    5 -> "Friday"
                    6 -> "Saturday"
                    7 -> "Sunday"
                    else -> "Everyday"
                }
                val timeStr = String.format("%02d:%02d", reminder.hour, reminder.minute)

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = timeStr,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reminder.isEnabled) FitTextPrimary else FitGrey
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (reminder.isEnabled) FitCyan else FitGrey
                                )
                            }
                            Text(
                                text = if (reminder.minutesBefore == 0) "At workout time" else "${reminder.minutesBefore}m before",
                                fontSize = 11.sp,
                                color = FitTextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = reminder.isEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.saveReminder(reminder.copy(isEnabled = isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = FitCyan,
                                    uncheckedThumbColor = FitGrey,
                                    uncheckedTrackColor = FitCardSurfaceVariant
                                )
                            )
                            IconButton(onClick = { viewModel.deleteReminder(reminder) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = FitGrey, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FitCardBackground)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No workout alarms set. Tap '+ Add Alarm' to schedule reminders.", color = FitTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        // 4. Offline Privacy Notice
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.CloudOff, contentDescription = "Offline", tint = FitGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("100% Offline & Private", color = FitGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Your workouts, logs, and body measurements are stored strictly on this device in local SQLite.", color = FitTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // 5. Data Backup & Restore
        item {
            Text(
                text = "DATA MANAGEMENT & BACKUP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FitTextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.exportBackup { json ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Fit Tracker Backup", json)
                            clipboard.setPrimaryClip(clip)
                            statusText = "Full JSON backup copied to clipboard!"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Backup", tint = FitCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export JSON", color = FitCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showRestoreModal = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FitCardSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = "Restore", tint = FitGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore JSON", color = FitGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (statusText != null) {
                Text(
                    text = statusText ?: "",
                    color = FitGreen,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }

    // Modal: Edit Profile
    if (showEditProfileDialog) {
        EditProfileModal(
            current = profile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.updateUserProfile(updated)
                showEditProfileDialog = false
            }
        )
    }

    // Modal: Add Reminder
    if (showAddReminderDialog) {
        AddReminderModal(
            onDismiss = { showAddReminderDialog = false },
            onSave = { reminder ->
                viewModel.saveReminder(reminder)
                showAddReminderDialog = false
            }
        )
    }

    // Modal: Restore Backup
    if (showRestoreModal) {
        RestoreBackupModal(
            onDismiss = { showRestoreModal = false },
            onRestore = { json, replaceAll ->
                viewModel.restoreBackup(json, replaceAll) { success ->
                    statusText = if (success) "Backup restored successfully!" else "Failed to parse backup JSON."
                    showRestoreModal = false
                }
            }
        )
    }
}

@Composable
fun EditProfileModal(
    current: UserProfileEntity,
    onDismiss: () -> Unit,
    onSave: (UserProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(current.name) }
    var age by remember { mutableStateOf(current.age.toString()) }
    var weight by remember { mutableStateOf(current.weightKg.toString()) }
    var height by remember { mutableStateOf(current.heightCm.toString()) }
    var goals by remember { mutableStateOf(current.goals) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = { Text("Edit Fitness Profile", color = FitTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
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
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
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
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
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
                    value = goals,
                    onValueChange = { goals = it },
                    label = { Text("Goals") },
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
                        current.copy(
                            name = name,
                            age = age.toIntOrNull() ?: current.age,
                            weightKg = weight.toFloatOrNull() ?: current.weightKg,
                            heightCm = height.toFloatOrNull() ?: current.heightCm,
                            goals = goals
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FitCyan)
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FitTextSecondary) }
        }
    )
}

@Composable
fun AddReminderModal(
    onDismiss: () -> Unit,
    onSave: (WorkoutReminderEntity) -> Unit
) {
    var selectedDay by remember { mutableStateOf(1) } // Mon
    var hour by remember { mutableStateOf("07") }
    var minute by remember { mutableStateOf("30") }
    var minutesBefore by remember { mutableStateOf("15") }

    val days = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = { Text("Schedule Workout Alarm", color = FitTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select Day of Week:", fontSize = 12.sp, color = FitTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEach { (label, dayVal) ->
                        val isSel = (selectedDay == dayVal)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) FitCyan else FitCardSurfaceVariant)
                                .clickable { selectedDay = dayVal }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else FitTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { hour = it },
                        label = { Text("Hour (0-23)") },
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
                        value = minute,
                        onValueChange = { minute = it },
                        label = { Text("Minute (0-59)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitCyan,
                            unfocusedBorderColor = FitCardBorder,
                            focusedTextColor = FitTextPrimary,
                            unfocusedTextColor = FitTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = minutesBefore,
                    onValueChange = { minutesBefore = it },
                    label = { Text("Reminder (minutes before)") },
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
                    val h = hour.toIntOrNull()?.coerceIn(0, 23) ?: 7
                    val m = minute.toIntOrNull()?.coerceIn(0, 59) ?: 30
                    val mb = minutesBefore.toIntOrNull() ?: 15
                    onSave(
                        WorkoutReminderEntity(
                            dayOfWeek = selectedDay,
                            hour = h,
                            minute = m,
                            minutesBefore = mb,
                            isEnabled = true
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FitGreen)
            ) {
                Text("Set Alarm", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FitTextSecondary) }
        }
    )
}

@Composable
fun RestoreBackupModal(
    onDismiss: () -> Unit,
    onRestore: (String, Boolean) -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    var replaceAll by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitCardBackground,
        title = { Text("Restore From Backup JSON", color = FitTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Paste your backup JSON string:", fontSize = 12.sp, color = FitTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FitGreen,
                        unfocusedBorderColor = FitCardBorder,
                        focusedTextColor = FitTextPrimary,
                        unfocusedTextColor = FitTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = replaceAll,
                        onCheckedChange = { replaceAll = it },
                        colors = CheckboxDefaults.colors(checkedColor = FitGreen)
                    )
                    Text("Replace existing entries", color = FitTextSecondary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (jsonText.isNotBlank()) onRestore(jsonText, replaceAll) },
                colors = ButtonDefaults.buttonColors(containerColor = FitGreen)
            ) {
                Text("Restore Data", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FitTextSecondary) }
        }
    )
}
