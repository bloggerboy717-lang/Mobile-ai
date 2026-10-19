package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.agent.ActionType
import com.example.agent.AgentAction
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevatedDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppControlScreen(
    viewModel: AlicViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardDark,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "App Control",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Control your phone with voice commands & actions",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voice Command Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x2238BDF8))
                    .border(1.dp, CyberSky.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberSky),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = SpaceDark, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Try Saying:", fontSize = 11.sp, color = CyberSky, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(text = "\"Hey Alic, open YouTube\"", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            }

            // Category 1: Applications
            ActionCategoryCard(title = "Open Applications", accent = CyberSky) {
                AppQuickActionRow(
                    name = "YouTube",
                    subtitle = "e.g. Open YouTube or play music",
                    icon = Icons.Default.PlayArrow,
                    iconTint = Color(0xFFFF0000),
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.SEARCH_YOUTUBE, title = "Open YouTube", description = "Launch YouTube")
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Google Maps",
                    subtitle = "e.g. Navigate to destinations",
                    icon = Icons.Default.Map,
                    iconTint = Color(0xFF34A853),
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.NAVIGATE_MAPS, title = "Open Maps", description = "Launch Maps")
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Camera",
                    subtitle = "e.g. Open Camera to snap a photo",
                    icon = Icons.Default.CameraAlt,
                    iconTint = CyberSky,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.OPEN_APP, title = "Open Camera", description = "Launch Camera", params = mapOf("appName" to "camera"))
                        )
                    }
                )
            }

            // Category 2: Device Control & Hardware
            ActionCategoryCard(title = "Device & Hardware Controls", accent = NeonPurple) {
                AppQuickActionRow(
                    name = "Flashlight / Torch",
                    subtitle = "Toggle LED flash on/off",
                    icon = Icons.Default.FlashlightOn,
                    iconTint = Color(0xFFFFD600),
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.TOGGLE_FLASHLIGHT, title = "Toggle Flashlight", description = "Turn on device LED flash", params = mapOf("turnOn" to "true"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Volume Up / Down",
                    subtitle = "Adjust media & call audio",
                    icon = Icons.Default.VolumeUp,
                    iconTint = CyberSky,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.ADJUST_VOLUME, title = "Adjust Volume", description = "Increase system audio volume", params = mapOf("direction" to "up"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "System Alarms & Clock",
                    subtitle = "Set alarms or view timers",
                    icon = Icons.Default.Alarm,
                    iconTint = MagentaGlow,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.SET_ALARM, title = "Open Alarms", description = "Launch Clock")
                        )
                    }
                )
            }

            // Category 3: System Settings
            ActionCategoryCard(title = "System Settings", accent = ElectricBlue) {
                AppQuickActionRow(
                    name = "Wi-Fi Settings",
                    subtitle = "Open Wi-Fi connection manager",
                    icon = Icons.Default.Wifi,
                    iconTint = CyberSky,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.OPEN_SETTINGS, title = "Wi-Fi Settings", description = "Open Wi-Fi settings", params = mapOf("setting" to "wifi"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Bluetooth Settings",
                    subtitle = "Manage connected accessories",
                    icon = Icons.Default.Bluetooth,
                    iconTint = ElectricBlue,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.OPEN_SETTINGS, title = "Bluetooth Settings", description = "Open Bluetooth settings", params = mapOf("setting" to "bluetooth"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Sound & Vibrations",
                    subtitle = "Open sound configuration",
                    icon = Icons.Default.Settings,
                    iconTint = TextSecondary,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.OPEN_SETTINGS, title = "Sound Settings", description = "Open Sound settings", params = mapOf("setting" to "sound"))
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ActionCategoryCard(
    title: String,
    accent: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        color = CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun AppQuickActionRow(
    name: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardElevatedDark)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Launch",
            tint = CyberSky,
            modifier = Modifier.size(18.dp)
        )
    }
}
