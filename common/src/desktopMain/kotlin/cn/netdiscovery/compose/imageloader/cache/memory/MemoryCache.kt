package cn.netdiscovery.compose.imageloader.cache.memory

import androidx.compose.ui.graphics.ImageBitmap
import cn.netdiscovery.compose.imageloader.cache.LruCache

class MemoryCache(size: Long) {
    private val lruCache: LruCache<String, ImageBitmap>

    init {
        lruCache = LruCache(size, sizeCalculator = { _, value ->
            (value.width * value.height).toLong()
        })
    }

    suspend fun getBitmap(key: String): ImageBitmap? = lruCache.get(key)

    suspend fun putBitmap(key: String?, bitmap: ImageBitmap?) {
        if (key.isNullOrEmpty() || bitmap == null) {
            return
        }

        lruCache.put(key, bitmap)
    }
}