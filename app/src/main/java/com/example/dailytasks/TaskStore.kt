package com.example.dailytasks

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class Task(
    var id: Long,
    var text: String,
    var note: String = "",
    var done: Boolean = false
)

object TaskStore {
    private const val PREF = "daily_tasks"
    private fun p(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun load(c: Context): MutableList<Task> {
        val raw = p(c).getString("tasks", null) ?: return mutableListOf()
        val arr = JSONArray(raw)
        val out = mutableListOf<Task>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Task(
                    o.getLong("id"),
                    o.getString("text"),
                    o.optString("note"),
                    o.optBoolean("done")
                )
            )
        }
        return out
    }

    fun save(c: Context, list: List<Task>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("text", it.text)
                put("note", it.note)
                put("done", it.done)
            })
        }
        p(c).edit().putString("tasks", arr.toString()).apply()
    }

    /** 跨天自动取消所有勾选 */
    fun resetIfNewDay(c: Context) {
        val cal = Calendar.getInstance()
        val today = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
        if (p(c).getString("last", "") != today) {
            val l = load(c)
            l.forEach { it.done = false }
            save(c, l)
            p(c).edit().putString("last", today).apply()
        }
    }
}
