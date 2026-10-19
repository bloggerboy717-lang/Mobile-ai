package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.agent.ActionType
import com.example.agent.AgentAction
import com.example.service.AlicAccessibilityService
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevatedDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppControlScreen(
    viewModel: AlicViewModel,
    modifier: Modifier = Modifier
) {
    val isAccessibilityActive = AlicAccessibilityService.isServiceEnabled

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
                    text = "App & Agent Control",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Voice commands, automated YouTube playback & app controls",
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
            // Accessibility Service Status Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.openAccessibilitySettings() },
                color = if (isAccessibilityActive) Color(0xFF07241A) else Color(0xFF261233),
                border = BorderStroke(
                    1.dp,
                    if (isAccessibilityActive) SuccessGreen.copy(alpha = 0.5f) else NeonPurple.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAccessibilityActive) SuccessGreen.copy(alpha = 0.2f) else NeonPurple.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAccessibilityActive) Icons.Default.Check else Icons.Default.TouchApp,
                            contentDescription = "Accessibility",
                            tint = if (isAccessibilityActive) SuccessGreen else NeonPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAccessibilityActive) "एक्सेसिबिलिटी सक्रिय है (Accessibility ON)" else "एक्सेसिबिलिटी चालू करें (Enable Accessibility)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isAccessibilityActive)
                                "YouTube में बिना टच किए गाना ऑटो-प्ले और ऐप ऑटोमेशन तैयार है।"
                            else
                                "YouTube में अपने आप गाना प्ले करने के लिए सेटिंग्स में Alic AI को ऑन करें।",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAccessibilityActive) SuccessGreen else NeonPurple)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isAccessibilityActive) "सक्रिय" else "चालू करें",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Featured YouTube Auto-Play Card (as requested by user)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = CardDark,
                border = BorderStroke(1.dp, CyberSky.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF0000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "YOUTUBE AUTO-PLAY TEST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberSky, letterSpacing = 1.sp)
                            Text(text = "यो यो हनी सिंह का पहला गाना प्ले करो", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "माइक से बोलें: \"YouTube ओपन करके यो यो हनी सिंह का पहला गाना प्ले करो\" या नीचे दिए बटन पर क्लिक करें।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))
                            )
                            .clickable {
                                viewModel.executeAgentAction(
                                    AgentAction(
                                        type = ActionType.SEARCH_YOUTUBE,
                                        title = "Play Honey Singh Song",
                                        description = "Playing Honey Singh's first song on YouTube",
                                        params = mapOf("query" to "Yo Yo Honey Singh", "autoPlay" to "true")
                                    )
                                )
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "पहला गाना ऑटो-प्ले टेस्ट करें", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Category 1: Applications
            ActionCategoryCard(title = "Open Applications", accent = CyberSky) {
                AppQuickActionRow(
                    name = "YouTube",
                    subtitle = "Open YouTube & search videos",
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
                    subtitle = "Navigate to destinations",
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
                    subtitle = "Launch Camera to snap photos",
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
                    subtitle = "\"टॉर्च चालू करो\" या \"टॉर्च बंद करो\"",
                    icon = Icons.Default.FlashlightOn,
                    iconTint = Color(0xFFFFD600),
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.TOGGLE_FLASHLIGHT, title = "Toggle Flashlight", description = "टॉर्च चालू/बंद कर रहा हूँ", params = mapOf("turnOn" to "true"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Volume Up / Down",
                    subtitle = "\"आवाज बढ़ाओ\" या \"वॉल्यूम कम करो\"",
                    icon = Icons.Default.VolumeUp,
                    iconTint = CyberSky,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.ADJUST_VOLUME, title = "Adjust Volume", description = "मीडिया वॉल्यूम एडजस्ट कर रहा हूँ", params = mapOf("direction" to "up"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "System Alarms & Clock",
                    subtitle = "\"अलार्म सेट करो\"",
                    icon = Icons.Default.Alarm,
                    iconTint = MagentaGlow,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.SET_ALARM, title = "Open Alarms", description = "अलार्म सेटिंग्स खोल रहा हूँ")
                        )
                    }
                )
            }

            // Category 3: System Settings
            ActionCategoryCard(title = "System Settings", accent = ElectricBlue) {
                AppQuickActionRow(
                    name = "Wi-Fi Settings",
                    subtitle = "वाई-फाई सेटिंग्स खोलें",
                    icon = Icons.Default.Wifi,
                    iconTint = CyberSky,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.OPEN_SETTINGS, title = "Wi-Fi Settings", description = "Wi-Fi सेटिंग्स खोल रहा हूँ", params = mapOf("setting" to "wifi"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Bluetooth Settings",
                    subtitle = "ब्लूटूथ सेटिंग्स खोलें",
                    icon = Icons.Default.Bluetooth,
                    iconTint = ElectricBlue,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.OPEN_SETTINGS, title = "Bluetooth Settings", description = "Bluetooth सेटिंग्स खोल रहा हूँ", params = mapOf("setting" to "bluetooth"))
                        )
                    }
                )
                AppQuickActionRow(
                    name = "Sound & Vibrations",
                    subtitle = "साउंड कॉन्फ़िगरेशन खोलें",
                    icon = Icons.Default.Settings,
                    iconTint = TextSecondary,
                    onClick = {
                        viewModel.executeAgentAction(
                            AgentAction(type = ActionType.OPEN_SETTINGS, title = "Sound Settings", description = "Sound सेटिंग्स खोल रहा हूँ", params = mapOf("setting" to "sound"))
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
        border = BorderStroke(1.dp, CardBorderDark)
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
            .padding(vertical = 5.dp)
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
