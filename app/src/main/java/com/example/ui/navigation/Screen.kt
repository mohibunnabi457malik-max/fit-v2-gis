package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Progress : Screen("progress", "Progress")
    object Plans : Screen("plans", "Plans")
    object AiCoach : Screen("ai_coach", "AI Coach")
    object Profile : Screen("profile", "Profile")
    object ActiveWorkout : Screen("active_workout/{sessionId}", "Active Workout") {
        fun createRoute(sessionId: Long) = "active_workout/$sessionId"
    }
}
