package cn.netdiscovery.compose.imageloader.core

import androidx.compose.ui.res.loadImageBitmap
import cn.netdiscovery.compose.imageloader.cache.LruUtil
import cn.netdiscovery.compose.imageloader.cache.disk.DiskLruCache
import cn.netdiscovery.compose.imageloader.cache.memory.MemoryCache
import cn.netdiscovery.compose.imageloader.http.HttpConnectionClient
import cn.netdiscovery.compose.imageloader.transform.Transformer
import cn.netdiscovery.compose.imageloader.utils.extension.toBitmapPainter
import cn.netdiscovery.compose.imageloader.utils.extension.transformationKey
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.ImageLoaderFactory
 * @author: Tony Shen
 * @date: 2024/4/16 23:47
 * @version: V1.0 <描述当前版本功能>
 */
enum class SaveStrategy {
    ORIGINAL,TRANSFORMED
}

object ImageLoaderFactory {

    const val CACHE_DEFAULT_MEMORY_SIZE = 1024 * 1024 * 300L
    const val CACHE_DEFAULT_DISK_SIZE = 1024 * 1024 * 100L
    val USER_DIR = File(System.getProperty("user.dir"))

    var maxMemoryCacheSize by Delegates.notNull<Long>()
    var maxDiskCacheSize by Delegates.notNull<Long>()
    lateinit var rootDirectory:File

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var diskLruCache: DiskLruCache? = null
    private lateinit var memoryLruCache: MemoryCache
    private lateinit var imageCacheDir: File
    private lateinit var client: HttpConnectionClient

    fun configuration(
        maxMemoryCacheSize: Long = CACHE_DEFAULT_MEMORY_SIZE,
        maxDiskCacheSize: Long = CACHE_DEFAULT_DISK_SIZE,
        rootDirectory: File = USER_DIR
    ) {
        ImageLoaderFactory.maxMemoryCacheSize = maxMemoryCacheSize
        ImageLoaderFactory.maxDiskCacheSize = maxDiskCacheSize
        ImageLoaderFactory.rootDirectory = rootDirectory

        imageCacheDir = File(ImageLoaderFactory.rootDirectory, "imageCache")
        if (!imageCacheDir.exists()) {
            if (!imageCacheDir.mkdirs()) {
                throw IllegalStateException("Could not create image cache directory: ${imageCacheDir.absolutePath}")
            }
        }
        memoryLruCache = MemoryCache(ImageLoaderFactory.maxMemoryCacheSize)
        client = HttpConnectionClient()

        scope.launch {
            diskLruCache = DiskLruCache.open(directory = imageCacheDir, maxSize = ImageLoaderFactory.maxDiskCacheSize)
            client.diskLruCache = diskLruCache
        }
    }

    private fun debugLog(msg: String) {
//        ImageLoaderLogger.debugLog("Thread: ${Thread.currentThread().name}, $msg")
    }

    suspend fun enqueue(request: ImageRequest): ImageResponse {
        val loadFile = request.file
        if (loadFile != null && loadFile.exists()) {
            return runFileLoad(loadFile, request.transformers)
        }
        return runUrlLoad(request)
    }

    private suspend fun runFileLoad(file: File, transformers: MutableList<Transformer>): ImageResponse {

        return scope.async(Dispatchers.IO) {
            val key = LruUtil.hashKey(file.absolutePath) + transformers.transformationKey()
            val cachedImageBitmap = memoryLruCache.getBitmap(key)
            if (cachedImageBitmap != null) {
                ImageResponse(cachedImageBitmap.toBitmapPainter(), null)
            } else {
                try {
                    var imageBitmap = file.inputStream().buffered().use(::loadImageBitmap)
                    for (transformer in transformers) {
                        imageBitmap = transformer.transform(imageBitmap)
                    }
                    memoryLruCache.putBitmap(key, imageBitmap)
                    ImageResponse(imageBitmap.toBitmapPainter(), null)
                } catch (e: Exception) {
                    ImageResponse(null, e)
                }
            }
        }.await()
    }

    private suspend fun runUrlLoad(request: ImageRequest): ImageResponse {

        val url = request.url
        if (url.isNullOrEmpty()) {
            debugLog("onError - Url is null or empty!")
            return ImageResponse(null, NullPointerException("Url is null or empty!"))
        }

        return scope.async(Dispatchers.IO) {

            val diskKey = LruUtil.hashKey(url)

            if (request.useCache) {
                val memoryKey = diskKey + request.transformers.transformationKey()
                val memoryImage = memoryLruCache.getBitmap(memoryKey)
                if (memoryImage != null) {
                    debugLog("onSuccess - from: memory")
                    return@async ImageResponse(memoryImage.toBitmapPainter(), null)
                }

                try {
                    val cacheFile = try {
                        diskLruCache?.get(diskKey)
                    } catch (e: IOException) {
                        null
                    }

                    if (cacheFile == null) {
                        debugLog("pull ($url)")
                        val data = scope.async(client.dispatcher()) {
                            client.getImage(url, diskKey, request.ua)
                        }.await()
                        val newFetchedCache = data?.contentSnapshot
                        if (newFetchedCache == null) {
                            debugLog("onError")
                            return@async ImageResponse(null, NullPointerException("Can't find the local image snapshot"))
                        } else {
                            var imageBitmap = loadImageBitmap(newFetchedCache.inputStream())
                            if (diskKey != memoryKey) {
                                for (transformer in request.transformers) {
                                    imageBitmap = transformer.transform(imageBitmap)
                                }
                            }
                            memoryLruCache.putBitmap(memoryKey, imageBitmap)
                            debugLog("onSuccess - from: network")
                            return@async ImageResponse(imageBitmap.toBitmapPainter(), null)
                        }
                    } else {
                        var imageBitmap = loadImageBitmap(cacheFile.inputStream())
                        if (diskKey != memoryKey) {
                            for (transformer in request.transformers) {
                                imageBitmap = transformer.transform(imageBitmap)
                            }
                        }
                        memoryLruCache.putBitmap(memoryKey, imageBitmap)
                        debugLog("onSuccess - from: disk")
                        return@async ImageResponse(imageBitmap.toBitmapPainter(), null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    debugLog("onError")
                    return@async ImageResponse(null, e)
                }
            } else {
                val data = scope.async(client.dispatcher()) {
                    client.getImage(url, diskKey, request.ua)
                }.await()

                if (data!=null) {
                    var imageBitmap = loadImageBitmap(data.contentSnapshot.inputStream())
                    for (transformer in request.transformers) {
                        imageBitmap = transformer.transform(imageBitmap)
                    }

                    return@async ImageResponse(imageBitmap.toBitmapPainter(), null)
                } else {
                    return@async ImageResponse(null, NullPointerException("Can't get the image..."))
                }
            }
        }.await()
    }

    fun shutdown() {
        job.cancel()
        client.close()
    }

    fun shutdownAndClearEverything() {
        shutdown()
        clearCache()
    }

    fun clearCache() {
        scope.launch {
            diskLruCache?.clear()
        }
    }
}