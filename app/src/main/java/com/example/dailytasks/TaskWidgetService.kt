package com.example.dailytasks

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(applicationContext)
}

class Factory(private val c: Context) : RemoteViewsService.RemoteViewsFactory {
    private var list = mutableListOf<Task>()

    override fun onCreate() {}
    override fun onDataSetChanged() {
        TaskStore.resetIfNewDay(c)
        list = TaskStore.load(c)
    }
    override fun onDestroy() { list.clear() }
    override fun getCount() = list.size
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = list[position].id
    override fun hasStableIds() = true

    override fun getViewAt(position: Int): RemoteViews {
        val t = list[position]
        val v = RemoteViews(c.packageName, R.layout.widget_task_item)
        v.setTextViewText(R.id.item_text, t.text)
        v.setTextViewText(R.id.item_check, if (t.done) "✓" else "○")
        if (t.note.isBlank()) {
            v.setViewVisibility(R.id.item_note, View.GONE)
        } else {
            v.setViewVisibility(R.id.item_note, View.VISIBLE)
            v.setTextViewText(R.id.item_note, t.note)
        }

        v.setOnClickFillInIntent(
            R.id.item_check,
            Intent().putExtra("task_id", t.id).putExtra("op", "toggle")
        )
        v.setOnClickFillInIntent(
            R.id.item_body,
            Intent().putExtra("task_id", t.id).putExtra("op", "edit")
        )
        v.setOnClickFillInIntent(
            R.id.item_delete,
            Intent().putExtra("task_id", t.id).putExtra("op", "delete")
        )
        return v
    }
    override fun getLoadingView(): RemoteViews? {
    return null
    }
}
