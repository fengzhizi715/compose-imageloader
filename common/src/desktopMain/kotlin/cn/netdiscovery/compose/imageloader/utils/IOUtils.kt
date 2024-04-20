package cn.netdiscovery.compose.imageloader.utils

import java.io.Closeable
import java.io.IOException


/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.utils.IOUtils
 * @author: Tony Shen
 * @date:  2024/4/20 15:06
 * @version: V1.0 <描述当前版本功能>
 */
/**
 * 安全关闭io流
 * @param closeable
 */
fun closeQuietly(closeable: Closeable?) {
    if (closeable != null) {
        try {
            closeable.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

/**
 * 安全关闭io流
 * @param closeables
 */
fun closeQuietly(vararg closeables: Closeable?) {
    if (closeables.isNotEmpty()) {
        for (closeable in closeables) {
            closeQuietly(closeable)
        }
    }
}