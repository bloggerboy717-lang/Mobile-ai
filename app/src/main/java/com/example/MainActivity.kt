package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ApiKeySetupDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ErrorRed
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
    val hasExplicitGenderVoices by viewModel.hasExplicitGenderVoices.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val recognizedText by viewModel.recognizedLiveText.collectAsState()
    val audioRms by viewModel.activeAudioRms.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showSetupDialog by viewModel.showSetupDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    // Handle Android hardware/predictive back gesture when in secondary tabs
    BackHandler(enabled = currentTab != 0) {
        viewModel.switchTab(0)
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
            AlicBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    // Stop TTS or reset listening if changing tabs
                    if (tab != currentTab) {
                        viewModel.stopSpeaking()
                    }
                    viewModel.switchTab(tab)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                        onNavigateToSettings = { viewModel.switchTab(2) }
                    )
                    1 -> ChatScreen(
                        viewModel = viewModel,
                        chatMessages = chatMessages,
                        assistantState = assistantState
                    )
                    2 -> SettingsScreen(
                        viewModel = viewModel,
                        hasConfiguredKey = hasConfiguredKey,
                        maskedApiKey = maskedApiKey,
                        connectionStatus = connectionStatus,
                        selectedModel = selectedModel,
                        voiceSettings = voiceSettings,
                        hasExplicitGenderVoices = hasExplicitGenderVoices
                    )
                }
            }

            // First-time or requested API Key Setup Dialog
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
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("alic_bottom_navigation")
    ) {
        NavigationBarItem(
            selected = currentTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 0) Icons.Filled.Mic else Icons.Outlined.MicNone,
                    contentDescription = "Voice Assistant Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Assistant", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = CyberSky,
                indicatorColor = CyberSky,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        NavigationBarItem(
            selected = currentTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 1) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Chat Conversation",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Chat", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = NeonPurple,
                indicatorColor = NeonPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_chat")
        )

        NavigationBarItem(
            selected = currentTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (currentTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpaceDark,
                selectedTextColor = CyberSky,
                indicatorColor = CyberSky,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}
