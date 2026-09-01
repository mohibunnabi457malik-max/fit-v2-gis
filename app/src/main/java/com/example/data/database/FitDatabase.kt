package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FitDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        UserProfileEntity::class,
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        PlanDayEntity::class,
        PlanExerciseEntity::class,
        PlanSetEntity::class,
        WorkoutSessionEntity::class,
        LoggedSetEntity::class,
        BodyMeasurementEntity::class,
        PersonalRecordEntity::class,
        WorkoutReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FitDatabase : RoomDatabase() {
    abstract fun fitDao(): FitDao

    companion object {
        @Volatile
        private var INSTANCE: FitDatabase? = null

        fun getDatabase(context: Context): FitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitDatabase::class.java,
                    "fit_tracker_planner.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    populateInitialData(database.fitDao())
                }
            }
        }

        suspend fun populateInitialData(dao: FitDao) {
            // 1. User Profile
            dao.insertOrUpdateUserProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Mohib",
                    age = 26,
                    gender = "Male",
                    heightCm = 178f,
                    weightKg = 75.0f,
                    activityLevel = "Moderately Active",
                    fitnessLevel = "Intermediate",
                    goals = "Gain muscle, Strength, Consistency",
                    availableDays = "Monday, Wednesday, Friday",
                    preferredDurationMin = 20,
                    equipment = "Dumbbells, Bodyweight",
                    weightUnit = "kg",
                    heightUnit = "cm",
                    distanceUnit = "km",
                    defaultRestSecs = 60
                )
            )

            // 2. Default Exercise Library
            val exercises = listOf(
                ExerciseEntity(name = "Squat", muscleGroup = "Legs", equipment = "Bodyweight", trackingType = "REPS", instructions = "Stand with feet shoulder-width apart. Lower hips back and down while keeping chest up.", isFocus = true),
                ExerciseEntity(name = "Push-Up", muscleGroup = "Chest", equipment = "Bodyweight", trackingType = "REPS", instructions = "Keep body straight in a plank. Lower chest until nearly touching floor, then push back up.", isFocus = true),
                ExerciseEntity(name = "Plank", muscleGroup = "Core", equipment = "Bodyweight", trackingType = "TIME_SECS", instructions = "Hold a straight body position resting on elbows and toes, engaging core and glutes.", isFocus = false),
                ExerciseEntity(name = "Dumbbell Press", muscleGroup = "Chest", equipment = "Dumbbells", trackingType = "WEIGHT_REPS", instructions = "Lie back on bench/floor. Press dumbbells up until arms are fully extended.", isFocus = false),
                ExerciseEntity(name = "Pull-Up", muscleGroup = "Back", equipment = "Bodyweight", trackingType = "REPS", instructions = "Grip bar overhand. Pull body up until chin clears the bar, lowering with control.", isFocus = true),
                ExerciseEntity(name = "Deadlift", muscleGroup = "Back", equipment = "Barbell", trackingType = "WEIGHT_REPS", instructions = "Hinge at hips with neutral spine. Lift weight by extending hips and knees.", isFocus = true),
                ExerciseEntity(name = "Lunges", muscleGroup = "Legs", equipment = "Bodyweight", trackingType = "REPS", instructions = "Step forward and lower hips until both knees are bent at about 90-degree angles.", isFocus = false),
                ExerciseEntity(name = "Bicep Curls", muscleGroup = "Arms", equipment = "Dumbbells", trackingType = "WEIGHT_REPS", instructions = "Keep elbows close to torso. Curl weights while contracting biceps.", isFocus = false),
                ExerciseEntity(name = "Tricep Dips", muscleGroup = "Arms", equipment = "Bodyweight", trackingType = "REPS", instructions = "Use parallel bars or chair edge. Lower body by bending elbows to 90 degrees.", isFocus = false),
                ExerciseEntity(name = "Overhead Shoulder Press", muscleGroup = "Shoulders", equipment = "Dumbbells", trackingType = "WEIGHT_REPS", instructions = "Press dumbbells directly overhead until arms are locked out.", isFocus = false),
                ExerciseEntity(name = "Lateral Raises", muscleGroup = "Shoulders", equipment = "Dumbbells", trackingType = "WEIGHT_REPS", instructions = "Raise arms out to sides with slight elbow bend until parallel with floor.", isFocus = false),
                ExerciseEntity(name = "Mountain Climbers", muscleGroup = "Core", equipment = "Bodyweight", trackingType = "TIME_SECS", instructions = "In plank position, rapidly alternate driving knees towards chest.", isFocus = false),
                ExerciseEntity(name = "Russian Twists", muscleGroup = "Core", equipment = "Bodyweight", trackingType = "REPS", instructions = "Sit with torso leaned back. Rotate torso from side to side.", isFocus = false),
                ExerciseEntity(name = "Running", muscleGroup = "Cardio", equipment = "None", trackingType = "DISTANCE_KM", instructions = "Steady state outdoor or treadmill run.", isFocus = false),
                ExerciseEntity(name = "Jump Rope", muscleGroup = "Cardio", equipment = "Other", trackingType = "TIME_SECS", instructions = "Skip rope at continuous high cadence.", isFocus = false)
            )
            val exerciseIds = dao.insertExercises(exercises)

            val squatId = exerciseIds[0]
            val pushUpId = exerciseIds[1]
            val plankId = exerciseIds[2]
            val dbPressId = exerciseIds[3]

            // 3. Default Plan: "Full Body A"
            val planId = dao.insertPlan(
                WorkoutPlanEntity(
                    name = "Full Body Foundation",
                    goal = "Strength & Muscle",
                    durationWeeks = 12,
                    daysPerWeek = 3,
                    progressiveOverloadType = "MONTHLY",
                    progressiveOverloadRate = 5.0f,
                    progressiveOverloadTarget = "REPS_WEIGHT",
                    isActive = true
                )
            )

            // Day 1: Monday - Full Body A
            val monDayId = dao.insertPlanDay(
                PlanDayEntity(planId = planId, dayOfWeek = 1, workoutName = "Full Body A", isRestDay = false, estimatedDurationMin = 20, orderIndex = 0)
            )
            // Day 2: Tuesday - Rest Day
            dao.insertPlanDay(
                PlanDayEntity(planId = planId, dayOfWeek = 2, workoutName = "Rest Day", isRestDay = true, estimatedDurationMin = 0, orderIndex = 1)
            )
            // Day 3: Wednesday - Full Body B
            val wedDayId = dao.insertPlanDay(
                PlanDayEntity(planId = planId, dayOfWeek = 3, workoutName = "Full Body B", isRestDay = false, estimatedDurationMin = 20, orderIndex = 2)
            )
            // Day 4: Thursday - Rest Day
            dao.insertPlanDay(
                PlanDayEntity(planId = planId, dayOfWeek = 4, workoutName = "Rest Day", isRestDay = true, estimatedDurationMin = 0, orderIndex = 3)
            )
            // Day 5: Friday - Full Body A
            val friDayId = dao.insertPlanDay(
                PlanDayEntity(planId = planId, dayOfWeek = 5, workoutName = "Full Body A", isRestDay = false, estimatedDurationMin = 20, orderIndex = 4)
            )
            // Day 6: Saturday - Active Recovery
            dao.insertPlanDay(
                PlanDayEntity(planId = planId, dayOfWeek = 6, workoutName = "Rest Day", isRestDay = true, estimatedDurationMin = 0, orderIndex = 5)
            )
            // Day 7: Sunday - Rest Day
            dao.insertPlanDay(
                PlanDayEntity(planId = planId, dayOfWeek = 7, workoutName = "Rest Day", isRestDay = true, estimatedDurationMin = 0, orderIndex = 6)
            )

            // Exercises for Monday Full Body A (matching the screenshot reference)
            val ex1Id = dao.insertPlanExercise(PlanExerciseEntity(planDayId = monDayId, exerciseId = squatId, exerciseName = "SQUAT", muscleGroup = "Legs", trackingType = "REPS", orderIndex = 1))
            val ex2Id = dao.insertPlanExercise(PlanExerciseEntity(planDayId = monDayId, exerciseId = pushUpId, exerciseName = "PUSH-UP", muscleGroup = "Chest", trackingType = "REPS", orderIndex = 2))
            val ex3Id = dao.insertPlanExercise(PlanExerciseEntity(planDayId = monDayId, exerciseId = plankId, exerciseName = "PLANK", muscleGroup = "Core", trackingType = "TIME_SECS", orderIndex = 3))
            val ex4Id = dao.insertPlanExercise(PlanExerciseEntity(planDayId = monDayId, exerciseId = dbPressId, exerciseName = "DUMBBELL PRESS", muscleGroup = "Chest", trackingType = "REPS", orderIndex = 4))

            // Plan Sets
            dao.insertPlanSets(listOf(
                PlanSetEntity(planExerciseId = ex1Id, setNumber = 1, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = ex1Id, setNumber = 2, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = ex2Id, setNumber = 1, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = ex2Id, setNumber = 2, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = ex3Id, setNumber = 1, targetDurationSecs = 30, restSecs = 45),
                PlanSetEntity(planExerciseId = ex3Id, setNumber = 2, targetDurationSecs = 30, restSecs = 45),
                PlanSetEntity(planExerciseId = ex4Id, setNumber = 1, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = ex4Id, setNumber = 2, targetReps = 10, restSecs = 60)
            ))

            // Wednesday Full Body B exercises
            val wedEx1 = dao.insertPlanExercise(PlanExerciseEntity(planDayId = wedDayId, exerciseId = pushUpId, exerciseName = "PUSH-UP", muscleGroup = "Chest", trackingType = "REPS", orderIndex = 1))
            val wedEx2 = dao.insertPlanExercise(PlanExerciseEntity(planDayId = wedDayId, exerciseId = squatId, exerciseName = "SQUAT", muscleGroup = "Legs", trackingType = "REPS", orderIndex = 2))
            dao.insertPlanSets(listOf(
                PlanSetEntity(planExerciseId = wedEx1, setNumber = 1, targetReps = 12, restSecs = 60),
                PlanSetEntity(planExerciseId = wedEx1, setNumber = 2, targetReps = 12, restSecs = 60),
                PlanSetEntity(planExerciseId = wedEx2, setNumber = 1, targetReps = 12, restSecs = 60),
                PlanSetEntity(planExerciseId = wedEx2, setNumber = 2, targetReps = 12, restSecs = 60)
            ))

            // Friday Full Body A exercises
            val friEx1 = dao.insertPlanExercise(PlanExerciseEntity(planDayId = friDayId, exerciseId = squatId, exerciseName = "SQUAT", muscleGroup = "Legs", trackingType = "REPS", orderIndex = 1))
            val friEx2 = dao.insertPlanExercise(PlanExerciseEntity(planDayId = friDayId, exerciseId = pushUpId, exerciseName = "PUSH-UP", muscleGroup = "Chest", trackingType = "REPS", orderIndex = 2))
            dao.insertPlanSets(listOf(
                PlanSetEntity(planExerciseId = friEx1, setNumber = 1, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = friEx1, setNumber = 2, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = friEx2, setNumber = 1, targetReps = 10, restSecs = 60),
                PlanSetEntity(planExerciseId = friEx2, setNumber = 2, targetReps = 10, restSecs = 60)
            ))

            // Today's date
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val todayStr = sdf.format(calendar.time)

            // Seed today's session with the exact values from screenshot:
            // Squat: 10/12, 10/10 (+2 reps)
            // Push-up: 10/8, 10/10 (-2 reps)
            // Plank: 30s/35s, 30s/30s (+5s)
            // Dumbbell Press: 10/null, 10/null (Not started)
            val todaySessionId = dao.insertSession(
                WorkoutSessionEntity(
                    planId = planId,
                    planDayId = monDayId,
                    workoutName = "Full Body A",
                    date = todayStr,
                    weekNumber = 3,
                    durationSecs = 1200,
                    status = "IN_PROGRESS",
                    estimatedCalories = 12
                )
            )

            dao.insertLoggedSets(listOf(
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = squatId, exerciseName = "SQUAT", trackingType = "REPS", setNumber = 1, plannedReps = 10, actualReps = 12, isCompleted = true, orderIndex = 1),
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = squatId, exerciseName = "SQUAT", trackingType = "REPS", setNumber = 2, plannedReps = 10, actualReps = 10, isCompleted = true, orderIndex = 1),
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = pushUpId, exerciseName = "PUSH-UP", trackingType = "REPS", setNumber = 1, plannedReps = 10, actualReps = 8, isCompleted = true, orderIndex = 2),
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = pushUpId, exerciseName = "PUSH-UP", trackingType = "REPS", setNumber = 2, plannedReps = 10, actualReps = 10, isCompleted = true, orderIndex = 2),
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = plankId, exerciseName = "PLANK", trackingType = "TIME_SECS", setNumber = 1, plannedDurationSecs = 30, actualDurationSecs = 35, isCompleted = true, orderIndex = 3),
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = plankId, exerciseName = "PLANK", trackingType = "TIME_SECS", setNumber = 2, plannedDurationSecs = 30, actualDurationSecs = 30, isCompleted = true, orderIndex = 3),
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = dbPressId, exerciseName = "DUMBBELL PRESS", trackingType = "REPS", setNumber = 1, plannedReps = 10, actualReps = null, isCompleted = false, orderIndex = 4),
                LoggedSetEntity(sessionId = todaySessionId, exerciseId = dbPressId, exerciseName = "DUMBBELL PRESS", trackingType = "REPS", setNumber = 2, plannedReps = 10, actualReps = null, isCompleted = false, orderIndex = 4)
            ))

            // Seed historical completed sessions for the streak (21 days) & weekly summary
            val histCalendar = Calendar.getInstance()
            for (i in 1..21) {
                histCalendar.add(Calendar.DAY_OF_YEAR, -1)
                val histDateStr = sdf.format(histCalendar.time)
                // skip if rest day or mark completed
                val dayOfWeek = histCalendar.get(Calendar.DAY_OF_WEEK)
                val status = if (dayOfWeek == Calendar.TUESDAY || dayOfWeek == Calendar.THURSDAY || dayOfWeek == Calendar.SUNDAY) "REST" else "COMPLETED"
                dao.insertSession(
                    WorkoutSessionEntity(
                        planId = planId,
                        planDayId = monDayId,
                        workoutName = if (status == "REST") "Rest Day" else "Full Body A",
                        date = histDateStr,
                        weekNumber = 3,
                        durationSecs = if (status == "REST") 0 else 1200,
                        status = status,
                        estimatedCalories = if (status == "REST") 0 else 145
                    )
                )
            }

            // Seed sample body measurements for the Progress Line Graphs
            val measCalendar = Calendar.getInstance()
            val weights = listOf(78.5f, 77.8f, 77.2f, 76.5f, 76.0f, 75.4f, 75.0f)
            val waists = listOf(86f, 85f, 84.5f, 84f, 83f, 82.5f, 82f)
            for (j in weights.indices.reversed()) {
                measCalendar.time = Date()
                measCalendar.add(Calendar.DAY_OF_YEAR, -j * 4)
                val dStr = sdf.format(measCalendar.time)
                dao.insertMeasurement(
                    BodyMeasurementEntity(
                        date = dStr,
                        weightKg = weights[weights.size - 1 - j],
                        waistCm = waists[waists.size - 1 - j],
                        chestCm = 100f + (weights.size - 1 - j) * 0.3f,
                        bodyFatPct = 19.5f - (weights.size - 1 - j) * 0.4f,
                        note = "Morning weigh-in"
                    )
                )
            }

            // Seed Personal Records
            dao.insertPersonalRecord(PersonalRecordEntity(exerciseId = squatId, exerciseName = "Squat", recordType = "MAX_REPS", value = 15f, unit = "reps", date = todayStr, details = "Bodyweight Squat 15 reps"))
            dao.insertPersonalRecord(PersonalRecordEntity(exerciseId = pushUpId, exerciseName = "Push-Up", recordType = "MAX_REPS", value = 25f, unit = "reps", date = todayStr, details = "25 clean reps"))
            dao.insertPersonalRecord(PersonalRecordEntity(exerciseId = plankId, exerciseName = "Plank", recordType = "MAX_DURATION", value = 90f, unit = "secs", date = todayStr, details = "1m 30s isometric hold"))

            // Seed Workout Reminders
            for (day in listOf(1, 3, 5)) {
                dao.insertReminder(
                    WorkoutReminderEntity(
                        dayOfWeek = day,
                        hour = 8,
                        minute = 30,
                        isEnabled = true,
                        minutesBefore = 15,
                        notifyType = "SOUND"
                    )
                )
            }
        }
    }
}
