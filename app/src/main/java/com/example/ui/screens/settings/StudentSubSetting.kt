package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.AlicViewModel
import com.example.data.StudentProfile
import com.example.ui.components.OutlinedAnimatedButton
import com.example.ui.theme.ThemeColors

@Composable
fun StudentSubSetting(
    viewModel: AlicViewModel,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val studentProfile by viewModel.studentProfile.collectAsState()

    var studentName by remember(studentProfile) { mutableStateOf(studentProfile.name) }
    var studentGrade by remember(studentProfile) { mutableStateOf(studentProfile.gradeOrCourse) }
    var studentGoal by remember(studentProfile) { mutableStateOf(studentProfile.goal) }
    var studentWeakSubject by remember(studentProfile) { mutableStateOf(studentProfile.weakSubject) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explanatory Banner (100% English)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.secondaryAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = themeColors.secondaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Student Profile & Personal Memory",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ALIC-AI remembers your name, course, targets, and weak topics to provide tailored study plans and advice.",
                        fontSize = 11.sp,
                        color = themeColors.textSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Student Form Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Your Name", color = themeColors.textSecondary) },
                    placeholder = { Text("e.g. Rohit", color = themeColors.textMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input"),
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
                    value = studentGrade,
                    onValueChange = { studentGrade = it },
                    label = { Text("Class / Course", color = themeColors.textSecondary) },
                    placeholder = { Text("e.g. 12th Grade / B.Tech CSE / NEET", color = themeColors.textMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_grade_input"),
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
                    value = studentGoal,
                    onValueChange = { studentGoal = it },
                    label = { Text("Study Goal / Target Exam", color = themeColors.textSecondary) },
                    placeholder = { Text("e.g. JEE 2026 / Top 1% / 95% Score", color = themeColors.textMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_goal_input"),
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
                    value = studentWeakSubject,
                    onValueChange = { studentWeakSubject = it },
                    label = { Text("Focus Area / Weak Subject", color = themeColors.textSecondary) },
                    placeholder = { Text("e.g. Physics Numericals / Algorithms", color = themeColors.textMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_weak_subject_input"),
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

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedAnimatedButton(
                    text = "Save Student Profile",
                    icon = Icons.Default.Save,
                    onClick = {
                        viewModel.updateStudentProfile(
                            StudentProfile(
                                name = studentName,
                                gradeOrCourse = studentGrade,
                                goal = studentGoal,
                                weakSubject = studentWeakSubject
                            )
                        )
                        Toast.makeText(context, "Student profile and memory saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_student_profile_button")
                )
            }
        }

        // Memory State Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardElevated)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = themeColors.secondaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Memory State",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (studentProfile.name.isNotBlank()) {
                        "ALIC-AI addresses you as \"${studentProfile.name}\". Target: \"${studentProfile.goal}\". Focus area: \"${studentProfile.weakSubject}\"."
                    } else {
                        "No student profile saved yet. Enter details above so ALIC-AI can personalize tutor and study advice."
                    },
                    fontSize = 11.sp,
                    color = themeColors.textSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
