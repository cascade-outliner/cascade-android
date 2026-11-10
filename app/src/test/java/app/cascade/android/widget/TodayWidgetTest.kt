package app.cascade.android.widget

import android.app.Application
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cascade.android.R
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Task
import app.cascade.android.data.dayId
import app.cascade.android.data.isoDay
import com.github.takahirom.roborazzi.captureRoboImage
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
class TodayWidgetTest {
    private val store = OutlineStore().apply { loaded(emptyList()) }
    private val today = LocalDate.of(2026, 11, 10)

    private fun seed() {
        val note = dayId(today)
        val standup = store.create(note, text = "Standup notes")
        store.create(standup, text = "Ship the Android widget")
        val call = store.create(note, text = "Call the venue back")
        store.setTask(call, Task(done = false))
        val invoice = store.create(note, text = "Send the invoice")
        store.setTask(invoice, Task(done = true))
        store.create(note, text = "Idea: weekly review template")
        val launch = store.create(text = "Launch prep")
        val press = store.create(launch, text = "Email the press list")
        store.setTask(press, Task(done = false))
        store.setDue(press, isoDay(today))
        val later = store.create(launch, text = "Book the venue")
        store.setDue(later, isoDay(today.plusDays(3)))
    }

    private fun labels(limit: Int) = todayItems(store, today, limit).map {
        when (it) {
            is TodayItem.Line -> " ".repeat(it.depth) + it.node.text + (it.from?.let { from -> " <$from>" } ?: "")
            is TodayItem.Section -> "# ${it.count}"
            is TodayItem.More -> "+${it.count}"
        }
    }

    @Test
    fun listsTodaysNoteThenWhatIsDueElsewhere() {
        seed()
        assertEquals(
            listOf(
                "Standup notes", " Ship the Android widget", "Call the venue back", "Send the invoice",
                "Idea: weekly review template", "# 1", "Email the press list <Launch prep>",
            ),
            labels(10),
        )
    }

    @Test
    fun cutsToTheRowsThatFitAndCountsTheRest() {
        seed()
        assertEquals(listOf("Standup notes", " Ship the Android widget", "Call the venue back", "+3"), labels(4))
        assertEquals(listOf("Standup notes", " Ship the Android widget", "Call the venue back", "Send the invoice", "Idea: weekly review template", "+1"), labels(6))
        assertTrue(todayItems(store, today.plusDays(30), 4).isEmpty())
        assertEquals(4, TodayWidget.rowsFor(0))
        assertEquals(1, TodayWidget.rowsFor(40))
        assertEquals(5, TodayWidget.rowsFor(220))
    }

    private fun shot(name: String, widthDp: Int, heightDp: Int, maxRows: Int) {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val density = activity.resources.displayMetrics.density
        val night = activity.resources.configuration.isNightModeActive
        val wallpaper = FrameLayout(activity).apply {
            setBackgroundColor(if (night) Color.rgb(12, 14, 22) else Color.rgb(196, 204, 221))
        }
        val views = TodayWidget.render(ApplicationProvider.getApplicationContext(), store, today, maxRows)
        val widget = views.apply(activity, wallpaper)
        wallpaper.addView(
            widget,
            FrameLayout.LayoutParams((widthDp * density).toInt(), (heightDp * density).toInt(), Gravity.CENTER),
        )
        val pad = (24 * density).toInt()
        activity.setContentView(
            wallpaper,
            FrameLayout.LayoutParams((widthDp * density).toInt() + 2 * pad, (heightDp * density).toInt() + 2 * pad),
        )
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(View.VISIBLE, widget.findViewById<TextView>(R.id.widget_title).visibility)
        wallpaper.captureRoboImage("build/screenshots/$name.png")
    }

    @Test
    fun rendersLight() {
        seed()
        shot("widget-today", 340, 300, TodayWidget.rowsFor(300))
        shot("widget-today-small", 250, 190, TodayWidget.rowsFor(190))
    }

    @Test
    @Config(qualifiers = "+night")
    fun rendersDark() {
        seed()
        shot("widget-today-dark", 340, 300, TodayWidget.rowsFor(300))
    }

    @Test
    fun rendersEmpty() {
        shot("widget-today-empty", 250, 150, TodayWidget.rowsFor(150))
    }
}
