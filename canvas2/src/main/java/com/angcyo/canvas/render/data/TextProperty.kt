package com.angcyo.canvas.render.data

import android.graphics.Paint
import android.widget.LinearLayout
import com.angcyo.canvas.render.element.toAlignString
import com.angcyo.library.annotation.Pixel

/**
 * 文本属性
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/13
 */
data class TextProperty(

    /**需要绘制的文本*/
    var text: String? = null,

    /**字体名称*/
    var fontFamily: String? = null,

    /**文本绘制的方向*/
    var orientation: Int = LinearLayout.HORIZONTAL,

    /**字符之间的间隙*/
    @Pixel
    var charSpacing: Float = 0f,

    /**行之间的间隙*/
    @Pixel
    var lineSpacing: Float = 0f,

    /**是否是紧凑的文本, 所有字符会挨得更近*/
    var isCompactText: Boolean = true,

    /**删除线*/
    var isStrikeThruText: Boolean = false,

    /**下划线*/
    var isUnderlineText: Boolean = false,

    /**加粗*/
    var isFakeBoldText: Boolean = false,

    /**斜体*/
    var isItalic: Boolean = false,

    /**笔的样式*/
    var paintStyle: Paint.Style = Paint.Style.FILL,

    /**文本的对齐方式
     * [Paint.Align.toAlignString]
     * */
    var textAlign: String? = null,

    /**字体大小*/
    @Pixel
    var fontSize: Float = 20f,

    /**文本颜色*/
    var textColor: String? = null,

    )
