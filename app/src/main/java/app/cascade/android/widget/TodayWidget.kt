package app.cascade.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import app.cascade.android.MainActivity
import app.cascade.android.R
import app.cascade.android.commands.nodeLink
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.dayId
import app.cascade.android.data.isoDay
import app.cascade.android.data.relativeDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface TodayItem {
    data class Line(val node: Node, val depth: Int, val from: String? = null) : TodayItem

    data class Section(val count: Int) : TodayItem

    data class More(val count: Int) : TodayItem
}

fun todayItems(store: OutlineStore, today: LocalDate, limit: Int): List<TodayItem> {
    val note = dayId(today)
    val all = ArrayList<TodayItem>()
    store.rows(note).mapTo(all) { TodayItem.Line(it.node, it.depth) }
    val due = store.dueOn(isoDay(today), excluding = note)
    if (due.isNotEmpty()) {
        all.add(TodayItem.Section(due.size))
        due.mapTo(all) { node ->
            val parent = node.parentId?.let(store::get)
            TodayItem.Line(node, 0, parent?.let { relativeDay(it.id, today) ?: it.text.ifBlank { "Untitled" } } ?: "Home")
        }
    }
    if (all.size <= limit) return all
    val kept = all.take((limit - 1).coerceAtLeast(0)).dropLastWhile { it is TodayItem.Section }
    val hidden = all.drop(kept.size).count { it is TodayItem.Line }
    return kept + TodayItem.More(hidden)
}

object TodayWidget {
    const val ACTION_TOGGLE = "app.cascade.android.widget.TOGGLE"
    private const val DEFAULT_ROWS = 4
    private const val CHROME_DP = 60
    private const val ROW_DP = 32
    private val DATE = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

    fun rowsFor(heightDp: Int): Int = if (heightDp <= 0) DEFAULT_ROWS else ((heightDp - CHROME_DP) / ROW_DP).coerceAtLeast(1)

    fun render(context: Context, store: OutlineStore, today: LocalDate, maxRows: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_today)
        val open = openNode(context, dayId(today))
        views.setTextViewText(R.id.widget_date, today.format(DATE))
        views.setOnClickPendingIntent(R.id.widget_header, open)
        views.setOnClickPendingIntent(R.id.widget_add, open)
        views.setOnClickPendingIntent(R.id.widget_empty, open)
        views.removeAllViews(R.id.widget_rows)

        val items = todayItems(store, today, maxRows)
        views.setViewVisibility(R.id.widget_empty, if (items.isEmpty()) View.VISIBLE else View.GONE)
        for (item in items) {
            views.addView(
                R.id.widget_rows,
                when (item) {
                    is TodayItem.Line -> line(context, item)
                    is TodayItem.Section -> section(context, "${context.getString(R.string.widget_due_elsewhere)}  ${item.count}")
                    is TodayItem.More -> section(context, context.getString(R.string.widget_more, item.count)).apply {
                        setOnClickPendingIntent(R.id.widget_section, open)
                    }
                },
            )
        }
        return views
    }

    fun updateAll(context: Context, store: OutlineStore, today: LocalDate = LocalDate.now()) {
        val manager = AppWidgetManager.getInstance(context) ?: return
        val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
        for (id in ids) {
            val height = manager.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            manager.updateAppWidget(id, render(context, store, today, rowsFor(height)))
        }
    }

    private fun line(context: Context, item: TodayItem.Line): RemoteViews {
        val node = item.node
        val done = node.task?.done == true
        val views = RemoteViews(context.packageName, R.layout.widget_row)
        val indent = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            14f * item.depth.coerceAtMost(4),
            context.resources.displayMetrics,
        ).toInt()
        views.setViewPadding(R.id.widget_row, indent, 0, 0, 0)
        views.setTextViewText(R.id.widget_row_text, node.text.replace('\n', ' ').ifBlank { "Untitled" })
        views.setTextColor(
            R.id.widget_row_text,
            context.getColor(if (done) R.color.widget_muted else R.color.widget_ink),
        )
        views.setInt(
            R.id.widget_row_text,
            "setPaintFlags",
            Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or if (done) Paint.STRIKE_THRU_TEXT_FLAG else 0,
        )
        views.setImageViewResource(
            R.id.widget_row_marker,
            when (node.task?.done) {
                null -> R.drawable.widget_bullet
                true -> R.drawable.widget_task_done
                false -> R.drawable.widget_task_todo
            },
        )
        if (item.from == null) {
            views.setViewVisibility(R.id.widget_row_from, View.GONE)
        } else {
            views.setTextViewText(R.id.widget_row_from, item.from)
        }
        views.setOnClickPendingIntent(R.id.widget_row_text, openNode(context, node.id))
        if (node.task != null) {
            views.setContentDescription(
                R.id.widget_row_marker,
                context.getString(if (done) R.string.widget_mark_not_done else R.string.widget_mark_done),
            )
            views.setOnClickPendingIntent(R.id.widget_row_marker, toggle(context, node.id))
        } else {
            views.setOnClickPendingIntent(R.id.widget_row_marker, openNode(context, node.id))
        }
        return views
    }

    private fun section(context: Context, text: String): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_section).apply { setTextViewText(R.id.widget_section, text) }

    private fun openNode(context: Context, id: String): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(Intent.ACTION_VIEW, Uri.parse(nodeLink(id)), context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun toggle(context: Context, id: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(ACTION_TOGGLE, Uri.parse("cascade://toggle/$id"), context, TodayWidgetProvider::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
