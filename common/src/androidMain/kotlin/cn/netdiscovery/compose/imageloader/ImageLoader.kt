package cn.netdiscovery.compose.imageloader

import androidx.compose.runtime.Composable
import cn.netdiscovery.compose.imageloader.core.ImageCallback
import cn.netdiscovery.compose.imageloader.transform.Transformer

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.ImageLoader
 * @author: Tony Shen
 * @date: 2024/4/16 23:30
 * @version: V1.0 <描述当前版本功能>
 */
@Composable
actual fun imageUrl(url: String, transformations: List<Transformer>?, imageCallback: ImageCallback) {
}

@Composable
actual fun imageFile(filePath: String, transformations: List<Transformer>?, imageCallback: ImageCallback) {

}