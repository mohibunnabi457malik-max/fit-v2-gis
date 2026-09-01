package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.FitViewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.activeworkout.ActiveWorkoutScreen
import com.example.ui.screens.aicoach.AiCoachScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.plans.PlansScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.theme.FitCardBackground
import com.example.ui.theme.FitCyan
import com.example.ui.theme.FitDarkBackground
import com.example.ui.theme.FitGreen
import com.example.ui.theme.FitOrange
import com.example.ui.theme.FitTextPrimary
import com.example.ui.theme.FitTextSecondary
import com.example.ui.theme.FitTrackerTheme
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            FitTrackerTheme {
                FitTrackerApp()
            }
        }
    }
}

@Composable
fun FitTrackerApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: FitViewModel = viewModel()

    // Request Notification Permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Workout alarms may not show popups without notification permission", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Toast event listener
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    MainAppLayout(
        navController = navController,
        viewModel = viewModel
    )
}

@Composable
fun MainAppLayout(
    navController: NavHostController,
    viewModel: FitViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Progress.route,
        Screen.Plans.route,
        Screen.AiCoach.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FitDarkBackground,
        bottomBar = {
            if (showBottomBar) {
                FitBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onStartWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    },
                    onNavigateToPlans = {
                        navController.navigate(Screen.Plans.route)
                    }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(viewModel = viewModel)
            }

            composable(Screen.Plans.route) {
                PlansScreen(
                    viewModel = viewModel,
                    onNavigateToAiCoach = {
                        navController.navigate(Screen.AiCoach.route)
                    }
                )
            }

            composable(Screen.AiCoach.route) {
                AiCoachScreen(
                    viewModel = viewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.ActiveWorkout.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                ActiveWorkoutScreen(
                    sessionId = sessionId,
                    viewModel = viewModel,
                    onFinishWorkout = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun FitBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = FitCardBackground,
        contentColor = FitTextPrimary,
        tonalElevation = 8.dp
    ) {
        // 1. Home (Workout selection)
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Screen.Home.route) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FitCyan,
                selectedTextColor = FitCyan,
                unselectedIconColor = FitTextSecondary,
                unselectedTextColor = FitTextSecondary,
                indicatorColor = Color(0xFF132A44)
            ),
            modifier = Modifier.testTag("nav_home")
        )

        // 2. Progress (Line graph for weight & workouts)
        NavigationBarItem(
            selected = currentRoute == Screen.Progress.route,
            onClick = { onNavigate(Screen.Progress) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Screen.Progress.route) Icons.Filled.Timeline else Icons.Outlined.Timeline,
                    contentDescription = "Progress",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Progress", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FitGreen,
                selectedTextColor = FitGreen,
                unselectedIconColor = FitTextSecondary,
                unselectedTextColor = FitTextSecondary,
                indicatorColor = Color(0xFF0F3628)
            ),
            modifier = Modifier.testTag("nav_progress")
        )

        // 3. Plans
        NavigationBarItem(
            selected = currentRoute == Screen.Plans.route,
            onClick = { onNavigate(Screen.Plans) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Screen.Plans.route) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter,
                    contentDescription = "Plans",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Plans", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FitCyan,
                selectedTextColor = FitCyan,
                unselectedIconColor = FitTextSecondary,
                unselectedTextColor = FitTextSecondary,
                indicatorColor = Color(0xFF132A44)
            ),
            modifier = Modifier.testTag("nav_plans")
        )

        // 4. AI Coach
        NavigationBarItem(
            selected = currentRoute == Screen.AiCoach.route,
            onClick = { onNavigate(Screen.AiCoach) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Screen.AiCoach.route) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                    contentDescription = "AI Coach",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("AI Coach", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FitOrange,
                selectedTextColor = FitOrange,
                unselectedIconColor = FitTextSecondary,
                unselectedTextColor = FitTextSecondary,
                indicatorColor = Color(0xFF382310)
            ),
            modifier = Modifier.testTag("nav_ai_coach")
        )

        // 5. Profile
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Screen.Profile.route) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Profile", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FitCyan,
                selectedTextColor = FitCyan,
                unselectedIconColor = FitTextSecondary,
                unselectedTextColor = FitTextSecondary,
                indicatorColor = Color(0xFF132A44)
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}
