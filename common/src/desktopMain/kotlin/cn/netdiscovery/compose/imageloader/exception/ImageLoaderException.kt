package cn.netdiscovery.compose.imageloader.exception

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.exception.ImageLoaderException
 * @author: Tony Shen
 * @date: 2024/4/20 21:26
 * @version: V1.0 <描述当前版本功能>
 */
class ImageLoaderException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(throwable: Throwable?) : super(throwable)
    constructor(message: String?, throwable: Throwable?) : super(message, throwable)
}