package app.cascade.android.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.cascade.android.CascadeApplication
import app.cascade.android.data.Task

class TodayWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TodayWidget.ACTION_TOGGLE) {
            super.onReceive(context, intent)
            return
        }
        val id = intent.data?.lastPathSegment ?: return
        val app = context.applicationContext as? CascadeApplication ?: return
        val pending = goAsync()
        app.whenReady {
            val node = app.store.get(id)
            if (node?.task != null) app.store.setTask(id, Task(done = !node.task.done))
            pending.finish()
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = refresh(context)

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) =
        refresh(context)

    private fun refresh(context: Context) {
        val app = context.applicationContext as? CascadeApplication ?: return
        app.whenReady { TodayWidget.updateAll(context, app.store) }
    }
}
