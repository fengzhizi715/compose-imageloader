package cn.netdiscovery.compose.imageloader.transform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.Image
import java.awt.image.BufferedImage

class ResizeTransformation(private val width: Int, private val height: Int) : Transformer {

    override fun tag(): String {
        return TransformationTag.ResizeTransformation
    }

    override fun transform(imageBitmap: ImageBitmap): ImageBitmap {
        if (width == imageBitmap.width && height == imageBitmap.height) {
            return imageBitmap
        }
        val tmp = imageBitmap.toAwtImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)
        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = resizedImage.createGraphics()
        try {
            g2d.drawImage(tmp, 0, 0, null)
        } finally {
            g2d.dispose()
        }
        return resizedImage.toComposeImageBitmap()
    }
}