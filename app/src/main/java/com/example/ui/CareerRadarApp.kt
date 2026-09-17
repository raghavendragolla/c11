package com.example.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notifications.PushNotificationHelper
import com.example.ui.components.RadarPulseBlip
import com.example.ui.components.ServerConfigDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JobDetailScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SavedJobsScreen
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.RadarMint
import com.example.ui.theme.RadarTeal
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.JobViewModel

enum class RadarDestination {
    RADAR_JOBS,
    SAVED_JOBS,
    NOTIFICATIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerRadarApp(
    initialJobId: String? = null,
    jobViewModel: JobViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val serverUrl by authViewModel.serverUrl.collectAsState()
    val isMockFallback by authViewModel.isMockFallback.collectAsState()
    val savedJobs by jobViewModel.savedJobs.collectAsState()
    val notifications by jobViewModel.notifications.collectAsState()

    var currentDestination by remember { mutableStateOf(RadarDestination.RADAR_JOBS) }
    var activeJobDetailId by remember { mutableStateOf<String?>(initialJobId) }
    var showServerConfig by remember { mutableStateOf(false) }

    // Android 13+ Notification Permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        PushNotificationHelper.createNotificationChannels(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PushNotificationHelper.isNotificationPermissionGranted(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(initialJobId) {
        if (!initialJobId.isNullOrBlank()) {
            activeJobDetailId = initialJobId
        }
    }

    if (!isLoggedIn) {
        LoginScreen(authViewModel = authViewModel)
        return
    }

    BackHandler(enabled = activeJobDetailId != null) {
        activeJobDetailId = null
        jobViewModel.clearSelectedJob()
    }

    BackHandler(enabled = activeJobDetailId == null && currentDestination != RadarDestination.RADAR_JOBS) {
        currentDestination = RadarDestination.RADAR_JOBS
    }

    if (activeJobDetailId != null) {
        JobDetailScreen(
            jobId = activeJobDetailId!!,
            jobViewModel = jobViewModel,
            onBack = {
                activeJobDetailId = null
                jobViewModel.clearSelectedJob()
            }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadarPulseBlip(sizeDp = 26, color = RadarMint)
                        Text(
                            text = "Career Radar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Server Config button
                    IconButton(
                        onClick = { showServerConfig = true },
                        modifier = Modifier.testTag("topbar_server_btn")
                    ) {
                        Icon(
                            Icons.Default.Dns,
                            contentDescription = "FastAPI Server Settings",
                            tint = RadarTeal
                        )
                    }

                    // Logout button
                    IconButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier.testTag("topbar_logout_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentDestination == RadarDestination.RADAR_JOBS,
                    onClick = { currentDestination = RadarDestination.RADAR_JOBS },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "Radar Feed") },
                    label = { Text("Radar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RadarTeal,
                        indicatorColor = RadarTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_radar")
                )

                NavigationBarItem(
                    selected = currentDestination == RadarDestination.SAVED_JOBS,
                    onClick = { currentDestination = RadarDestination.SAVED_JOBS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (savedJobs.isNotEmpty()) {
                                    Badge(containerColor = RadarMint) {
                                        Text("${savedJobs.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Saved Jobs")
                        }
                    },
                    label = { Text("Saved (${savedJobs.size})") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RadarTeal,
                        indicatorColor = RadarTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_saved")
                )

                NavigationBarItem(
                    selected = currentDestination == RadarDestination.NOTIFICATIONS,
                    onClick = { currentDestination = RadarDestination.NOTIFICATIONS },
                    icon = {
                        BadgedBox(
                            badge = {
                                val unread = notifications.count { !it.isRead }
                                if (unread > 0) {
                                    Badge(containerColor = RadarCyan) {
                                        Text("$unread")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Radar Alerts")
                        }
                    },
                    label = { Text("Alerts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RadarTeal,
                        indicatorColor = RadarTeal.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_alerts")
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            modifier = Modifier.padding(innerPadding),
            label = "nav_crossfade"
        ) { destination ->
            when (destination) {
                RadarDestination.RADAR_JOBS -> {
                    HomeScreen(
                        jobViewModel = jobViewModel,
                        onJobClick = { jobId ->
                            activeJobDetailId = jobId
                        }
                    )
                }
                RadarDestination.SAVED_JOBS -> {
                    SavedJobsScreen(
                        jobViewModel = jobViewModel,
                        onJobClick = { jobId ->
                            activeJobDetailId = jobId
                        }
                    )
                }
                RadarDestination.NOTIFICATIONS -> {
                    NotificationsScreen(
                        jobViewModel = jobViewModel,
                        onJobClick = { jobId ->
                            activeJobDetailId = jobId
                        }
                    )
                }
            }
        }
    }

    if (showServerConfig) {
        ServerConfigDialog(
            currentUrl = serverUrl,
            isMockEnabled = isMockFallback,
            onSave = { newUrl, mockEnabled ->
                authViewModel.updateServerUrl(newUrl)
                authViewModel.setMockFallback(mockEnabled)
                jobViewModel.loadJobs()
            },
            onDismiss = { showServerConfig = false }
        )
    }
}
