package cn.netdiscovery.compose.imageloader.transform.filter

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import cn.netdiscovery.compose.imageloader.transform.Transformer
import java.awt.Color
import java.awt.image.BufferedImage

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.transform.WhiteImageTransformation
 * @author: Tony Shen
 * @date: 2024/4/23 10:24
 * @version: V1.0 <描述当前版本功能>
 */
class WhiteImageFilter(val beta:Double = 1.1): Transformer {

    override fun transform(imageBitmap: ImageBitmap): ImageBitmap {
        // make LUT
        val lut = IntArray(256)
        for (i in 0..255) {
            lut[i] = imageMath(i)
        }

        val width: Int = imageBitmap.width
        val height: Int = imageBitmap.height

        val image: BufferedImage = imageBitmap.toAwtImage()

        for (row in 0 until height) {
            for (col in 0 until width) {
                val rgb = image.getRGB(col,row)

                var r = rgb and (0x00ff0000 shr 16)
                var g = rgb and (0x0000ff00 shr 8)
                var b = rgb and 0x000000ff

                r = lut[r and 0xff]
                g = lut[g and 0xff]
                b = lut[b and 0xff]
                image.setRGB(col, row, Color(r, g, b).rgb)
            }
        }

        return image.toComposeImageBitmap()
    }

    private fun imageMath(gray: Int): Int {
        val scale = 255 / (Math.log(255 * (this.beta - 1) + 1) / Math.log(this.beta))
        val p1 = Math.log(gray * (this.beta - 1) + 1)
        val np = p1 / Math.log(this.beta)
        return (np * scale).toInt()
    }
}