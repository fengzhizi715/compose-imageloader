package cn.netdiscovery.compose.imageloader.transform.filter

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.transform.filter.SepiaToneFilter
 * @author: Tony Shen
 * @date: 2024/4/23 15:02
 * @version: V1.0 老照片特效
 */
class SepiaToneFilter :BaseFilter() {

    override fun doFilter(image: BufferedImage): ImageBitmap {
        val inPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        getRGB(image, 0, 0, width, height, inPixels)
        var index = 0
        for (row in 0 until height) {
            var ta = 0
            var tr = 0
            var tg = 0
            var tb = 0
            for (col in 0 until width) {
                index = row * width + col
                ta = inPixels[index] shr 24 and 0xff
                tr = inPixels[index] shr 16 and 0xff
                tg = inPixels[index] shr 8 and 0xff
                tb = inPixels[index] and 0xff

                val fr = colorBlend(noise(), tr * 0.393 + tg * 0.769 + tb * 0.189, tr).toInt()
                val fg = colorBlend(noise(), tr * 0.349 + tg * 0.686 + tb * 0.168, tg).toInt()
                val fb = colorBlend(noise(), tr * 0.272 + tg * 0.534 + tb * 0.131, tb).toInt()
                outPixels[index] = ta shl 24 or (clamp(fr) shl 16) or (clamp(fg) shl 8) or clamp(fb)
            }
        }
        setRGB(image, 0, 0, width, height, outPixels)

        return image.toComposeImageBitmap()
    }

    private fun noise(): Double = Math.random() * 0.5 + 0.5

    private fun colorBlend(scale: Double, dest: Double, src: Int): Double {
        return scale * dest + (1.0 - scale) * src
    }

    fun clamp(c: Int): Int {
        return if (c > 255) 255 else if (c < 0) 0 else c
    }
}