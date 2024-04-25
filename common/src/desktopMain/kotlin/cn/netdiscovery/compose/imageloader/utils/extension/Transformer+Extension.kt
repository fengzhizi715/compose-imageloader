package cn.netdiscovery.compose.imageloader.utils.extension

import cn.netdiscovery.compose.imageloader.transform.Transformer

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.utils.`Transformer+Extension`
 * @author: Tony Shen
 * @date: 2024/4/19 17:21
 * @version: V1.0 <描述当前版本功能>
 */
internal fun List<Transformer>?.transformationKey(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }

    return this.joinToString("-") {
        it.javaClass.simpleName
    }
}

internal fun List<Transformer>?.prettyDisplay(): String {
    if (this.isNullOrEmpty()) {
        return "[]"
    }

    return "["+this.joinToString(",") {
        it.javaClass.simpleName
    }+"]"
}