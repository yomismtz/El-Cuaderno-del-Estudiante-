package com.cuadernoestudiante.app.data.repository

import android.content.Context
import com.cuadernoestudiante.app.network.StudentTeamActivityDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CachedStudentTeamChoice(
    val className: String,
    val activity: StudentTeamActivityDto,
)

class StudentTeamOfflineCache(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("student_team_offline", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): List<CachedStudentTeamChoice> {
        val raw = prefs.getString("choices", null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CachedStudentTeamChoice>>() {}.type
            gson.fromJson<List<CachedStudentTeamChoice>>(raw, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    fun save(items: List<CachedStudentTeamChoice>) {
        prefs.edit().putString("choices", gson.toJson(items)).apply()
    }
}
