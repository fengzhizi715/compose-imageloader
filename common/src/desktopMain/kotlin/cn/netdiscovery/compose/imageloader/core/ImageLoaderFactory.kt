package cn.netdiscovery.compose.imageloader.core

import androidx.compose.ui.res.loadImageBitmap
import cn.netdiscovery.compose.imageloader.cache.disk.DiskLruCache
import cn.netdiscovery.compose.imageloader.cache.md5Key
import cn.netdiscovery.compose.imageloader.cache.memory.MemoryCache
import cn.netdiscovery.compose.imageloader.exception.ImageLoaderException
import cn.netdiscovery.compose.imageloader.http.HttpConnectionClient
import cn.netdiscovery.compose.imageloader.log.*
import cn.netdiscovery.compose.imageloader.transform.Transformer
import cn.netdiscovery.compose.imageloader.utils.extension.prettyDisplay
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
object ImageLoaderFactory {

    private const val CACHE_DEFAULT_MEMORY_SIZE = 1024 * 1024 * 300L // 300 M memory
    private const val CACHE_DEFAULT_DISK_SIZE = 1024 * 1024 * 100L   // 100 M disk
    private val USER_DIR = File(System.getProperty("user.dir"))
    private val defaultLogger = DefaultLogger

    private var maxMemoryCacheSize by Delegates.notNull<Long>()
    private var maxDiskCacheSize by Delegates.notNull<Long>()
    private lateinit var rootDirectory:File
    private lateinit var logger:Logger

    private val job = SupervisorJob()
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val scope = CoroutineScope(dispatcher + job)

    private var diskLruCache: DiskLruCache? = null
    private lateinit var memoryLruCache: MemoryCache
    private lateinit var imageCacheDir: File
    private lateinit var client: HttpConnectionClient
    const val RETRY_MAX = 3

    fun configuration(
        maxMemoryCacheSize: Long = CACHE_DEFAULT_MEMORY_SIZE,
        maxDiskCacheSize: Long = CACHE_DEFAULT_DISK_SIZE,
        rootDirectory: File = USER_DIR,
        logger:Logger = defaultLogger
    ) {
        ImageLoaderFactory.maxMemoryCacheSize = maxMemoryCacheSize
        ImageLoaderFactory.maxDiskCacheSize = maxDiskCacheSize
        ImageLoaderFactory.rootDirectory = rootDirectory
        ImageLoaderFactory.logger = logger

        memoryLruCache = MemoryCache(ImageLoaderFactory.maxMemoryCacheSize)
        client = HttpConnectionClient()

        imageCacheDir = File(ImageLoaderFactory.rootDirectory, "imageCache")
        if (!imageCacheDir.exists()) {
            if (!imageCacheDir.mkdirs()) {
                throw IllegalStateException("Could not create image cache directory: ${imageCacheDir.absolutePath}")
            }
        }

        LoggerProxy.initLogger(logger)

        scope.launch {
            diskLruCache = DiskLruCache.open(directory = imageCacheDir, maxSize = ImageLoaderFactory.maxDiskCacheSize)
            client.diskLruCache = diskLruCache
        }
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
            "file: ${file.absolutePath}, transformers:${transformers.prettyDisplay()}".logD()

            val key = md5Key(file.absolutePath) + transformers.transformationKey()
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
            ("request's url is null or empty!").logE()
            return ImageResponse(null, NullPointerException("Url is null or empty!"))
        }

        return scope.async(dispatcher) {
            "url: $url, transformers:${request.transformers.prettyDisplay()}".logD()

            val diskKey = md5Key(url)
            val memoryKey = diskKey + request.transformers.transformationKey()

            if (request.useCache) {

                // 优先取 memory 的数据
                val memoryImage = memoryLruCache.getBitmap(memoryKey)
                if (memoryImage != null) {
                   "onSuccess - load url:${request.url} from memory".logD()
                    return@async ImageResponse(memoryImage.toBitmapPainter(), null)
                }

                // memory 取不到数据，再去 disk 中取数据
                try {
                    val cacheFile = try {
                        diskLruCache?.get(diskKey)
                    } catch (e: IOException) {
                        null
                    }

                    // disk 也取不到数据，则通过 http 获取图片
                    if (cacheFile == null) {
                        val data = scope.async(client.dispatcher()) {
                            client.getImage(url, diskKey, request.ua)
                        }.await()

                        val newFetchedCache = data?.contentSnapshot
                        if (newFetchedCache == null) {
                            val errorMsg = "Can't find the local image snapshot"
                            errorMsg.logE()
                            return@async ImageResponse(null, ImageLoaderException(errorMsg))
                        } else {
                            return@async getImageResponse(request,newFetchedCache,diskKey,memoryKey,1)
                        }
                    } else {
                        return@async getImageResponse(request,cacheFile,diskKey,memoryKey,2)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    "onError".logE()
                    return@async ImageResponse(null, e)
                }
            } else {
                // 每次通过 http 获取图片
                val data = scope.async(client.dispatcher()) {
                    client.getImage(url, diskKey, request.ua)
                }.await()

                if (data!=null) {
                    return@async getImageResponse(request,data.contentSnapshot,diskKey,"",3)
                } else {
                    return@async ImageResponse(null, ImageLoaderException("Can't get the image..."))
                }
            }
        }.await()
    }

    /**
     * @param status 1:通过 http 获取图片；2:从 disk 中取数据；3：每次通过 http 获取图片
     */
    private suspend fun getImageResponse(request:ImageRequest,
                                         file:File,
                                         diskKey:String,
                                         memoryKey:String,
                                         status:Int):ImageResponse {
        var imageBitmap = loadImageBitmap(file.inputStream())

        if (diskKey != memoryKey) {
            for (transformer in request.transformers) {
                imageBitmap = transformer.transform(imageBitmap)
            }
        }

        when(status) {
            1 -> {
                memoryLruCache.putBitmap(memoryKey, imageBitmap)
                "onSuccess - load url:${request.url} from network".logD()
            }
            2 -> {
                memoryLruCache.putBitmap(memoryKey, imageBitmap)
                "onSuccess - load url:${request.url} from disk".logD()
            }
            3 -> {
                "onSuccess - load url:${request.url} from network(never use cache)".logD()
            }
        }

        return ImageResponse(imageBitmap.toBitmapPainter(), null)
    }

    fun shutdown() {
        job.cancel()
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