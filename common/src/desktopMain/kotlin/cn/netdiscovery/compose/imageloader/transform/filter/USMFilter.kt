package cn.netdiscovery.compose.imageloader.transform.filter

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import cn.netdiscovery.compose.imageloader.utils.clamp
import java.awt.image.BufferedImage

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.transform.filter.USMFilter
 * @author: Tony Shen
 * @date: 2024/4/24 15:07
 * @version: V1.0 <描述当前版本功能>
 */
class USMFilter(override val radius: Float =2f, val amount: Float = 0.5f, val threshold:Int =1) :GaussianFilter(radius) {

    override fun doFilter(image: BufferedImage): ImageBitmap {
        var inPixels = IntArray(width * height)
        var outPixels = IntArray(width * height)
        getRGB(image, 0, 0, width, height, inPixels)

        if ( radius > 0 ) {
            convolveAndTranspose(kernel, inPixels, outPixels, width, height, alpha, alpha && premultiplyAlpha, false, CLAMP_EDGES)
            convolveAndTranspose(kernel, outPixels, inPixels, height, width, alpha, false, alpha && premultiplyAlpha, CLAMP_EDGES)
        }

        getRGB( image,0, 0, width, height, outPixels)

        val a: Float = 4 * amount

        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb1 = outPixels[index]
                var r1 = rgb1 shr 16 and 0xff
                var g1 = rgb1 shr 8 and 0xff
                var b1 = rgb1 and 0xff
                val rgb2 = inPixels[index]
                val r2 = rgb2 shr 16 and 0xff
                val g2 = rgb2 shr 8 and 0xff
                val b2 = rgb2 and 0xff
                if (Math.abs(r1 - r2) >= threshold) r1 = clamp(((a + 1) * (r1 - r2) + r2).toInt())
                if (Math.abs(g1 - g2) >= threshold) g1 = clamp(((a + 1) * (g1 - g2) + g2).toInt())
                if (Math.abs(b1 - b2) >= threshold) b1 = clamp(((a + 1) * (b1 - b2) + b2).toInt())
                inPixels[index] = rgb1 and -0x1000000 or (r1 shl 16) or (g1 shl 8) or b1
                index++
            }
        }

        setRGB(image, 0, 0, width, height, inPixels)
        return image.toComposeImageBitmap()
    }
}