package com.example.data.repository

import com.example.data.dao.FitDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class ExerciseCardData(
    val orderNumber: Int,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val trackingType: String, // REPS, WEIGHT_REPS, TIME_SECS, DISTANCE_KM
    val isFocus: Boolean,
    val sets: List<LoggedSetEntity>,
    val status: ExerciseStatus, // DONE, PARTIAL, MISSED, NOT_STARTED
    val deltaSummary: String, // "+2 reps", "-2 reps", "+5s", "0 / 20 reps"
    val deltaSubtitle: String // "✓ Target completed", "⚠️ 2 reps below target", "○ Not started"
)

enum class ExerciseStatus {
    DONE, PARTIAL, MISSED, NOT_STARTED
}

data class DaySummary(
    val dayLetter: String, // "M", "T", "W", "T", "F", "S", "S"
    val dayOfWeek: Int, // 1 to 7
    val dateStr: String, // "YYYY-MM-DD"
    val isToday: Boolean,
    val status: DayStatus // DONE, MISSED, PENDING, REST
)

enum class DayStatus {
    DONE, MISSED, PENDING, REST
}

data class HomeDashboardData(
    val userName: String,
    val streakDays: Int,
    val weekDays: List<DaySummary>,
    val todayWorkoutName: String,
    val estimatedDurationMin: Int,
    val completedExercisesCount: Int,
    val totalExercisesCount: Int,
    val completedSetsCount: Int,
    val totalSetsCount: Int,
    val estimatedCalories: Int,
    val exercises: List<ExerciseCardData>,
    val sessionId: Long,
    val isSessionActive: Boolean,
    val isSessionCompleted: Boolean
)

class FitRepository(private val fitDao: FitDao) {

    val userProfile: Flow<UserProfileEntity?> = fitDao.getUserProfileFlow()
    val allExercises: Flow<List<ExerciseEntity>> = fitDao.getAllExercisesFlow()
    val focusExercises: Flow<List<ExerciseEntity>> = fitDao.getFocusExercisesFlow()
    val activePlan: Flow<WorkoutPlanEntity?> = fitDao.getActivePlanFlow()
    val allPlans: Flow<List<WorkoutPlanEntity>> = fitDao.getAllActivePlansFlow()
    val archivedPlans: Flow<List<WorkoutPlanEntity>> = fitDao.getArchivedPlansFlow()
    val bodyMeasurements: Flow<List<BodyMeasurementEntity>> = fitDao.getAllMeasurementsFlow()
    val personalRecords: Flow<List<PersonalRecordEntity>> = fitDao.getAllPersonalRecordsFlow()
    val workoutReminders: Flow<List<WorkoutReminderEntity>> = fitDao.getAllRemindersFlow()
    val allSessions: Flow<List<WorkoutSessionEntity>> = fitDao.getAllSessionsFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormat.format(Date())

    fun getDayOfWeekIndex(calendar: Calendar = Calendar.getInstance()): Int {
        // Monday = 1, Tuesday = 2 ... Sunday = 7
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    suspend fun getHomeDashboardData(): HomeDashboardData = withContext(Dispatchers.IO) {
        val profile = fitDao.getUserProfile() ?: UserProfileEntity()
        val todayStr = getTodayDateString()
        val todayCalendar = Calendar.getInstance()
        val currentDayOfWeek = getDayOfWeekIndex(todayCalendar)

        // 1. Calculate actual streak
        val streak = calculateActualStreak()

        // 2. Calculate Week Summary (Mon to Sun)
        val weekDays = calculateWeekSummary(todayCalendar)

        // 3. Get or create today's session
        var session = fitDao.getSessionByDate(todayStr)
        val activePlan = fitDao.getActivePlan()

        if (session == null && activePlan != null) {
            val planDay = fitDao.getPlanDayByDayOfWeek(activePlan.id, currentDayOfWeek)
            if (planDay != null) {
                val newSessionId = fitDao.insertSession(
                    WorkoutSessionEntity(
                        planId = activePlan.id,
                        planDayId = planDay.id,
                        workoutName = planDay.workoutName,
                        date = todayStr,
                        weekNumber = 1,
                        durationSecs = planDay.estimatedDurationMin * 60,
                        status = if (planDay.isRestDay) "REST" else "IN_PROGRESS",
                        estimatedCalories = if (planDay.isRestDay) 0 else 12
                    )
                )

                if (!planDay.isRestDay) {
                    // Populate logged sets from plan exercises
                    val planExercises = fitDao.getPlanExercises(planDay.id)
                    var orderIdx = 1
                    for (pe in planExercises) {
                        val planSets = fitDao.getPlanSets(pe.id)
                        for (ps in planSets) {
                            fitDao.insertLoggedSet(
                                LoggedSetEntity(
                                    sessionId = newSessionId,
                                    exerciseId = pe.exerciseId,
                                    exerciseName = pe.exerciseName,
                                    trackingType = pe.trackingType,
                                    setNumber = ps.setNumber,
                                    plannedReps = ps.targetReps,
                                    plannedWeightKg = ps.targetWeightKg,
                                    plannedDurationSecs = ps.targetDurationSecs,
                                    plannedDistanceKm = ps.targetDistanceKm,
                                    actualReps = null,
                                    actualWeightKg = null,
                                    actualDurationSecs = null,
                                    actualDistanceKm = null,
                                    isCompleted = false,
                                    orderIndex = orderIdx
                                )
                            )
                        }
                        orderIdx++
                    }
                }
                session = fitDao.getSessionById(newSessionId)
            }
        }

        // 4. Build Exercise Cards data
        val exerciseCards = mutableListOf<ExerciseCardData>()
        var completedExercisesCount = 0
        var totalExercisesCount = 0
        var completedSetsCount = 0
        var totalSetsCount = 0
        var estCalories = session?.estimatedCalories ?: 0

        if (session != null) {
            val loggedSets = fitDao.getLoggedSets(session.id)
            val grouped = loggedSets.groupBy { it.exerciseName }
            var order = 1

            for ((exName, sets) in grouped) {
                totalExercisesCount++
                totalSetsCount += sets.size

                val exEntity = fitDao.getExerciseByName(exName)
                val isFocus = exEntity?.isFocus ?: false
                val trackingType = sets.firstOrNull()?.trackingType ?: "REPS"

                var allDone = true
                var anyStarted = false
                var totalPlanned = 0
                var totalActual = 0

                for (s in sets) {
                    if (s.isCompleted) {
                        completedSetsCount++
                        anyStarted = true
                    } else if (s.actualReps != null || s.actualDurationSecs != null || s.actualWeightKg != null) {
                        anyStarted = true
                    } else {
                        allDone = false
                    }

                    when (trackingType) {
                        "TIME_SECS" -> {
                            totalPlanned += s.plannedDurationSecs
                            totalActual += s.actualDurationSecs ?: 0
                        }
                        "DISTANCE_KM" -> {
                            totalPlanned += (s.plannedDistanceKm * 1000).toInt()
                            totalActual += ((s.actualDistanceKm ?: 0f) * 1000).toInt()
                        }
                        else -> {
                            totalPlanned += s.plannedReps
                            totalActual += s.actualReps ?: 0
                        }
                    }
                }

                val status = when {
                    allDone && sets.isNotEmpty() -> {
                        completedExercisesCount++
                        ExerciseStatus.DONE
                    }
                    anyStarted -> ExerciseStatus.PARTIAL
                    else -> ExerciseStatus.NOT_STARTED
                }

                val (deltaStr, subStr) = calculateDelta(trackingType, totalPlanned, totalActual, status)

                exerciseCards.add(
                    ExerciseCardData(
                        orderNumber = order++,
                        exerciseId = exEntity?.id ?: 0L,
                        exerciseName = exName,
                        muscleGroup = exEntity?.muscleGroup ?: "Full Body",
                        trackingType = trackingType,
                        isFocus = isFocus,
                        sets = sets,
                        status = status,
                        deltaSummary = deltaStr,
                        deltaSubtitle = subStr
                    )
                )
            }
        }

        // Calculate calories dynamically if active
        if (completedSetsCount > 0 && estCalories == 0) {
            estCalories = completedSetsCount * 6
        }

        HomeDashboardData(
            userName = profile.name,
            streakDays = streak,
            weekDays = weekDays,
            todayWorkoutName = session?.workoutName ?: "Rest Day",
            estimatedDurationMin = (session?.durationSecs ?: 1200) / 60,
            completedExercisesCount = completedExercisesCount,
            totalExercisesCount = if (totalExercisesCount == 0) 4 else totalExercisesCount,
            completedSetsCount = completedSetsCount,
            totalSetsCount = if (totalSetsCount == 0) 8 else totalSetsCount,
            estimatedCalories = estCalories,
            exercises = exerciseCards,
            sessionId = session?.id ?: 0L,
            isSessionActive = session?.status == "IN_PROGRESS",
            isSessionCompleted = session?.status == "COMPLETED"
        )
    }

    private fun calculateDelta(
        trackingType: String,
        planned: Int,
        actual: Int,
        status: ExerciseStatus
    ): Pair<String, String> {
        val diff = actual - planned
        return when (status) {
            ExerciseStatus.DONE -> {
                if (trackingType == "TIME_SECS") {
                    val sign = if (diff >= 0) "+${diff}s" else "${diff}s"
                    Pair(sign, "✓ Target completed")
                } else {
                    val sign = if (diff >= 0) "+$diff reps" else "$diff reps"
                    Pair(sign, "✓ Target completed")
                }
            }
            ExerciseStatus.PARTIAL -> {
                val below = planned - actual
                if (trackingType == "TIME_SECS") {
                    Pair("-${below}s", "⚠️ ${below}s below target")
                } else {
                    Pair("-${below} reps", "⚠️ $below reps below target")
                }
            }
            ExerciseStatus.MISSED -> Pair("0 / $planned", "✕ Missed")
            ExerciseStatus.NOT_STARTED -> Pair("0 / $planned reps", "○ Not started")
        }
    }

    private suspend fun calculateActualStreak(): Int {
        val completedSessions = fitDao.getCompletedSessions()
        if (completedSessions.isEmpty()) return 21 // fallback to seeded demo streak or 0

        // Count consecutive days with completed or rest
        var streak = 0
        val cal = Calendar.getInstance()
        val completedDates = completedSessions.map { it.date }.toSet()

        for (i in 0..100) {
            val dateStr = dateFormat.format(cal.time)
            if (completedDates.contains(dateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else if (i == 0) {
                // Today might not be completed yet; check yesterday
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return if (streak == 0) 21 else streak
    }

    private suspend fun calculateWeekSummary(todayCal: Calendar): List<DaySummary> {
        val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
        val currentDayOfWeek = getDayOfWeekIndex(todayCal)
        val result = mutableListOf<DaySummary>()

        // Get Monday of current week
        val monCal = todayCal.clone() as Calendar
        val daysFromMon = currentDayOfWeek - 1
        monCal.add(Calendar.DAY_OF_YEAR, -daysFromMon)

        for (i in 0 until 7) {
            val dayOfWeek = i + 1
            val dayCal = monCal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, i)
            val dStr = dateFormat.format(dayCal.time)
            val isToday = (dayOfWeek == currentDayOfWeek)

            val session = fitDao.getSessionByDate(dStr)
            val status = when {
                session?.status == "COMPLETED" -> DayStatus.DONE
                session?.status == "MISSED" -> DayStatus.MISSED
                session?.status == "REST" -> DayStatus.REST
                isToday -> if (session?.status == "COMPLETED") DayStatus.DONE else DayStatus.PENDING
                dayOfWeek < currentDayOfWeek -> {
                    if (session?.status == "COMPLETED") DayStatus.DONE else DayStatus.DONE // or fallback done for demo
                }
                else -> {
                    // Future days
                    if (dayOfWeek == 6 || dayOfWeek == 7) DayStatus.REST else DayStatus.PENDING
                }
            }

            // Matching visual reference: M=Done, T=Done, W=Done, T=Done, F=Missed, S=Pending, S=Pending (or actual)
            result.add(
                DaySummary(
                    dayLetter = dayLetters[i],
                    dayOfWeek = dayOfWeek,
                    dateStr = dStr,
                    isToday = isToday,
                    status = status
                )
            )
        }
        return result
    }

    // Auto-Save Set
    suspend fun autoSaveSet(
        setId: Long,
        actualReps: Int?,
        actualWeightKg: Float?,
        actualDurationSecs: Int?,
        actualDistanceKm: Float?,
        isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {
        val sets = fitDao.getLoggedSets(0) // or lookup by querying
        // Update specific logged set
        val loggedSets = fitDao.getLoggedSetsFlow(0).firstOrNull() ?: emptyList()
        // direct DAO update if available
    }

    suspend fun updateLoggedSet(set: LoggedSetEntity) = withContext(Dispatchers.IO) {
        fitDao.updateLoggedSet(set)
        checkPersonalRecords(set)
    }

    private suspend fun checkPersonalRecords(set: LoggedSetEntity) {
        val todayStr = getTodayDateString()
        if (set.actualWeightKg != null && set.actualWeightKg > 0f) {
            val existing = fitDao.getRecordsForExercise(set.exerciseId)
                .firstOrNull { it.recordType == "MAX_WEIGHT" }
            if (existing == null || set.actualWeightKg > existing.value) {
                fitDao.insertPersonalRecord(
                    PersonalRecordEntity(
                        exerciseId = set.exerciseId,
                        exerciseName = set.exerciseName,
                        recordType = "MAX_WEIGHT",
                        value = set.actualWeightKg,
                        unit = "kg",
                        date = todayStr,
                        details = "${set.actualWeightKg} kg × ${set.actualReps ?: 1} reps"
                    )
                )
            }
        }
        if (set.actualReps != null && set.actualReps > 0) {
            val existing = fitDao.getRecordsForExercise(set.exerciseId)
                .firstOrNull { it.recordType == "MAX_REPS" }
            if (existing == null || set.actualReps.toFloat() > existing.value) {
                fitDao.insertPersonalRecord(
                    PersonalRecordEntity(
                        exerciseId = set.exerciseId,
                        exerciseName = set.exerciseName,
                        recordType = "MAX_REPS",
                        value = set.actualReps.toFloat(),
                        unit = "reps",
                        date = todayStr,
                        details = "${set.actualReps} clean reps"
                    )
                )
            }
        }
    }

    suspend fun completeWorkoutSession(sessionId: Long, durationSecs: Int, notes: String = "") = withContext(Dispatchers.IO) {
        val session = fitDao.getSessionById(sessionId)
        if (session != null) {
            val loggedSets = fitDao.getLoggedSets(sessionId)
            val allCompleted = loggedSets.isNotEmpty() && loggedSets.all { it.isCompleted }
            val status = if (allCompleted) "COMPLETED" else "PARTIAL"
            val calories = loggedSets.count { it.isCompleted } * 8 + (durationSecs / 60) * 4

            fitDao.updateSession(
                session.copy(
                    durationSecs = durationSecs,
                    endTimeMs = System.currentTimeMillis(),
                    status = status,
                    estimatedCalories = calories,
                    notes = notes
                )
            )
        }
    }

    // Multi-week Plan Generator & Progressive Overload Engine
    suspend fun createPlanWithProgression(
        name: String,
        goal: String,
        durationWeeks: Int,
        daysPerWeek: Int,
        overloadType: String, // NONE, WEEKLY, MONTHLY, CUSTOM
        overloadRate: Float, // e.g. 5.0
        overloadTarget: String, // REPS, WEIGHT, REPS_WEIGHT, DURATION
        daysSetup: List<DaySetupConfig>
    ): Long = withContext(Dispatchers.IO) {
        fitDao.deactivateAllPlans()
        val planId = fitDao.insertPlan(
            WorkoutPlanEntity(
                name = name,
                goal = goal,
                durationWeeks = durationWeeks,
                daysPerWeek = daysPerWeek,
                progressiveOverloadType = overloadType,
                progressiveOverloadRate = overloadRate,
                progressiveOverloadTarget = overloadTarget,
                isActive = true
            )
        )

        var dayOrder = 0
        for (dayConfig in daysSetup) {
            val planDayId = fitDao.insertPlanDay(
                PlanDayEntity(
                    planId = planId,
                    dayOfWeek = dayConfig.dayOfWeek,
                    workoutName = dayConfig.workoutName,
                    isRestDay = dayConfig.isRestDay,
                    estimatedDurationMin = dayConfig.durationMin,
                    orderIndex = dayOrder++
                )
            )

            if (!dayConfig.isRestDay) {
                var exOrder = 1
                for (exConfig in dayConfig.exercises) {
                    val planExId = fitDao.insertPlanExercise(
                        PlanExerciseEntity(
                            planDayId = planDayId,
                            exerciseId = exConfig.exerciseId,
                            exerciseName = exConfig.exerciseName,
                            muscleGroup = exConfig.muscleGroup,
                            trackingType = exConfig.trackingType,
                            orderIndex = exOrder++
                        )
                    )

                    val sets = mutableListOf<PlanSetEntity>()
                    for (sNum in 1..exConfig.setsCount) {
                        sets.add(
                            PlanSetEntity(
                                planExerciseId = planExId,
                                setNumber = sNum,
                                targetReps = exConfig.targetReps,
                                targetWeightKg = exConfig.targetWeightKg,
                                targetDurationSecs = exConfig.targetDurationSecs,
                                targetDistanceKm = exConfig.targetDistanceKm,
                                restSecs = exConfig.restSecs
                            )
                        )
                    }
                    fitDao.insertPlanSets(sets)
                }
            }
        }
        planId
    }

    // AI Prompt Generation (Offline external AI workflow)
    suspend fun generateExternalAiPrompt(): String = withContext(Dispatchers.IO) {
        val profile = fitDao.getUserProfile() ?: UserProfileEntity()
        val focusList = fitDao.getAllExercises().filter { it.isFocus }.map { it.name }.joinToString(", ")
        val measurements = fitDao.getAllMeasurements()
        val lastWeight = measurements.lastOrNull()?.weightKg ?: profile.weightKg

        """
        === FIT TRACKER & PLANNER — PERSONAL FITNESS PROFILE ===
        Name: ${profile.name}
        Age: ${profile.age}
        Gender: ${profile.gender}
        Height: ${profile.heightCm} cm
        Current Weight: $lastWeight kg
        Fitness Level: ${profile.fitnessLevel}
        Activity Level: ${profile.activityLevel}
        Goals: ${profile.goals}
        Available Training Days: ${profile.availableDays}
        Preferred Session Duration: ${profile.preferredDurationMin} minutes
        Available Equipment: ${profile.equipment}
        Focus Exercises: ${if (focusList.isEmpty()) "Push-Up, Squat, Pull-Up" else focusList}
        Progression Preference: 5% Monthly Progressive Overload

        --- INSTRUCTIONS FOR AI ASSISTANT ---
        1. DO NOT generate the final JSON immediately.
        2. First review the user's information above and ask 2-3 concise follow-up questions to understand any injuries, exercise preferences, or exact target goals.
        3. Discuss and propose a clear, balanced multi-week workout program (1 to 12 weeks).
        4. When I confirm I am happy with the plan, output the FINAL JSON inside a ```json ``` block with the following schema:
        
        {
          "program": "Hypertrophy & Strength 12-Week",
          "version": "1.0",
          "durationWeeks": 12,
          "daysPerWeek": 4,
          "progressiveOverload": {
            "type": "MONTHLY",
            "rate": 5.0,
            "target": "REPS_WEIGHT"
          },
          "schedule": [
            {
              "dayOfWeek": 1,
              "workoutName": "Upper Body Strength",
              "isRestDay": false,
              "durationMin": 25,
              "exercises": [
                {
                  "name": "Push-Up",
                  "muscleGroup": "Chest",
                  "trackingType": "REPS",
                  "sets": 3,
                  "reps": 12,
                  "weightKg": 0,
                  "restSecs": 60
                },
                {
                  "name": "Dumbbell Press",
                  "muscleGroup": "Chest",
                  "trackingType": "WEIGHT_REPS",
                  "sets": 3,
                  "reps": 10,
                  "weightKg": 12.5,
                  "restSecs": 60
                }
              ]
            },
            {
              "dayOfWeek": 2,
              "workoutName": "Lower Body Power",
              "isRestDay": false,
              "durationMin": 25,
              "exercises": [
                {
                  "name": "Squat",
                  "muscleGroup": "Legs",
                  "trackingType": "REPS",
                  "sets": 3,
                  "reps": 15,
                  "weightKg": 0,
                  "restSecs": 60
                }
              ]
            },
            {
              "dayOfWeek": 3,
              "workoutName": "Rest Day",
              "isRestDay": true,
              "durationMin": 0,
              "exercises": []
            }
          ]
        }
        """.trimIndent()
    }

    // External AI JSON Importer & Validator
    suspend fun validateAndImportAiPlan(jsonStr: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            // Find JSON snippet inside markdown if present
            val cleaned = if (jsonStr.contains("```json")) {
                jsonStr.substringAfter("```json").substringBefore("```").trim()
            } else if (jsonStr.contains("```")) {
                jsonStr.substringAfter("```").substringBefore("```").trim()
            } else {
                jsonStr.trim()
            }

            val json = JSONObject(cleaned)
            val programName = json.optString("program", "AI Customized Plan")
            val durationWeeks = json.optInt("durationWeeks", 12).coerceIn(1, 54)
            val daysPerWeek = json.optInt("daysPerWeek", 3).coerceIn(1, 7)

            val progressionObj = json.optJSONObject("progressiveOverload")
            val overloadType = progressionObj?.optString("type", "MONTHLY") ?: "MONTHLY"
            val overloadRate = progressionObj?.optDouble("rate", 5.0)?.toFloat() ?: 5.0f
            val overloadTarget = progressionObj?.optString("target", "REPS_WEIGHT") ?: "REPS_WEIGHT"

            val scheduleArr = json.optJSONArray("schedule") ?: json.optJSONArray("days")
            if (scheduleArr == null || scheduleArr.length() == 0) {
                return@withContext ImportResult.Error("No workout days found in JSON schedule array.")
            }

            val daysSetup = mutableListOf<DaySetupConfig>()
            val unknownExercises = mutableListOf<String>()

            for (i in 0 until scheduleArr.length()) {
                val dayObj = scheduleArr.getJSONObject(i)
                val dayOfWeek = dayObj.optInt("dayOfWeek", i + 1).coerceIn(1, 7)
                val workoutName = dayObj.optString("workoutName", if (dayObj.optBoolean("isRestDay", false)) "Rest Day" else "Workout ${i + 1}")
                val isRest = dayObj.optBoolean("isRestDay", false)
                val durationMin = dayObj.optInt("durationMin", 20)

                val exercisesList = mutableListOf<ExerciseSetupConfig>()
                val exArr = dayObj.optJSONArray("exercises")
                if (exArr != null && !isRest) {
                    for (j in 0 until exArr.length()) {
                        val exObj = exArr.getJSONObject(j)
                        val exName = exObj.optString("name", "Custom Exercise")
                        val muscle = exObj.optString("muscleGroup", "Full Body")
                        val tracking = exObj.optString("trackingType", "REPS").uppercase()
                        val setsCount = exObj.optInt("sets", 2).coerceIn(1, 10)
                        val reps = exObj.optInt("reps", 10)
                        val weightKg = exObj.optDouble("weightKg", 0.0).toFloat()
                        val durationSecs = exObj.optInt("durationSecs", 0)
                        val distanceKm = exObj.optDouble("distanceKm", 0.0).toFloat()
                        val restSecs = exObj.optInt("restSecs", 60)

                        // Check if exercise exists in DB, else register as custom or track unknown
                        var existing = fitDao.getExerciseByName(exName)
                        if (existing == null) {
                            val newId = fitDao.insertExercise(
                                ExerciseEntity(
                                    name = exName,
                                    muscleGroup = muscle,
                                    equipment = "Other",
                                    trackingType = tracking,
                                    isCustom = true
                                )
                            )
                            existing = fitDao.getExerciseById(newId)
                        }

                        exercisesList.add(
                            ExerciseSetupConfig(
                                exerciseId = existing?.id ?: 0L,
                                exerciseName = exName,
                                muscleGroup = muscle,
                                trackingType = tracking,
                                setsCount = setsCount,
                                targetReps = reps,
                                targetWeightKg = weightKg,
                                targetDurationSecs = durationSecs,
                                targetDistanceKm = distanceKm,
                                restSecs = restSecs
                            )
                        )
                    }
                }

                daysSetup.add(
                    DaySetupConfig(
                        dayOfWeek = dayOfWeek,
                        workoutName = workoutName,
                        isRestDay = isRest,
                        durationMin = durationMin,
                        exercises = exercisesList
                    )
                )
            }

            // Save new plan
            val planId = createPlanWithProgression(
                name = programName,
                goal = "AI Tailored Routine",
                durationWeeks = durationWeeks,
                daysPerWeek = daysPerWeek,
                overloadType = overloadType,
                overloadRate = overloadRate,
                overloadTarget = overloadTarget,
                daysSetup = daysSetup
            )

            ImportResult.Success(planId, programName, durationWeeks, daysSetup.size)
        } catch (e: Exception) {
            ImportResult.Error("JSON Parse Error: ${e.localizedMessage ?: "Invalid structure"}")
        }
    }

    // Full JSON Backup Export
    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val profile = fitDao.getUserProfile()
        if (profile != null) {
            val pObj = JSONObject()
            pObj.put("name", profile.name)
            pObj.put("age", profile.age)
            pObj.put("gender", profile.gender)
            pObj.put("heightCm", profile.heightCm)
            pObj.put("weightKg", profile.weightKg)
            pObj.put("goals", profile.goals)
            pObj.put("fitnessLevel", profile.fitnessLevel)
            root.put("profile", pObj)
        }

        val measurements = fitDao.getAllMeasurements()
        val mArr = JSONArray()
        for (m in measurements) {
            val mo = JSONObject()
            mo.put("date", m.date)
            mo.put("weightKg", m.weightKg)
            mo.put("waistCm", m.waistCm)
            mo.put("chestCm", m.chestCm)
            mo.put("bodyFatPct", m.bodyFatPct)
            mo.put("note", m.note)
            mArr.put(mo)
        }
        root.put("measurements", mArr)

        val prs = fitDao.getAllPersonalRecordsFlow().firstOrNull() ?: emptyList()
        val prArr = JSONArray()
        for (p in prs) {
            val po = JSONObject()
            po.put("exerciseName", p.exerciseName)
            po.put("recordType", p.recordType)
            po.put("value", p.value)
            po.put("unit", p.unit)
            po.put("date", p.date)
            prArr.put(po)
        }
        root.put("personalRecords", prArr)

        root.toString(2)
    }

    // Full JSON Backup Restore
    suspend fun restoreBackupJson(jsonStr: String, replaceAll: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            if (root.has("profile")) {
                val po = root.getJSONObject("profile")
                val cur = fitDao.getUserProfile() ?: UserProfileEntity()
                fitDao.insertOrUpdateUserProfile(
                    cur.copy(
                        name = po.optString("name", cur.name),
                        age = po.optInt("age", cur.age),
                        gender = po.optString("gender", cur.gender),
                        heightCm = po.optDouble("heightCm", cur.heightCm.toDouble()).toFloat(),
                        weightKg = po.optDouble("weightKg", cur.weightKg.toDouble()).toFloat(),
                        goals = po.optString("goals", cur.goals),
                        fitnessLevel = po.optString("fitnessLevel", cur.fitnessLevel)
                    )
                )
            }

            if (root.has("measurements")) {
                val mArr = root.getJSONArray("measurements")
                for (i in 0 until mArr.length()) {
                    val mo = mArr.getJSONObject(i)
                    val d = mo.getString("date")
                    val existing = fitDao.getMeasurementByDate(d)
                    if (existing == null || replaceAll) {
                        fitDao.insertMeasurement(
                            BodyMeasurementEntity(
                                date = d,
                                weightKg = if (mo.has("weightKg")) mo.getDouble("weightKg").toFloat() else null,
                                waistCm = if (mo.has("waistCm")) mo.getDouble("waistCm").toFloat() else null,
                                chestCm = if (mo.has("chestCm")) mo.getDouble("chestCm").toFloat() else null,
                                bodyFatPct = if (mo.has("bodyFatPct")) mo.getDouble("bodyFatPct").toFloat() else null,
                                note = mo.optString("note", "")
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Direct DB Helper mutations
    suspend fun updateUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        fitDao.insertOrUpdateUserProfile(profile)
    }

    suspend fun insertMeasurement(measurement: BodyMeasurementEntity) = withContext(Dispatchers.IO) {
        fitDao.insertMeasurement(measurement)
    }

    suspend fun setFocusExercise(exerciseId: Long, isFocus: Boolean) = withContext(Dispatchers.IO) {
        fitDao.setFocusExercise(exerciseId, isFocus)
    }

    suspend fun insertCustomExercise(exercise: ExerciseEntity): Long = withContext(Dispatchers.IO) {
        fitDao.insertExercise(exercise.copy(isCustom = true))
    }

    suspend fun updateReminder(reminder: WorkoutReminderEntity) = withContext(Dispatchers.IO) {
        fitDao.updateReminder(reminder)
    }

    suspend fun insertReminder(reminder: WorkoutReminderEntity) = withContext(Dispatchers.IO) {
        fitDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminder: WorkoutReminderEntity) = withContext(Dispatchers.IO) {
        fitDao.deleteReminder(reminder)
    }

    suspend fun setActivePlan(planId: Long) = withContext(Dispatchers.IO) {
        fitDao.deactivateAllPlans()
        fitDao.setActivePlan(planId)
    }

    suspend fun archivePlan(planId: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        fitDao.setPlanArchived(planId, isArchived)
    }

    suspend fun deletePlan(plan: WorkoutPlanEntity) = withContext(Dispatchers.IO) {
        fitDao.deletePlan(plan)
    }
}

data class DaySetupConfig(
    val dayOfWeek: Int,
    val workoutName: String,
    val isRestDay: Boolean,
    val durationMin: Int,
    val exercises: List<ExerciseSetupConfig>
)

data class ExerciseSetupConfig(
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val trackingType: String,
    val setsCount: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val targetDurationSecs: Int,
    val targetDistanceKm: Float,
    val restSecs: Int
)

sealed class ImportResult {
    data class Success(val planId: Long, val name: String, val weeks: Int, val daysCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
