package cn.netdiscovery.compose.imageloader.transform

import androidx.compose.ui.graphics.Color

object TransformationTag {
    const val CenterCropTransformation = "CenterCropTransformation"
    const val CircleCropTransformation = "CircleCropTransformation"
    const val ResizeTransformation     = "ResizeTransformation"
}

class Transformation {
    private val transformers = mutableListOf<Transformer>()

    fun toList(): List<Transformer> {
        return transformers
    }

    fun centerCrop(width: Int, height: Int): Transformation {
        if (transformers.any { it.tag() == TransformationTag.CenterCropTransformation }) {
            transformers.removeIf {
                it.tag() == TransformationTag.CenterCropTransformation
            }
        }
        transformers.add(CenterCropTransformation(width, height))
        return this
    }

    fun circleCrop(
        strokeWidth: Float? = null, strokeColor: Color? = null, backgroundColor: Color? = null
    ): Transformation {
        if (transformers.any { it.tag() == TransformationTag.CircleCropTransformation }) {
            transformers.removeIf {
                it.tag() == TransformationTag.CircleCropTransformation
            }
        }
        transformers.add(CircleCropTransformation(strokeWidth, strokeColor, backgroundColor))
        return this
    }

    fun resize(width: Int, height: Int): Transformation {
        if (transformers.any { it.tag() == TransformationTag.ResizeTransformation }) {
            transformers.removeIf {
                it.tag() == TransformationTag.ResizeTransformation
            }
        }
        transformers.add(ResizeTransformation(width, height))
        return this
    }

    fun none(): Transformation {
        return this
    }
}
