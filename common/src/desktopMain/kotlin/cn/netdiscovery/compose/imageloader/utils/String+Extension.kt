package cn.netdiscovery.compose.imageloader.utils

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.utils.`String+Extension`
 * @author: Tony Shen
 * @date: 2024/4/19 18:20
 * @version: V1.0 <描述当前版本功能>
 */
@Throws(IOException::class)
internal fun String.openConnection(
    ua: String?, connectTimeout: Int = 6000, readTimeout: Int = 6000
): HttpURLConnection {
    val connection = URL(this).openConnection() as HttpURLConnection
    if (ua != null) {
        connection.setRequestProperty("User-Agent", ua)
    }
    connection.connectTimeout = connectTimeout
    connection.readTimeout = readTimeout
    return connection
}