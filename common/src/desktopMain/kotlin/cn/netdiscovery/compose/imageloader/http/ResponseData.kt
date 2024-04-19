package cn.netdiscovery.compose.imageloader.http

import java.io.File

enum class ImageType(val value: String) {
    PNG("png"),
    JPG("jpg"),
    WEBP("webp")
}

data class ResponseData(
    val contentType: String,
    val contentLength: Int = 0,
    val contentSnapshot: File
) {
    private var imageType: ImageType? = null

    init {
        imageType = when (contentType) {
            "image/png" -> ImageType.PNG
            "image/jpeg", "image/jpg" -> ImageType.JPG
            "image/webp" -> ImageType.WEBP
            else -> null
        }
    }

    fun isSupportImage(): Boolean {
        return imageType != null
    }
}