package com.angcyo.canvas.items

import android.graphics.Color
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.have

/**
 * 文本组件数据
 * [TextItemRenderer]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class TextItem : BaseItem() {

    companion object {

        /**字体样式, 无*/
        const val TEXT_STYLE_NONE = 0x00

        /**字体样式, 加粗*/
        const val TEXT_STYLE_BOLD = 0x01

        /**字体样式, 斜体*/
        const val TEXT_STYLE_ITALIC = 0x02

        /**字体样式, 下划线*/
        const val TEXT_STYLE_UNDER_LINE = 0x04

        /**字体样式, 删除线*/
        const val TEXT_STYLE_DELETE_LINE = 0x08

        /**斜体的倾斜角度*/
        const val ITALIC_SKEW = -0.25f
    }

    /**需要绘制的文本*/
    var text: String? = null

    /**字体样式*/
    var textStyle: Int = TEXT_STYLE_NONE

    var paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }

    /**更新[textStyle]*/
    fun updatePaintStyle() {
        paint.apply {
            isStrikeThruText = textStyle.isDeleteLine
            isUnderlineText = textStyle.isUnderLine
            isFakeBoldText = textStyle.isTextBold
            textSkewX = if (textStyle.isTextItalic) ITALIC_SKEW else 0f
            //typeface =
        }
    }
}

val Int.isTextBold: Boolean
    get() = have(TextItem.TEXT_STYLE_BOLD)

val Int.isUnderLine: Boolean
    get() = have(TextItem.TEXT_STYLE_UNDER_LINE)

val Int.isDeleteLine: Boolean
    get() = have(TextItem.TEXT_STYLE_DELETE_LINE)

val Int.isTextItalic: Boolean
    get() = have(TextItem.TEXT_STYLE_ITALIC)