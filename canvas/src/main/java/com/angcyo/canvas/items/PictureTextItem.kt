package com.angcyo.canvas.items

import android.graphics.Paint

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/21
 */
class PictureTextItem : DrawableItem() {

    /**需要绘制的文本*/
    var text: String? = null

    /**字体样式*/
    var textStyle: Int = TextItem.TEXT_STYLE_NONE

    /**更新[textStyle]*/
    fun updatePaintStyle(paint: Paint) {
        paint.apply {
            isStrikeThruText = textStyle.isDeleteLine
            isUnderlineText = textStyle.isUnderLine
            isFakeBoldText = textStyle.isTextBold
            textSkewX = if (textStyle.isTextItalic) TextItem.ITALIC_SKEW else 0f
            //typeface =
        }
    }
}