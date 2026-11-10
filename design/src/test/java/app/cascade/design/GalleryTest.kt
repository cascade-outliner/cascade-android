package app.cascade.design

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cascade.design.gallery.GallerySectionFrame
import app.cascade.design.gallery.gallerySections
import app.cascade.design.theme.CascadeTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
class GalleryTest(private val index: Int, private val name: String) {
    @get:Rule
    val compose = createComposeRule()

    private fun render(dark: Boolean) {
        val section = gallerySections[index]
        compose.setContent { CascadeTheme(dark = dark) { GallerySectionFrame(section) } }
        val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "-")
        compose.onRoot().captureRoboImage("build/screenshots/$slug-${if (dark) "dark" else "light"}.png")
    }

    @Test
    fun light() = render(dark = false)

    @Test
    fun dark() = render(dark = true)

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        fun sections(): List<Array<Any>> = gallerySections.mapIndexed { index, section -> arrayOf(index, section.name) }
    }
}

@RunWith(AndroidJUnit4::class)
class GalleryNamesTest {
    @Test
    fun sectionNamesAreUnique() {
        assertEquals(gallerySections.size, gallerySections.map { it.name }.toSet().size)
    }
}
