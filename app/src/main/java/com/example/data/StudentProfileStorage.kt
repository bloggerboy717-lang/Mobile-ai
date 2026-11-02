package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class StudentProfile(
    val name: String = "",
    val gradeOrCourse: String = "",
    val goal: String = "",
    val weakSubject: String = "",
    val isConfigured: Boolean = false
)

class StudentProfileStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alic_student_profile_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NAME = "student_name"
        private const val KEY_GRADE = "student_grade"
        private const val KEY_GOAL = "student_goal"
        private const val KEY_WEAK_SUBJECT = "student_weak_subject"
        private const val KEY_CONFIGURED = "student_configured"
    }

    fun getProfile(): StudentProfile {
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val grade = prefs.getString(KEY_GRADE, "") ?: ""
        val goal = prefs.getString(KEY_GOAL, "") ?: ""
        val weakSubject = prefs.getString(KEY_WEAK_SUBJECT, "") ?: ""
        val configured = prefs.getBoolean(KEY_CONFIGURED, name.isNotBlank())
        return StudentProfile(
            name = name,
            gradeOrCourse = grade,
            goal = goal,
            weakSubject = weakSubject,
            isConfigured = configured
        )
    }

    fun saveProfile(profile: StudentProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name.trim())
            .putString(KEY_GRADE, profile.gradeOrCourse.trim())
            .putString(KEY_GOAL, profile.goal.trim())
            .putString(KEY_WEAK_SUBJECT, profile.weakSubject.trim())
            .putBoolean(KEY_CONFIGURED, profile.name.isNotBlank())
            .apply()
    }
}
