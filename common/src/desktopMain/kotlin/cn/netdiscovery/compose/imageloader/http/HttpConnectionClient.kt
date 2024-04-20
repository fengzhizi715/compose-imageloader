package cn.netdiscovery.compose.imageloader.http

import cn.netdiscovery.compose.imageloader.cache.disk.DiskLruCache
import cn.netdiscovery.compose.imageloader.utils.closeQuietly
import cn.netdiscovery.compose.imageloader.utils.openConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

class HttpConnectionClient(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val job = Job()

    var diskLruCache: DiskLruCache? = null

    fun dispatcher(): CoroutineDispatcher = dispatcher

    suspend fun getImage(url: String, key: String, ua:String?=null): ResponseData? {
        var conn: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            conn = url.openConnection(ua)
            conn.requestMethod = "GET"
            if (conn.responseCode != 200) {
                debugLog("Response status code is (${conn.responseCode})!")
                return null
            }

            val contentTypeString = conn.contentType
            if (contentTypeString == null) {
                debugLog("Content-type is null!")
                return null
            }

            val contentLength = conn.contentLength
            if (contentLength <= 0) {
                debugLog("Content length is null!")
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

    fun close() {
        job.cancel()
    }

    private fun debugLog(msg: String) {
//        ImageLoaderLogger.debugLog("Thread: ${Thread.currentThread().name}, $msg")
    }
}