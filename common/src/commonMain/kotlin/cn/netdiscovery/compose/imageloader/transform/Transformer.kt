package cn.netdiscovery.compose.imageloader.transform

import androidx.compose.ui.graphics.ImageBitmap

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.transform.Transformer
 * @author: Tony Shen
 * @date: 2024/4/16 23:24
 * @version: V1.0 <描述当前版本功能>
 */
interface Transformer {

    fun tag(): String

    fun transform(imageBitmap: ImageBitmap): ImageBitmap
}