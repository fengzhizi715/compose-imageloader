package cn.netdiscovery.compose.imageloader.utils

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.utils.ImageUtils
 * @author: Tony Shen
 * @date: 2024/4/23 15:41
 * @version: V1.0 <描述当前版本功能>
 */
fun clamp(c: Int): Int {
    return if (c > 255) 255 else if (c < 0) 0 else c
}