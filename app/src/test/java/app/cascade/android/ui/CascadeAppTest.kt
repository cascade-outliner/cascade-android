package app.cascade.android.ui

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Settings
import app.cascade.android.data.Task
import app.cascade.android.data.dayId
import app.cascade.android.data.isoDay
import app.cascade.android.ui.theme.CascadeTheme
import com.github.takahirom.roborazzi.captureRoboImage
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
class CascadeAppTest {
    @get:Rule
    val compose = createComposeRule()

    private val store = OutlineStore().apply { loaded(emptyList()) }
    private lateinit var settings: Settings
    private val today = LocalDate.now()

    @Before
    fun setUp() {
        settings = Settings(ApplicationProvider.getApplicationContext())
        settings.onboarded = true
    }

    private fun launch(dark: Boolean = false, deepLink: String? = null) {
        compose.setContent {
            CascadeTheme(dark = dark) { CascadeApp(store, settings, deepLink = deepLink, onDeepLinkHandled = {}) }
        }
    }

    private fun shot(name: String) = compose.onRoot().captureRoboImage("build/screenshots/$name.png")

    private fun seed(): Map<String, String> {
        val ids = HashMap<String, String>()
        fun add(parent: String?, text: String) = store.create(parent, text = text).also { ids[text] = it }
        val launch = add(null, "Launch prep")
        add(launch, "Write the announcement")
        val press = add(launch, "Email the press list")
        add(press, "Draft goes to Sara first")
        add(launch, "Book the venue")
        val reading = add(null, "Reading list")
        add(reading, "The Timeless Way of Building")
        add(null, "Ideas for the garden")
        store.setTask(ids.getValue("Write the announcement"), Task(done = true))
        store.setTask(press, Task(done = false))
        store.setDue(press, isoDay(today))
        store.setTask(ids.getValue("Book the venue"), Task(done = false))
        store.setDue(ids.getValue("Book the venue"), isoDay(today.plusDays(7)))
        store.setCollapsed(reading, true)
        return ids
    }

    @Test
    fun onboardingWalksThroughThreeStepsAndDoesNotReturn() {
        settings.onboarded = false
        launch()
        compose.onNodeWithText("Everything is a line").assertIsDisplayed()
        shot("onboarding-1")
        compose.onNodeWithTag("onboarding-continue").performClick()
        compose.onNodeWithText("How do you want to start?").assertIsDisplayed()
        shot("onboarding-2")
        compose.onNodeWithTag("onboarding-back").performClick()
        compose.onNodeWithText("Everything is a line").assertIsDisplayed()
        compose.onNodeWithTag("onboarding-continue").performClick()
        compose.onNodeWithTag("onboarding-continue").performClick()
        compose.onNodeWithText("Still in development").assertIsDisplayed()
        shot("onboarding-3")
        compose.onNodeWithTag("onboarding-start").performClick()
        compose.onNodeWithTag("outline-empty").assertIsDisplayed()
        assertTrue(settings.onboarded)
        shot("outline-empty")
    }

    @Test
    fun anOutlineWithNodesSkipsOnboarding() {
        settings.onboarded = false
        store.create(text = "Already here")
        launch()
        compose.onNodeWithText("Already here").assertIsDisplayed()
    }

    @Test
    fun captureAddsNodesInOrderAndIgnoresBlankInput() {
        launch()
        val input = compose.onNodeWithTag("capture-bar-input")
        input.performTextInput("  First  ")
        compose.onNodeWithTag("capture-bar-submit").performClick()
        input.performTextInput("Second")
        input.performImeAction()
        input.performTextInput("   ")
        input.performImeAction()
        assertEquals(listOf("First", "Second"), store.rows().map { it.node.text })
        compose.onAllNodesWithTag("outliner-row").assertCountEquals(2)
    }

    @Test
    fun aSlashInTheCaptureBarPicksAChipThatAppliesOnSubmit() {
        launch()
        val input = compose.onNodeWithTag("capture-bar-input")
        input.performTextInput("Buy milk /ta")
        compose.onNodeWithTag("slash-menu").assertIsDisplayed()
        shot("capture-slash-menu")
        compose.onNodeWithText("Turn into task").performClick()
        compose.onAllNodesWithTag("capture-bar-chip").assertCountEquals(1)
        input.assertTextEquals("Buy milk ")
        compose.onNodeWithTag("capture-bar-plus").performClick()
        compose.onNodeWithText("Due tomorrow").performClick()
        compose.onAllNodesWithTag("capture-bar-chip").assertCountEquals(2)
        input.performImeAction()
        val node = store.rows().single().node
        assertEquals("Buy milk", node.text)
        assertEquals(Task(done = false), node.task)
        assertEquals(isoDay(today.plusDays(1)), node.due)
        compose.onAllNodesWithTag("capture-bar-chip").assertCountEquals(0)
    }

    @Test
    fun aSlashInARowRunsTheCommandAndRemovesTheQuery() {
        val id = store.create(text = "Call the bank")
        launch()
        compose.onNodeWithText("Call the bank").performTextInput("/due")
        compose.onNodeWithText("Due today").performClick()
        assertEquals("Call the bank", store.get(id)?.text)
        assertEquals(isoDay(today), store.get(id)?.due)
        compose.onNodeWithTag("due-pill").assertTextEquals("Today")
    }

    @Test
    fun theOutlineShowsTasksDueDatesAndCollapsedNodes() {
        seed()
        launch()
        compose.onAllNodesWithTag("outliner-row").assertCountEquals(7)
        compose.onNodeWithText("The Timeless Way of Building").assertDoesNotExist()
        shot("outline")
        compose.onNodeWithContentDescription("Expand").performClick()
        compose.onNodeWithText("The Timeless Way of Building").assertIsDisplayed()
        compose.onAllNodesWithContentDescription("Mark as done").onFirst().performClick()
        assertEquals(Task(done = true), store.rows().first { it.node.text == "Email the press list" }.node.task)
    }

    @Test
    fun darkTheme() {
        seed()
        launch(dark = true)
        shot("outline-dark")
    }

    @Test
    fun tappingABulletZoomsInAndBreadcrumbsLeadBack() {
        val ids = seed()
        launch()
        compose.onAllNodesWithTag("outliner-bullet")[2].performClick()
        compose.onNodeWithTag("zoom-title").assertTextEquals("Email the press list")
        compose.onAllNodesWithTag("outliner-row").assertCountEquals(1)
        shot("zoomed")
        compose.onNodeWithTag("capture-bar-input").performTextInput("Follow up Friday")
        compose.onNodeWithTag("capture-bar-input").performImeAction()
        assertEquals(ids["Email the press list"], store.rows().first { it.node.text == "Follow up Friday" }.node.parentId)
        compose.onNodeWithText("Launch prep").performClick()
        compose.onNodeWithTag("zoom-title").assertTextEquals("Launch prep")
        compose.onNodeWithContentDescription("Zoom out to root").performClick()
        compose.onNodeWithTag("zoom-title").assertDoesNotExist()
    }

    @Test
    fun holdingABulletOpensTheMenu() {
        val ids = seed()
        launch()
        compose.onAllNodesWithTag("outliner-bullet")[4].performTouchInput { longClick(durationMillis = 900) }
        compose.onNodeWithTag("node-menu").assertIsDisplayed()
        shot("node-menu")
        compose.onNodeWithText("Duplicate").performClick()
        compose.waitForIdle()
        assertEquals(2, store.rows().count { it.node.text == "Book the venue" })
        compose.onAllNodesWithTag("outliner-bullet")[0].performTouchInput { longClick(durationMillis = 900) }
        compose.onNodeWithText("Delete").performClick()
        compose.waitForIdle()
        assertNull(store.get(ids.getValue("Launch prep")))
        assertNull(store.get(ids.getValue("Book the venue")))
    }

    @Test
    fun draggingABulletMovesTheNode() {
        seed()
        launch()
        val bullets = compose.onAllNodesWithTag("outliner-bullet")
        bullets[6].performTouchInput {
            down(center)
            advanceEventTime(900)
            moveBy(androidx.compose.ui.geometry.Offset(0f, -40f))
            moveBy(androidx.compose.ui.geometry.Offset(0f, -2000f))
            up()
        }
        compose.waitForIdle()
        assertEquals("Ideas for the garden", store.rows().first().node.text)
    }

    @Test
    fun todaysNoteIsVirtualUntilSomethingIsAddedAndListsWhatIsDueElsewhere() {
        seed()
        launch()
        compose.onNode(hasText("Today") and hasClickAction()).performClick()
        compose.onNodeWithTag("zoom-title").assertTextEquals("Today")
        assertNull(store.get(dayId(today)))
        compose.onNodeWithText("DUE TODAY, ELSEWHERE").assertIsDisplayed()
        compose.onNodeWithText("Email the press list").assertIsDisplayed()
        compose.onNodeWithContentDescription("Previous day").performClick()
        compose.onNodeWithTag("zoom-title").assertTextEquals("Yesterday")
        assertNull(store.get(dayId(today.minusDays(1))))
        compose.onNodeWithContentDescription("Next day").performClick()
        compose.onNodeWithTag("capture-bar-input").performTextInput("Standup notes")
        compose.onNodeWithTag("capture-bar-input").performImeAction()
        assertEquals(dayId(today), store.rows().first { it.node.text == "Standup notes" }.node.parentId)
        shot("today")
    }

    @Test
    fun searchFindsNodesAndCreatesFromTheQuery() {
        val ids = seed()
        launch()
        compose.onNodeWithContentDescription("Search").performClick()
        compose.onNodeWithTag("search-input").performTextInput("venue")
        compose.onNodeWithText("1 result").assertIsDisplayed()
        shot("search")
        compose.onNodeWithTag("search-hit").performClick()
        compose.onNodeWithTag("zoom-title").assertTextEquals("Book the venue")
        compose.onNodeWithContentDescription("Search").performClick()
        compose.onNodeWithTag("search-input").performTextInput("Seating plan")
        compose.onNodeWithTag("search-create").performClick()
        assertEquals(ids["Book the venue"], store.rows().first { it.node.text == "Seating plan" }.node.parentId)
    }

    @Test
    fun aLinkToAMissingNodeSaysSo() {
        launch(deepLink = "gone")
        compose.onNodeWithTag("node-not-found").assertIsDisplayed()
        compose.onNode(hasText("Back to the outline")).performClick()
        compose.onNodeWithTag("outline-empty").assertIsDisplayed()
    }
}
