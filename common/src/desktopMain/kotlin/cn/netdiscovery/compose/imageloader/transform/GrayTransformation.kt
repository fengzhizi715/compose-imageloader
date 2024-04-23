package cn.netdiscovery.compose.imageloader.transform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.Color
import java.awt.image.BufferedImage

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.transform.GrayTransformation
 * @author: Tony Shen
 * @date: 2024/4/22 19:53
 * @version: V1.0  Gray = R*0.299+G*0.587+B*0.114
 */
class GrayTransformation: Transformer {

    override fun transform(imageBitmap: ImageBitmap): ImageBitmap {
        val width: Int = imageBitmap.width
        val height: Int = imageBitmap.height

        val image:BufferedImage = imageBitmap.toAwtImage()

        for (row in 0 until height) {
            for (col in 0 until width) {
                val rgb = image.getRGB(col,row)
                val r = rgb and (0x00ff0000 shr 16)
                val g = rgb and (0x0000ff00 shr 8)
                val b = rgb and 0x000000ff

                val color = (r * 0.299 + g * 0.587 + b * 0.114).toInt()
                image.setRGB(col, row, Color(color, color, color).rgb)
            }
        }

        return image.toComposeImageBitmap()
    }
}