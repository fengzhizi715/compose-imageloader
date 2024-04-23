package cn.netdiscovery.compose.imageloader.transform.filter

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import cn.netdiscovery.compose.imageloader.transform.Transformer
import java.awt.image.BufferedImage

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.transform.filter.BaseFilter
 * @author: Tony Shen
 * @date: 2024/4/23 11:36
 * @version: V1.0 <描述当前版本功能>
 */
abstract class BaseFilter:Transformer {

    protected var width = 0
    protected var height = 0
    private lateinit var image: BufferedImage

    override fun transform(imageBitmap: ImageBitmap): ImageBitmap {
        width = imageBitmap.width
        height = imageBitmap.height
        image = imageBitmap.toAwtImage()

        return doFilter(image)
    }

    abstract fun doFilter(image: BufferedImage): ImageBitmap
}