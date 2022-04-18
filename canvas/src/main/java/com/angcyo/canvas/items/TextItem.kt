package com.angcyo.canvas.items

import com.angcyo.library.ex.have

/**
 * 文本组件数据
 * [TextItemRenderer]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class TextItem : BaseItem() {

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
    }

    /**需要绘制的文本*/
    var text: String? = null

    /**字体样式*/
    var textStyle: Int = TEXT_STYLE_NONE

    val isTextBold: Boolean
        get() = textStyle.have(TEXT_STYLE_BOLD)

    val isUnderLine: Boolean
        get() = textStyle.have(TEXT_STYLE_UNDER_LINE)

    val isDeleteLine: Boolean
        get() = textStyle.have(TEXT_STYLE_DELETE_LINE)

    val isTextItalic: Boolean
        get() = textStyle.have(TEXT_STYLE_ITALIC)

}