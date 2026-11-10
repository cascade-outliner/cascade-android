package app.cascade.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Space {
    val px = 1.dp
    val x0_5 = 2.dp
    val x1 = 4.dp
    val x1_5 = 6.dp
    val x2 = 8.dp
    val x2_5 = 10.dp
    val x3 = 12.dp
    val x3_5 = 14.dp
    val x4 = 16.dp
    val x5 = 20.dp
    val x6 = 24.dp
    val x8 = 32.dp
}

object Radius {
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(20.dp)
    val xl = RoundedCornerShape(24.dp)
    val full = CircleShape
}

object Elevation {
    val float = 10.dp
    val popup = 12.dp
}

object BorderWidth {
    val thin = 1.dp
    val thick = 1.5.dp
}

object Motion {
    const val D100 = 100
    const val D150 = 150
    const val D200 = 200
    const val D300 = 300

    val soft = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)
    val pop = CubicBezierEasing(0.3f, 1.4f, 0.5f, 1f)
    val exit = CubicBezierEasing(0.4f, 0f, 1f, 1f)
}
