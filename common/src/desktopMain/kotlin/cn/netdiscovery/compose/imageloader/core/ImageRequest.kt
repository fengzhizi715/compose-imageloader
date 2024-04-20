package cn.netdiscovery.compose.imageloader.core

import cn.netdiscovery.compose.imageloader.transform.Transformer
import java.io.File

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.core.ImageRequest
 * @author: Tony Shen
 * @date: 2024/4/19 23:25
 * @version: V1.0 <描述当前版本功能>
 */
class ImageRequest {
    internal var ua: String? = null
    internal var url: String? = null
    internal var file: File? = null
    internal var useCache:Boolean = true

    internal var saveStrategy = SaveStrategy.ORIGINAL
    internal var transformers = mutableListOf<Transformer>()

    companion object{
        fun create(): ImageRequest = ImageRequest()
    }

    fun ua(ua:String): ImageRequest {
        this.ua = ua
        return this
    }

    fun url(url: String): ImageRequest {
        this.url = url
        return this
    }

    fun file(file: File): ImageRequest {
        this.file = file
        return this
    }

    fun useCache(useCache: Boolean): ImageRequest {
        this.useCache = useCache
        return this
    }

    fun saveStrategy(strategy: SaveStrategy): ImageRequest {
        saveStrategy = strategy
        return this
    }

    fun transformations(transformations: List<Transformer>?): ImageRequest {
        if (!transformations.isNullOrEmpty()) {
            transformers.addAll(transformations)
        }
        return this
    }

    suspend fun request(): ImageResponse {
        return try {
            ImageLoaderFactory.enqueue(this)
        } catch (e: Exception) {
            ImageResponse(null, e)
        }
    }
}