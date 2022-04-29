package com.angcyo.canvas.items

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.ex.dp

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
abstract class BaseItem : ICanvasItem {

    /**自身实际的宽*/
    var itemWidth: Float = 0f

    /**自身实际的高*/
    var itemHeight: Float = 0f

    /**绘制的画笔属性*/
    var paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }

    var paintStyle: Paint.Style = Paint.Style.FILL

    /**文本对齐方法*/
    var paintAlign: Paint.Align = Paint.Align.LEFT

    /**字体*/
    var paintTypeface: Typeface? = Typeface.DEFAULT

    /**字体样式*/
    var textStyle: Int = PictureTextItem.TEXT_STYLE_NONE

    /**更新画笔*/
    open fun updatePaint(paint: TextPaint = this.paint) {
        this.paint = paint
        paint.let {
            it.isStrikeThruText = textStyle.isDeleteLine
            it.isUnderlineText = textStyle.isUnderLine
            it.isFakeBoldText = textStyle.isTextBold
            it.textSkewX = if (textStyle.isTextItalic) PictureTextItem.ITALIC_SKEW else 0f
            it.typeface = paintTypeface
            it.textAlign = paintAlign
            it.style = paintStyle
        }
    }

}