package com.example.dailytasks

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout

class EditTaskActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra("task_id", -1L)
        val task = TaskStore.load(this).find { it.id == id }
        val pad = (20 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val etText = EditText(this).apply {
            hint = "要做的事"
            setText(task?.text ?: "")
            setSelection(text.length)
        }
        val etNote = EditText(this).apply {
            hint = "备注（可选）"
            setText(task?.note ?: "")
        }

        root.addView(etText, LinearLayout.LayoutParams(-1, -2))
        root.addView(etNote, LinearLayout.LayoutParams(-1, -2))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        row.addView(Button(this).apply {
            text = "取消"
            setOnClickListener { finish() }
        })

        row.addView(Button(this).apply {
            text = "保存"
            setOnClickListener {
                val t = etText.text.toString().trim()
                if (t.isNotEmpty()) {
                    val all = TaskStore.load(this@EditTaskActivity)
                    val found = all.find { it.id == id }
                    if (found != null) {
                        found.text = t
                        found.note = etNote.text.toString().trim()
                    } else {
                        all.add(
                            Task(
                                System.currentTimeMillis(),
                                t,
                                etNote.text.toString().trim()
                            )
                        )
                    }
                    TaskStore.save(this@EditTaskActivity, all)
                }
                TaskWidgetProvider.refresh(this@EditTaskActivity)
                finish()
            }
        })

        root.addView(row, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }
}
