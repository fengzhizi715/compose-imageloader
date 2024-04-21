package cn.netdiscovery.compose.imageloader.transform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap

class CenterCropTransformation(private val width: Int, private val height: Int) : Transformer {

    override fun transform(imageBitmap: ImageBitmap): ImageBitmap {
        val sourceWidth = imageBitmap.width
        val sourceHeight = imageBitmap.height
        if (sourceWidth == width && sourceHeight == height) {
            return imageBitmap
        }
        val xScale = width.toDouble() / sourceWidth
        val yScale = height.toDouble() / sourceHeight
        val (newXScale, newYScale) = if (yScale > xScale) {
            ((1.0 / yScale) * xScale) to 1.0
        } else {
            1.0 to ((1.0 / xScale) * yScale)
        }
        val scaledWidth = newXScale * sourceWidth
        val scaledHeight = newYScale * sourceHeight

        val left = ((sourceWidth - scaledWidth) / 2).toInt()
        val top = ((sourceHeight - scaledHeight) / 2).toInt()
        val width = scaledWidth.toInt()
        val height = scaledHeight.toInt()

        val centeredImage = imageBitmap.toAwtImage().getSubimage(left, top, width, height)
        return ResizeTransformation(width, height).transform(centeredImage.toComposeImageBitmap())
    }
}