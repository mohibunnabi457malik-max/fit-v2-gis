package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.FitDatabase
import com.example.data.model.*
import com.example.data.repository.*
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FitDatabase.getDatabase(application)
    private val repository = FitRepository(db.fitDao())

    // Observables
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allExercises = repository.allExercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activePlan = repository.activePlan.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allPlans = repository.allPlans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedPlans = repository.archivedPlans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bodyMeasurements = repository.bodyMeasurements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val personalRecords = repository.personalRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workoutReminders = repository.workoutReminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _homeData = MutableStateFlow<HomeDashboardData?>(null)
    val homeData: StateFlow<HomeDashboardData?> = _homeData.asStateFlow()

    private val _aiPrompt = MutableStateFlow<String>("")
    val aiPrompt: StateFlow<String> = _aiPrompt.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        loadHomeData()
        generateAiPrompt()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            val data = repository.getHomeDashboardData()
            _homeData.value = data
        }
    }

    fun updateLoggedSet(set: LoggedSetEntity) {
        viewModelScope.launch {
            repository.updateLoggedSet(set)
            loadHomeData()
        }
    }

    fun completeSession(sessionId: Long, durationSecs: Int, notes: String = "") {
        viewModelScope.launch {
            repository.completeWorkoutSession(sessionId, durationSecs, notes)
            loadHomeData()
            _toastMessage.emit("Workout completed and saved locally! 🎉")
        }
    }

    fun logMeasurement(weight: Float?, waist: Float?, chest: Float?, bodyFat: Float?, note: String) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            repository.insertMeasurement(
                BodyMeasurementEntity(
                    date = todayStr,
                    weightKg = weight,
                    waistCm = waist,
                    chestCm = chest,
                    bodyFatPct = bodyFat,
                    note = note
                )
            )
            _toastMessage.emit("Measurement logged successfully!")
        }
    }

    fun toggleFocusExercise(exerciseId: Long, isFocus: Boolean) {
        viewModelScope.launch {
            repository.setFocusExercise(exerciseId, isFocus)
        }
    }

    fun addCustomExercise(name: String, muscleGroup: String, equipment: String, trackingType: String, instructions: String) {
        viewModelScope.launch {
            repository.insertCustomExercise(
                ExerciseEntity(
                    name = name,
                    muscleGroup = muscleGroup,
                    equipment = equipment,
                    trackingType = trackingType,
                    instructions = instructions,
                    isCustom = true
                )
            )
            _toastMessage.emit("Custom exercise added to library!")
        }
    }

    fun createPlan(
        name: String,
        goal: String,
        durationWeeks: Int,
        daysPerWeek: Int,
        overloadType: String,
        overloadRate: Float,
        overloadTarget: String,
        daysSetup: List<DaySetupConfig>
    ) {
        viewModelScope.launch {
            val planId = repository.createPlanWithProgression(
                name = name,
                goal = goal,
                durationWeeks = durationWeeks,
                daysPerWeek = daysPerWeek,
                overloadType = overloadType,
                overloadRate = overloadRate,
                overloadTarget = overloadTarget,
                daysSetup = daysSetup
            )
            loadHomeData()
            _toastMessage.emit("Created $durationWeeks-week program: $name! 🚀")
        }
    }

    fun generateAiPrompt() {
        viewModelScope.launch {
            val prompt = repository.generateExternalAiPrompt()
            _aiPrompt.value = prompt
        }
    }

    fun importAiJsonPlan(jsonStr: String, onResult: (ImportResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.validateAndImportAiPlan(jsonStr)
            if (result is ImportResult.Success) {
                loadHomeData()
                _toastMessage.emit("Successfully imported AI plan: ${result.name}!")
            }
            onResult(result)
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportFullBackupJson()
            onResult(json)
        }
    }

    fun restoreBackup(jsonStr: String, replaceAll: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreBackupJson(jsonStr, replaceAll)
            if (success) {
                loadHomeData()
                _toastMessage.emit("Backup restored successfully!")
            }
            onComplete(success)
        }
    }

    fun updateUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
            loadHomeData()
            generateAiPrompt()
            _toastMessage.emit("Profile updated!")
        }
    }

    fun saveReminder(reminder: WorkoutReminderEntity) {
        viewModelScope.launch {
            if (reminder.id == 0L) {
                repository.insertReminder(reminder)
            } else {
                repository.updateReminder(reminder)
            }
            NotificationHelper.scheduleWorkoutReminder(getApplication(), reminder)
            _toastMessage.emit("Workout reminder updated!")
        }
    }

    fun deleteReminder(reminder: WorkoutReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            NotificationHelper.cancelWorkoutReminder(getApplication(), reminder.id.toInt())
            _toastMessage.emit("Reminder removed.")
        }
    }

    fun switchActivePlan(planId: Long) {
        viewModelScope.launch {
            repository.setActivePlan(planId)
            loadHomeData()
            _toastMessage.emit("Active plan switched!")
        }
    }

    fun archivePlan(planId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.archivePlan(planId, isArchived)
            loadHomeData()
        }
    }
}
