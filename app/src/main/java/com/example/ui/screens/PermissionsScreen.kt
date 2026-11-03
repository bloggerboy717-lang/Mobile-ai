package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.AlicAccessibilityService
import com.example.service.VoiceAssistantForegroundService
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeColors

/**
 * Unified Permissions Center:
 * Houses ALL working permissions and background running switches in one single place.
 * - Voice & Microphone (RECORD_AUDIO)
 * - Background Assistant Service (FOREGROUND_SERVICE + MICROPHONE)
 * - Push Notifications (POST_NOTIFICATIONS)
 * - Battery Optimization Exemption (Background survival)
 * - Emergency Location (ACCESS_FINE_LOCATION)
 * - Camera Vision (CAMERA)
 * - Hands-Free Accessibility Service
 */
@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit,
    themeColors: ThemeColors = AppTheme.colors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }

    // Live permission states
    var micGranted by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    var batteryOptimizationIgnored by remember { mutableStateOf(false) }
    var isBackgroundServiceRunning by remember { mutableStateOf(false) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }

    // Evaluate live status on composition and whenever refreshKey triggers
    LaunchedEffect(refreshKey) {
        micGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        cameraGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        batteryOptimizationIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true

        isBackgroundServiceRunning = VoiceAssistantForegroundService.isRunning
        isAccessibilityEnabled = AlicAccessibilityService.isServiceEnabled
    }

    // Working ActivityResult Launchers
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshKey++ }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshKey++ }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshKey++ }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshKey++ }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // TOP HEADER
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = themeColors.cardBackground,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.cardElevated)
                        .border(1.dp, themeColors.cardBorder, CircleShape)
                        .testTag("permissions_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Permissions Center",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    Text(
                        text = "Unified Voice, Background & Sensor Access",
                        fontSize = 11.sp,
                        color = themeColors.textSecondary
                    )
                }
            }
        }

        // SCROLLABLE LIST OF PERMISSIONS
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = themeColors.cardBackground,
                border = BorderStroke(1.dp, themeColors.cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(themeColors.primaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = themeColors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Grant permissions below to enable continuous voice listening, background execution, and device control.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = themeColors.textSecondary
                    )
                }
            }

            // 1. VOICE & MICROPHONE (MANDATORY FOR VOICE ASSISTANT)
            PermissionItemCard(
                icon = Icons.Default.Mic,
                title = "Voice & Microphone",
                description = "Enables real-time speech recognition, hands-free conversation, and silent voice capture.",
                isGranted = micGranted,
                actionText = if (micGranted) "Granted" else "Allow Microphone",
                onAction = {
                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                themeColors = themeColors,
                testTag = "perm_microphone_button"
            )

            // 2. BACKGROUND VOICE ASSISTANT SERVICE
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = themeColors.cardBackground,
                border = BorderStroke(
                    1.dp,
                    if (isBackgroundServiceRunning) themeColors.primaryAccent.copy(alpha = 0.6f) else themeColors.cardBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.primaryAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBackgroundServiceRunning) Icons.Default.PlayArrow else Icons.Default.Stop,
                                    contentDescription = null,
                                    tint = if (isBackgroundServiceRunning) Color(0xFF10B981) else themeColors.primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Background Voice Service",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themeColors.textPrimary
                                )
                                Text(
                                    text = if (isBackgroundServiceRunning) "Running in Background" else "Stopped",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isBackgroundServiceRunning) Color(0xFF10B981) else themeColors.textMuted
                                )
                            }
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isBackgroundServiceRunning) Color(0x3310B981) else themeColors.cardElevated
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isBackgroundServiceRunning) "ACTIVE" else "IDLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBackgroundServiceRunning) Color(0xFF10B981) else themeColors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Allows ALIC-AI to continuously listen and assist you even when phone is locked or you are using other apps.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = themeColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                VoiceAssistantForegroundService.startService(context)
                                refreshKey++
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_background_service_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.primaryAccent
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                VoiceAssistantForegroundService.stopService(context)
                                refreshKey++
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stop_background_service_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, themeColors.cardBorder)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Service", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. NOTIFICATIONS
            PermissionItemCard(
                icon = Icons.Default.Notifications,
                title = "Assistant Notifications",
                description = "Required to show persistent active status and quick access controls in your notification bar.",
                isGranted = notificationGranted,
                actionText = if (notificationGranted) "Granted" else "Allow Notifications",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                themeColors = themeColors,
                testTag = "perm_notifications_button"
            )

            // 4. BATTERY OPTIMIZATION EXEMPTION
            PermissionItemCard(
                icon = Icons.Default.BatteryAlert,
                title = "Background Battery Optimization",
                description = "Prevents Android OS from pausing or terminating ALIC-AI in the background when your screen is turned off.",
                isGranted = batteryOptimizationIgnored,
                actionText = if (batteryOptimizationIgnored) "Unrestricted" else "Exempt from Sleep",
                onAction = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(fallback)
                    }
                },
                themeColors = themeColors,
                testTag = "perm_battery_optimization_button"
            )

            // 5. LOCATION (FOR SOS AND REAL-TIME MAPS)
            PermissionItemCard(
                icon = Icons.Default.MyLocation,
                title = "Emergency Location",
                description = "Required for sending live GPS coordinates during Emergency SOS and finding nearby places.",
                isGranted = locationGranted,
                actionText = if (locationGranted) "Granted" else "Allow Location",
                onAction = {
                    locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                themeColors = themeColors,
                testTag = "perm_location_button"
            )

            // 6. CAMERA (FOR HOMEWORK DOUBTS & VISION)
            PermissionItemCard(
                icon = Icons.Default.CameraAlt,
                title = "Camera Vision",
                description = "Allows ALIC-AI to scan homework, read textbook problems, and analyze diagrams.",
                isGranted = cameraGranted,
                actionText = if (cameraGranted) "Granted" else "Allow Camera",
                onAction = {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                },
                themeColors = themeColors,
                testTag = "perm_camera_button"
            )

            // 7. ACCESSIBILITY SERVICE (HANDS-FREE APP CONTROL)
            PermissionItemCard(
                icon = Icons.Default.Accessibility,
                title = "Hands-Free Accessibility",
                description = "Enables ALIC-AI to navigate apps, tap video results, and interact on your behalf hands-free.",
                isGranted = isAccessibilityEnabled,
                actionText = if (isAccessibilityEnabled) "Enabled" else "Open Accessibility",
                onAction = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                themeColors = themeColors,
                testTag = "perm_accessibility_button"
            )

            // 8. ALL APP SYSTEM SETTINGS BUTTON
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_system_app_settings_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, themeColors.cardBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Android System App Settings",
                    fontSize = 12.sp,
                    color = themeColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    actionText: String,
    onAction: () -> Unit,
    themeColors: ThemeColors,
    testTag: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = themeColors.cardBackground,
        border = BorderStroke(
            1.dp,
            if (isGranted) Color(0x3310B981) else themeColors.cardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGranted) Color(0x2210B981) else themeColors.primaryAccent.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF10B981) else themeColors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = if (isGranted) "Permission Granted" else "Action Required",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isGranted) Color(0x3310B981) else Color(0x26F59E0B)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "GRANTED" else "PENDING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = themeColors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!isGranted) {
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primaryAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = actionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (themeColors.isDark) Color(0xFF0F172A) else Color.White
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0x4D10B981))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Access Active (Tap to recheck)",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}
