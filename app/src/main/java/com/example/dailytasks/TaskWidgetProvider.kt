package com.example.dailytasks

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) {
        TaskStore.resetIfNewDay(c)
        ids.forEach { m.updateAppWidget(it, build(c)) }
        ids.forEach { m.notifyAppWidgetViewDataChanged(it, R.id.task_list) }
    }

    override fun onReceive(c: Context, i: Intent) {
        super.onReceive(c, i)
        val id = i.getLongExtra("task_id", -1L)
        if (id == -1L) return
        when (i.getStringExtra("op")) {
            "toggle" -> {
                val l = TaskStore.load(c)
                l.find { it.id == id }?.let { it.done = !it.done }
                TaskStore.save(c, l)
                refresh(c)
            }
            "delete" -> {
                val l = TaskStore.load(c)
                l.removeAll { it.id == id }
                TaskStore.save(c, l)
                refresh(c)
            }
            "edit" -> c.startActivity(
                Intent(c, EditTaskActivity::class.java)
                    .putExtra("task_id", id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    companion object {
        fun refresh(c: Context) {
            val m = AppWidgetManager.getInstance(c)
            val ids = m.getAppWidgetIds(ComponentName(c, TaskWidgetProvider::class.java))
            ids.forEach { m.updateAppWidget(it, build(c)) }
            ids.forEach { m.notifyAppWidgetViewDataChanged(it, R.id.task_list) }
        }

        fun build(c: Context): RemoteViews {
            TaskStore.resetIfNewDay(c)
            val v = RemoteViews(c.packageName, R.layout.widget_tasks)

            val svc = Intent(c, TaskWidgetService::class.java)
            svc.data = Uri.parse(svc.toUri(Intent.URI_INTENT_SCHEME))
            v.setRemoteAdapter(R.id.task_list, svc)
            v.setEmptyView(R.id.task_list, R.id.empty_view)

            v.setPendingIntentTemplate(
                R.id.task_list,
                PendingIntent.getBroadcast(
                    c, 0,
                    Intent(c, TaskWidgetProvider::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            )

            v.setOnClickPendingIntent(
                R.id.btn_add,
                PendingIntent.getActivity(
                    c, 1,
                    Intent(c, EditTaskActivity::class.java).putExtra("task_id", -1L),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            return v
        }
    }
}
