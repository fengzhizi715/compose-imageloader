package cn.netdiscovery.compose.imageloader.core

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.core.ImageResponse
 * @author: Tony Shen
 * @date: 2024/4/16 23:25
 * @version: V1.0 <描述当前版本功能>
 */
internal fun ImageBitmap.toBitmapPainter(): BitmapPainter = BitmapPainter(this)

val defaultResponse = ImageResponse(null, null, true)

data class ImageResponse(
    val imagePainter: Painter?,
    val exception: Exception?,
    val isLoading: Boolean = false
)