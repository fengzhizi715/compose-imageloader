import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.netdiscovery.compose.imageloader.transform.CenterCropTransformation
import cn.netdiscovery.compose.imageloader.transform.CircleCropTransformation
import cn.netdiscovery.compose.imageloader.transform.ResizeTransformation
import cn.netdiscovery.compose.imageloader.transform.Transformer
import cn.netdiscovery.compose.imageloader.transform.filter.*
import kotlin.random.Random


const val previewWidth = 600

val imageList = arrayListOf(
    "https://w.wallhaven.cc/full/wq/wallhaven-wq2787.jpg",
    "https://w.wallhaven.cc/full/9m/wallhaven-9mjoy1.png",
    "https://w.wallhaven.cc/full/6o/wallhaven-6ozkzl.jpg",
    "https://w.wallhaven.cc/full/z8/wallhaven-z8dg9y.png",
    "https://w.wallhaven.cc/full/j3/wallhaven-j3m8y5.png",
    "https://w.wallhaven.cc/full/y8/wallhaven-y8622k.jpg",
    "https://w.wallhaven.cc/full/57/wallhaven-572k81.png",
    "https://w.wallhaven.cc/full/72/wallhaven-72ywpv.jpg",
    "https://w.wallhaven.cc/full/v9/wallhaven-v9wo18.png",
    "https://w.wallhaven.cc/full/8o/wallhaven-8o23gk.jpg",
    "https://w.wallhaven.cc/full/pk/wallhaven-pkq1q9.png"
)

var imageUrl by mutableStateOf(imageList.first())

val transformationList = arrayListOf<Transformer>()

var transformations by mutableStateOf(arrayListOf<Transformer>())

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {

    Window(
        title = "ImageLoader 演示程序",
        state = rememberWindowState(width = Dp(previewWidth * 2.toFloat()), height = 900.dp),
        onCloseRequest = ::exitApplication) {
        MenuBar{
            Menu(text = "Demo", mnemonic = 'D') {
                Item(
                    text = "随机显示图片",
                    onClick = {
                        var random = Random.nextInt(0, imageList.size-1)
                        imageUrl = imageList[random]
                    },
                )

                Menu("transform") {
                    Item("CenterCropTransformation", onClick = {
                        transformationList.clear()
                        transformationList.add(CenterCropTransformation(400,400))

                        transformations = transformationList
                    })
                    Item("CircleCropTransformation", onClick = {
                        transformationList.clear()
                        transformationList.add(CircleCropTransformation(400f, Color.Red, Color.Gray))

                        transformations = transformationList
                    })
                    Item("ResizeTransformation", onClick = {
                        transformationList.clear()
                        transformationList.add(ResizeTransformation(400,400))

                        transformations = transformationList
                    })
                    Item("BilateralFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(BilateralFilter())

                        transformations = transformationList
                    })
                    Item("BoxBlurFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(BoxBlurFilter())

                        transformations = transformationList
                    })
                    Item("GaussianFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(GaussianFilter(10.0f))

                        transformations = transformationList
                    })
                    Item("GradientFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(GradientFilter())

                        transformations = transformationList
                    })
                    Item("GrayFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(GrayFilter())

                        transformations = transformationList
                    })
                    Item("MotionFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(MotionFilter())

                        transformations = transformationList
                    })
                    Item("SepiaToneFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(SepiaToneFilter())

                        transformations = transformationList
                    })
                    Item("SpotlightFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(SpotlightFilter())

                        transformations = transformationList
                    })
                    Item("USMFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(USMFilter())

                        transformations = transformationList
                    })
                    Item("WhiteImageFilter", onClick = {
                        transformationList.clear()
                        transformationList.add(WhiteImageFilter())

                        transformations = transformationList
                    })
                }
            }
        }

        App()
    }
}
