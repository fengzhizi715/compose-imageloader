package cn.netdiscovery.compose.imageloader

import androidx.compose.runtime.*
import cn.netdiscovery.compose.imageloader.core.*
import cn.netdiscovery.compose.imageloader.transform.Transformer
import cn.netdiscovery.compose.imageloader.utils.extension.transformationKey
import java.io.File

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.ImageLoader
 * @author: Tony Shen
 * @date: 2024/4/16 23:26
 * @version: V1.0 <描述当前版本功能>
 */
@Composable
actual fun imageUrl(url: String, transformations: List<Transformer>?, imageCallback: ImageCallback) {
    val key = url + transformations?.transformationKey()
    imageSuspendLoad(key, imageCallback) {
        ImageRequest.create()
            .url(url)
            .transformations(transformations)
            .saveStrategy(SaveStrategy.ORIGINAL)
            .request()
    }
}

@Composable
actual fun imageFile(filePath: String, transformations: List<Transformer>?, imageCallback: ImageCallback) {
    val key = filePath + transformations?.transformationKey()
    imageSuspendLoad(key, imageCallback) {
        ImageRequest.create()
            .file(File(filePath))
            .transformations(transformations)
            .saveStrategy(SaveStrategy.ORIGINAL)
            .request()
    }
}


@Composable
private fun imageSuspendLoad(key: String,
                             imageCallback: ImageCallback,
                             block: suspend () -> ImageResponse) {
    var imageResponse by remember { mutableStateOf(defaultResponse) }

    LaunchedEffect(key) {
        imageResponse = defaultResponse
        imageResponse = block()
    }

    if (imageResponse.exception != null) {
        imageCallback.errorView?.invoke()
    } else {
        val painter = imageResponse.imagePainter
        if (painter != null) {
            imageCallback.imageView.invoke(painter)
        } else {
            if (imageResponse.isLoading) {
                imageCallback.placeHolderView?.invoke()
            } else {
                imageCallback.errorView?.invoke()
            }
        }
    }
}