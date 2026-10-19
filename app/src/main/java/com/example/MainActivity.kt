package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ApiKeySetupDialog
import com.example.ui.screens.AppControlScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: AlicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AlicApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AlicApp(viewModel: AlicViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val assistantState by viewModel.assistantState.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val hasConfiguredKey by viewModel.hasConfiguredKey.collectAsState()
    val maskedApiKey by viewModel.maskedApiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val recognizedText by viewModel.recognizedLiveText.collectAsState()
    val audioRms by viewModel.activeAudioRms.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showSetupDialog by viewModel.showSetupDialog.collectAsState()

    var showPermissionsCenter by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    // Android back button handling
    BackHandler(enabled = showPermissionsCenter || currentTab != 0) {
        if (showPermissionsCenter) {
            showPermissionsCenter = false
        } else {
            viewModel.switchTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SpaceDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = CardDark,
                    contentColor = TextPrimary,
                    actionColor = CyberSky,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                )
            }
        },
        bottomBar = {
            if (!showPermissionsCenter) {
                AlicBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab != currentTab) {
                            viewModel.stopSpeaking()
                        }
                        viewModel.switchTab(tab)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showPermissionsCenter) {
                PermissionsScreen(onNavigateBack = { showPermissionsCenter = false })
            } else {
                Crossfade(targetState = currentTab, label = "ScreenCrossfade") { tab ->
                    when (tab) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            assistantState = assistantState,
                            recognizedText = recognizedText,
                            audioRms = audioRms,
                            connectionStatus = connectionStatus,
                            hasConfiguredKey = hasConfiguredKey,
                            chatMessages = chatMessages,
                            onNavigateToChat = { viewModel.switchTab(1) },
                            onNavigateToSettings = { viewModel.switchTab(4) }
                        )
                        1 -> ChatScreen(
                            viewModel = viewModel,
                            chatMessages = chatMessages,
                            assistantState = assistantState
                        )
                        2 -> VisionScreen(
                            viewModel = viewModel
                        )
                        3 -> AppControlScreen(
                            viewModel = viewModel
                        )
                        4 -> SettingsScreen(
                            viewModel = viewModel,
                            hasConfiguredKey = hasConfiguredKey,
                            maskedApiKey = maskedApiKey,
                            connectionStatus = connectionStatus,
                            selectedModel = selectedModel,
                            voiceSettings = voiceSettings,
                            onNavigateToPermissions = { showPermissionsCenter = true }
                        )
                    }
                }
            }

            if (showSetupDialog) {
                ApiKeySetupDialog(
                    connectionStatus = connectionStatus,
                    onSaveKey = { key ->
                        viewModel.saveApiKey(key, autoTest = true)
                    },
                    onTestConnection = {
                        viewModel.testCurrentConnection()
                    },
                    onDismiss = {
                        viewModel.dismissSetupDialog()
                    },
                    canDismiss = hasConfiguredKey
                )
            }
        }
    }
}

@Composable
fun AlicBottomNavigation(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = CardDark,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("alic_bottom_navigation")
    ) {
        // Tab 0: Home
        NavigationBarItem(
            selected = currentTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = CyberSky,
                indicatorColor = CyberSky,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        // Tab 1: Chat
        NavigationBarItem(
            selected = currentTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 1) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Chat",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Chat", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = NeonPurple,
                indicatorColor = NeonPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        // Tab 2: Vision
        NavigationBarItem(
            selected = currentTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 2) Icons.Filled.Visibility else Icons.Outlined.Visibility,
                    contentDescription = "Vision",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Vision", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = CyberSky,
                indicatorColor = CyberSky,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        // Tab 3: Apps / Control
        NavigationBarItem(
            selected = currentTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 3) Icons.Filled.GridView else Icons.Outlined.GridView,
                    contentDescription = "Apps",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Apps", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = CyberSky,
                indicatorColor = CyberSky,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        // Tab 4: Settings
        NavigationBarItem(
            selected = currentTab == 4,
            onClick = { onTabSelected(4) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 4) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Settings", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = NeonPurple,
                indicatorColor = NeonPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}
