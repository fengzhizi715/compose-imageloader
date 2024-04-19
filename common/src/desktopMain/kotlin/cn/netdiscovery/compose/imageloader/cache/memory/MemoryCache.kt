package cn.netdiscovery.compose.imageloader.cache.memory

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.netdiscovery.compose.imageloader.cache.LruCache

class MemoryCache(size: Long) {
    private val lruCache: LruCache<String, ImageBitmap>

    init {
        lruCache = LruCache(size, sizeCalculator = { key, value ->
            val s = when (value.config) {
                ImageBitmapConfig.Argb8888 -> {
                    4
                }

                ImageBitmapConfig.Alpha8 -> {
                    1
                }

                ImageBitmapConfig.Rgb565 -> {
                    3
                }

                ImageBitmapConfig.F16 -> {
                    4
                }

                else -> {
                    4
                }
            }
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