package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Mohib",
    val age: Int = 26,
    val gender: String = "Male",
    val heightCm: Float = 178f,
    val weightKg: Float = 75.0f,
    val activityLevel: String = "Moderately Active",
    val fitnessLevel: String = "Intermediate",
    val goals: String = "Gain muscle, Strength, Consistency",
    val availableDays: String = "Monday, Wednesday, Friday, Saturday",
    val preferredDurationMin: Int = 20,
    val equipment: String = "Dumbbells, Bodyweight",
    val weightUnit: String = "kg",
    val heightUnit: String = "cm",
    val distanceUnit: String = "km",
    val defaultRestSecs: Int = 60
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val trackingType: String, // REPS, WEIGHT_REPS, TIME_SECS, DISTANCE_KM
    val instructions: String = "",
    val isCustom: Boolean = false,
    val isFocus: Boolean = false
)

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val goal: String,
    val durationWeeks: Int = 12,
    val daysPerWeek: Int = 3,
    val progressiveOverloadType: String = "MONTHLY", // NONE, WEEKLY, MONTHLY, CUSTOM
    val progressiveOverloadRate: Float = 5.0f, // percentage e.g. 5%
    val progressiveOverloadTarget: String = "REPS_WEIGHT", // REPS, WEIGHT, REPS_WEIGHT, DURATION
    val isActive: Boolean = true,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "plan_days")
data class PlanDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val workoutName: String, // "Full Body A", "Rest Day"
    val isRestDay: Boolean = false,
    val estimatedDurationMin: Int = 20,
    val orderIndex: Int = 0
)

@Entity(tableName = "plan_exercises")
data class PlanExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planDayId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val trackingType: String,
    val orderIndex: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "plan_sets")
data class PlanSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planExerciseId: Long,
    val setNumber: Int,
    val targetReps: Int = 10,
    val targetWeightKg: Float = 0f,
    val targetDurationSecs: Int = 0,
    val targetDistanceKm: Float = 0f,
    val restSecs: Int = 60
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val planDayId: Long,
    val workoutName: String,
    val date: String, // YYYY-MM-DD
    val weekNumber: Int = 1,
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 0L,
    val durationSecs: Int = 0,
    val status: String = "IN_PROGRESS", // COMPLETED, PARTIAL, MISSED, REST, IN_PROGRESS
    val estimatedCalories: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "logged_sets")
data class LoggedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val trackingType: String,
    val setNumber: Int,
    val plannedReps: Int = 0,
    val plannedWeightKg: Float = 0f,
    val plannedDurationSecs: Int = 0,
    val plannedDistanceKm: Float = 0f,
    val actualReps: Int? = null,
    val actualWeightKg: Float? = null,
    val actualDurationSecs: Int? = null,
    val actualDistanceKm: Float? = null,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val weightKg: Float? = null,
    val waistCm: Float? = null,
    val chestCm: Float? = null,
    val hipsCm: Float? = null,
    val neckCm: Float? = null,
    val armCm: Float? = null,
    val thighCm: Float? = null,
    val bodyFatPct: Float? = null,
    val note: String = ""
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val exerciseName: String,
    val recordType: String, // MAX_WEIGHT, MAX_REPS, MAX_DURATION, MAX_VOLUME
    val value: Float,
    val unit: String,
    val date: String, // YYYY-MM-DD
    val details: String = ""
)

@Entity(tableName = "workout_reminders")
data class WorkoutReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1 to 7
    val hour: Int = 8,
    val minute: Int = 0,
    val isEnabled: Boolean = true,
    val minutesBefore: Int = 15,
    val notifyType: String = "SOUND" // SOUND, VIBRATION, SILENT
)
