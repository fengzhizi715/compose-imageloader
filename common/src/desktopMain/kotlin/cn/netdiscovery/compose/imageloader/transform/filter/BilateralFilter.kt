package cn.netdiscovery.compose.imageloader.transform.filter

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import cn.netdiscovery.compose.imageloader.utils.clamp
import java.awt.image.BufferedImage

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.transform.filter.BilateralFilter
 * @author: Tony Shen
 * @date: 2024/4/24 16:37
 * @version: V1.0 <描述当前版本功能>
 */
class BilateralFilter(private val ds:Double = 1.0, private val rs:Double = 1.0): BaseFilter() {

    private val factor = -0.5
    private var radius = 0 // half-length of Gaussian kernel Adobe Photoshop

    private lateinit var cWeightTable: Array<DoubleArray>
    private lateinit var sWeightTable: DoubleArray

    override fun doFilter(image: BufferedImage): ImageBitmap {

        radius = Math.max(ds, rs).toInt()
        buildDistanceWeightTable()
        buildSimilarityWeightTable()

        val inPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)

        getRGB(image, 0, 0, width, height, inPixels)

        var index = 0
        var redSum = 0.0
        var greenSum = 0.0
        var blueSum = 0.0
        var csRedWeight = 0.0
        var csGreenWeight = 0.0
        var csBlueWeight = 0.0
        var csSumRedWeight = 0.0
        var csSumGreenWeight = 0.0
        var csSumBlueWeight = 0.0
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
                var rowOffset = 0
                var colOffset = 0
                var index2 = 0
                var ta2 = 0
                var tr2 = 0
                var tg2 = 0
                var tb2 = 0
                for (semirow in -radius..radius) {
                    for (semicol in -radius..radius) {
                        rowOffset = if (row + semirow >= 0 && row + semirow < height) {
                            row + semirow
                        } else {
                            0
                        }
                        colOffset = if (semicol + col >= 0 && semicol + col < width) {
                            col + semicol
                        } else {
                            0
                        }
                        index2 = rowOffset * width + colOffset
                        ta2 = inPixels[index2] shr 24 and 0xff
                        tr2 = inPixels[index2] shr 16 and 0xff
                        tg2 = inPixels[index2] shr 8 and 0xff
                        tb2 = inPixels[index2] and 0xff
                        csRedWeight =
                            cWeightTable[semirow + radius][semicol + radius] * sWeightTable[Math.abs(tr2 - tr)]
                        csGreenWeight =
                            cWeightTable[semirow + radius][semicol + radius] * sWeightTable[Math.abs(tg2 - tg)]
                        csBlueWeight =
                            cWeightTable[semirow + radius][semicol + radius] * sWeightTable[Math.abs(tb2 - tb)]
                        csSumRedWeight += csRedWeight
                        csSumGreenWeight += csGreenWeight
                        csSumBlueWeight += csBlueWeight
                        redSum += csRedWeight * tr2.toDouble()
                        greenSum += csGreenWeight * tg2.toDouble()
                        blueSum += csBlueWeight * tb2.toDouble()
                    }
                }
                tr = Math.floor(redSum / csSumRedWeight).toInt()
                tg = Math.floor(greenSum / csSumGreenWeight).toInt()
                tb = Math.floor(blueSum / csSumBlueWeight).toInt()
                outPixels[index] = ta shl 24 or (clamp(tr) shl 16) or (clamp(tg) shl 8) or clamp(tb)

                // clean value for next time...
                blueSum = 0.0
                greenSum = blueSum
                redSum = greenSum
                csBlueWeight = 0.0
                csGreenWeight = csBlueWeight
                csRedWeight = csGreenWeight
                csSumBlueWeight = 0.0
                csSumGreenWeight = csSumBlueWeight
                csSumRedWeight = csSumGreenWeight
            }
        }
        setRGB(image, 0, 0, width, height, outPixels)

        return image.toComposeImageBitmap()
    }

    private fun buildDistanceWeightTable() {
        val size: Int = 2 * radius + 1
        cWeightTable = Array<DoubleArray>(size) { DoubleArray(size) }
        for (semirow in -radius..radius) {
            for (semicol in -radius..radius) {
                // calculate Euclidean distance between center point and close pixels
                val delta = Math.sqrt((semirow * semirow + semicol * semicol).toDouble()) / ds
                val deltaDelta = delta * delta
                cWeightTable.get(semirow + radius)[semicol + radius] = Math.exp(deltaDelta * factor)
            }
        }
    }

    /**
     * for gray image
     * @param row
     * @param col
     * @param inPixels
     */
    private fun buildSimilarityWeightTable() {
        sWeightTable = DoubleArray(256) // since the color scope is 0 ~ 255
        for (i in 0..255) {
            val delta = Math.sqrt((i * i).toDouble()) / rs
            val deltaDelta = delta * delta
            sWeightTable[i] = Math.exp(deltaDelta * factor)
        }
    }
}