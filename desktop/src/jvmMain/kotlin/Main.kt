import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


const val previewWidth = 600

fun main() = application {
    Window(
        title = "ImageLoader 演示程序",
        state = rememberWindowState(width = Dp(previewWidth * 2.toFloat()), height = 900.dp),
        onCloseRequest = ::exitApplication) {
        App()
    }
}
