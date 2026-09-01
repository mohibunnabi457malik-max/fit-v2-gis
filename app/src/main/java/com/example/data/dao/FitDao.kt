package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)

    // Exercises
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercisesFlow(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE isFocus = 1")
    fun getFocusExercisesFlow(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("UPDATE exercises SET isFocus = :isFocus WHERE id = :id")
    suspend fun setFocusExercise(id: Long, isFocus: Boolean)

    // Workout Plans
    @Query("SELECT * FROM workout_plans WHERE isActive = 1 AND isArchived = 0 LIMIT 1")
    fun getActivePlanFlow(): Flow<WorkoutPlanEntity?>

    @Query("SELECT * FROM workout_plans WHERE isActive = 1 AND isArchived = 0 LIMIT 1")
    suspend fun getActivePlan(): WorkoutPlanEntity?

    @Query("SELECT * FROM workout_plans WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActivePlansFlow(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedPlansFlow(): Flow<List<WorkoutPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WorkoutPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: WorkoutPlanEntity)

    @Delete
    suspend fun deletePlan(plan: WorkoutPlanEntity)

    @Query("UPDATE workout_plans SET isActive = 0")
    suspend fun deactivateAllPlans()

    @Query("UPDATE workout_plans SET isActive = 1 WHERE id = :id")
    suspend fun setActivePlan(id: Long)

    @Query("UPDATE workout_plans SET isArchived = :isArchived, isActive = 0 WHERE id = :id")
    suspend fun setPlanArchived(id: Long, isArchived: Boolean)

    // Plan Days
    @Query("SELECT * FROM plan_days WHERE planId = :planId ORDER BY orderIndex ASC, dayOfWeek ASC")
    fun getPlanDaysFlow(planId: Long): Flow<List<PlanDayEntity>>

    @Query("SELECT * FROM plan_days WHERE planId = :planId ORDER BY orderIndex ASC, dayOfWeek ASC")
    suspend fun getPlanDays(planId: Long): List<PlanDayEntity>

    @Query("SELECT * FROM plan_days WHERE planId = :planId AND dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getPlanDayByDayOfWeek(planId: Long, dayOfWeek: Int): PlanDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanDays(days: List<PlanDayEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanDay(day: PlanDayEntity): Long

    @Update
    suspend fun updatePlanDay(day: PlanDayEntity)

    @Query("DELETE FROM plan_days WHERE planId = :planId")
    suspend fun deletePlanDaysByPlanId(planId: Long)

    // Plan Exercises
    @Query("SELECT * FROM plan_exercises WHERE planDayId = :planDayId ORDER BY orderIndex ASC")
    fun getPlanExercisesFlow(planDayId: Long): Flow<List<PlanExerciseEntity>>

    @Query("SELECT * FROM plan_exercises WHERE planDayId = :planDayId ORDER BY orderIndex ASC")
    suspend fun getPlanExercises(planDayId: Long): List<PlanExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanExercises(exercises: List<PlanExerciseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanExercise(exercise: PlanExerciseEntity): Long

    @Update
    suspend fun updatePlanExercise(exercise: PlanExerciseEntity)

    @Delete
    suspend fun deletePlanExercise(exercise: PlanExerciseEntity)

    @Query("DELETE FROM plan_exercises WHERE planDayId = :planDayId")
    suspend fun deletePlanExercisesByDayId(planDayId: Long)

    // Plan Sets
    @Query("SELECT * FROM plan_sets WHERE planExerciseId = :planExerciseId ORDER BY setNumber ASC")
    fun getPlanSetsFlow(planExerciseId: Long): Flow<List<PlanSetEntity>>

    @Query("SELECT * FROM plan_sets WHERE planExerciseId = :planExerciseId ORDER BY setNumber ASC")
    suspend fun getPlanSets(planExerciseId: Long): List<PlanSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanSets(sets: List<PlanSetEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanSet(set: PlanSetEntity): Long

    @Update
    suspend fun updatePlanSet(set: PlanSetEntity)

    @Query("DELETE FROM plan_sets WHERE planExerciseId = :planExerciseId")
    suspend fun deletePlanSetsByExerciseId(planExerciseId: Long)

    // Workout Sessions
    @Query("SELECT * FROM workout_sessions WHERE date = :date LIMIT 1")
    fun getSessionByDateFlow(date: String): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE date = :date LIMIT 1")
    suspend fun getSessionByDate(date: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessionsFlow(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSessionsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getSessionsBetweenDates(startDate: String, endDate: String): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY date DESC")
    suspend fun getCompletedSessions(): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    // Logged Sets
    @Query("SELECT * FROM logged_sets WHERE sessionId = :sessionId ORDER BY orderIndex ASC, setNumber ASC")
    fun getLoggedSetsFlow(sessionId: Long): Flow<List<LoggedSetEntity>>

    @Query("SELECT * FROM logged_sets WHERE sessionId = :sessionId ORDER BY orderIndex ASC, setNumber ASC")
    suspend fun getLoggedSets(sessionId: Long): List<LoggedSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedSet(set: LoggedSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedSets(sets: List<LoggedSetEntity>): List<Long>

    @Update
    suspend fun updateLoggedSet(set: LoggedSetEntity)

    @Query("SELECT * FROM logged_sets WHERE exerciseName = :exerciseName ORDER BY id ASC")
    suspend fun getLoggedSetsForExercise(exerciseName: String): List<LoggedSetEntity>

    // Body Measurements
    @Query("SELECT * FROM body_measurements ORDER BY date ASC")
    fun getAllMeasurementsFlow(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY date ASC")
    suspend fun getAllMeasurements(): List<BodyMeasurementEntity>

    @Query("SELECT * FROM body_measurements WHERE date = :date LIMIT 1")
    suspend fun getMeasurementByDate(date: String): BodyMeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)

    // Personal Records
    @Query("SELECT * FROM personal_records ORDER BY date DESC")
    fun getAllPersonalRecordsFlow(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId")
    suspend fun getRecordsForExercise(exerciseId: Long): List<PersonalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecord(record: PersonalRecordEntity): Long

    // Workout Reminders
    @Query("SELECT * FROM workout_reminders ORDER BY dayOfWeek ASC")
    fun getAllRemindersFlow(): Flow<List<WorkoutReminderEntity>>

    @Query("SELECT * FROM workout_reminders ORDER BY dayOfWeek ASC")
    suspend fun getAllReminders(): List<WorkoutReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: WorkoutReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<WorkoutReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: WorkoutReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: WorkoutReminderEntity)
}
