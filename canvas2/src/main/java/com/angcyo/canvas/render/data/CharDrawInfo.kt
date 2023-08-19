package com.angcyo.canvas.render.data

import android.graphics.RectF
import com.angcyo.library.ex.getOutlineRect
import com.angcyo.library.ex.getOutlineWidthHeight

/**
 * 每个字符绘制的信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/08/17
 */
data class CharDrawInfo(
    /**需要绘制的字符*/
    val char: String,
    /**字符自身的宽高*/
    val charWidth: Float,
    val charHeight: Float,
    /**字符边界位置, 0,0开始*/
    val bounds: RectF,
    /**文本绘制时, 需要偏移的量. 只在绘制的时候生效*/
    val charDrawOffsetX: Float,
    val charDrawOffsetY: Float,
    /**所在的列索引*/
    val columnIndex: Int,
    /**所在的行索引*/
    val lineIndex: Int,
    /**当前字符所在的整体行高/行宽*/
    val lineWidth: Float,
    val lineHeight: Float,
    /**当前行, 文本下沉的距离*/
    val lineDescent: Float,
    //---
    /**曲线文本时, 当前的字符应该旋转到的目标角度*/
    var _curveAngle: Float = 0f,
)

/**返回对应的矩形*/
fun List<CharDrawInfo>.getOutlineRect(): RectF = map { it.bounds }.getOutlineRect()

/**返回对应的宽高*/
fun List<CharDrawInfo>.getCharTextWidthHeight(): Pair<Float, Float> =
    map { it.bounds }.getOutlineWidthHeight()