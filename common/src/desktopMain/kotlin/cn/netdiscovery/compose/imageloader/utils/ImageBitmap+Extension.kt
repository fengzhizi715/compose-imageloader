package cn.netdiscovery.compose.imageloader.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.utils.`ImageBitmap+Extension`
 * @author: Tony Shen
 * @date: 2024/4/20 00:07
 * @version: V1.0 <描述当前版本功能>
 */
internal fun ImageBitmap.toBitmapPainter(): BitmapPainter = BitmapPainter(this)