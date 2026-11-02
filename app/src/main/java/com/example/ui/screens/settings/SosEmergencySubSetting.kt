package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.ui.components.OutlinedAnimatedButton
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ThemeColors

@Composable
fun SosEmergencySubSetting(
    viewModel: AlicViewModel,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialContacts = remember { viewModel.emergencyStorage.getContacts() }

    var contact1Name by remember { mutableStateOf(initialContacts.getOrNull(0)?.name ?: "Emergency Contact 1") }
    var contact1Phone by remember { mutableStateOf(initialContacts.getOrNull(0)?.phoneNumber ?: "") }

    var contact2Name by remember { mutableStateOf(initialContacts.getOrNull(1)?.name ?: "Emergency Contact 2") }
    var contact2Phone by remember { mutableStateOf(initialContacts.getOrNull(1)?.phoneNumber ?: "") }

    var contact3Name by remember { mutableStateOf(initialContacts.getOrNull(2)?.name ?: "Emergency Contact 3") }
    var contact3Phone by remember { mutableStateOf(initialContacts.getOrNull(2)?.phoneNumber ?: "") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Trigger Info Banner (100% English)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            color = ErrorRed.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Emergency SOS Trigger Rules:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Saying or typing the distress call 3 consecutive times immediately activates emergency mode.\n" +
                            "• An SMS with live location coordinates is dispatched to your 3 saved emergency contacts.\n" +
                            "• Emergency mode remains active until you confirm you are safe.",
                    fontSize = 11.sp,
                    color = themeColors.textPrimary,
                    lineHeight = 16.sp
                )
            }
        }

        // 3 Contacts Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Contact 1
                Text(
                    text = "Emergency Contact 1",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = contact1Name,
                        onValueChange = { contact1Name = it },
                        label = { Text("Name", color = themeColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("contact_1_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primaryAccent,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardElevated,
                            unfocusedContainerColor = themeColors.cardElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = contact1Phone,
                        onValueChange = { contact1Phone = it },
                        label = { Text("Phone Number", color = themeColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("contact_1_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primaryAccent,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardElevated,
                            unfocusedContainerColor = themeColors.cardElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Contact 2
                Text(
                    text = "Emergency Contact 2",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.secondaryAccent
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = contact2Name,
                        onValueChange = { contact2Name = it },
                        label = { Text("Name", color = themeColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("contact_2_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.secondaryAccent,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardElevated,
                            unfocusedContainerColor = themeColors.cardElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = contact2Phone,
                        onValueChange = { contact2Phone = it },
                        label = { Text("Phone Number", color = themeColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("contact_2_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.secondaryAccent,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardElevated,
                            unfocusedContainerColor = themeColors.cardElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Contact 3
                Text(
                    text = "Emergency Contact 3",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = contact3Name,
                        onValueChange = { contact3Name = it },
                        label = { Text("Name", color = themeColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("contact_3_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardElevated,
                            unfocusedContainerColor = themeColors.cardElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = contact3Phone,
                        onValueChange = { contact3Phone = it },
                        label = { Text("Phone Number", color = themeColors.textSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("contact_3_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardElevated,
                            unfocusedContainerColor = themeColors.cardElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedAnimatedButton(
                        text = "Save Contacts",
                        icon = Icons.Default.Save,
                        onClick = {
                            viewModel.emergencyStorage.saveContact(1, contact1Name, contact1Phone)
                            viewModel.emergencyStorage.saveContact(2, contact2Name, contact2Phone)
                            viewModel.emergencyStorage.saveContact(3, contact3Name, contact3Phone)
                            Toast.makeText(context, "3 Emergency Contacts saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_emergency_contacts_button")
                    )

                    OutlinedAnimatedButton(
                        text = "Dial 112",
                        icon = Icons.Default.Call,
                        onClick = { viewModel.dialPolice112() },
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("dial_112_button")
                    )
                }
            }
        }
    }
}
