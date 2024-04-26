package cn.netdiscovery.compose.imageloader.http

import cn.netdiscovery.compose.imageloader.cache.disk.DiskLruCache
import cn.netdiscovery.compose.imageloader.core.ImageLoaderFactory.RETRY_MAX
import cn.netdiscovery.compose.imageloader.log.logD
import cn.netdiscovery.compose.imageloader.log.logE
import cn.netdiscovery.compose.imageloader.utils.closeQuietly
import cn.netdiscovery.compose.imageloader.utils.extension.openConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

class HttpConnectionClient(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val timeout:Int = 6000
) {
    var diskLruCache: DiskLruCache? = null

    fun dispatcher(): CoroutineDispatcher = dispatcher

    suspend fun getImage(url: String, key: String, ua:String?=null): ResponseData? {
        var conn: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            var retry = 0
            do {
                conn = url.openConnection(ua,timeout,timeout)
                conn.requestMethod = "GET"

                when (conn.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        "Response status code is ${conn.responseCode}".logD()
                        break
                    }

                    HttpURLConnection.HTTP_GATEWAY_TIMEOUT ->  {
                        "gateway timeout".logE()
                        break
                    }

                    HttpURLConnection.HTTP_UNAVAILABLE -> {
                        "http unavailable".logE()
                        break
                    }
                }

                retry++
            } while (retry < RETRY_MAX)

            if (conn?.responseCode != 200) {
                "Response status code is ${conn?.responseCode}".logE()
                return null
            }

            val contentTypeString = conn.contentType
            if (contentTypeString == null) {
                "Content-type is null!".logE()
                return null
            }

            val contentLength = conn.contentLength
            if (contentLength <= 0) {
                "Content length is null!".logE()
                return null
            }

            inputStream = conn.inputStream

            diskLruCache?.getOrPut(key) { cacheFile ->
                try {
                    val outputStream = cacheFile.outputStream()
                    inputStream.copyTo(outputStream)
                    closeQuietly(outputStream)
                    true // Caching succeeded - Save the file
                } catch (ex: IOException) {
                    false
                }
            }
            val snapshot = diskLruCache?.get(key) ?: return null
            return ResponseData(contentTypeString, contentLength, snapshot)
        } catch (error: Throwable) {
            error.printStackTrace()
            return null
        } finally {
            closeQuietly(inputStream)
            conn?.disconnect()
        }
    }
}