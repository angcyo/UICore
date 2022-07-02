package com.angcyo.canvas.items

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextPaint
import com.angcyo.canvas.utils.*
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.uuid

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
abstract class BaseItem : ICanvasItem {

    /**唯一标识符*/
    var uuid: String = uuid()

    /**标签, 自定义的属性, 用来区分数据*/
    var tag: String? = null

    /**用来存放自定义的数据*/
    var data: Any? = null

    /**自身实际的宽*/
    var itemWidth: Float = 0f

    /**自身实际的高*/
    var itemHeight: Float = 0f

    /**绘制的画笔属性*/
    var paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }

    /**字体样式*/
    var textStyle: Int = PictureTextItem.TEXT_STYLE_NONE

    /**额外存储的数据, 支持回退栈管理*/
    var holdData: Map<String, Any?>? = null

    /**更新画笔*/
    open fun updatePaint(paint: TextPaint = this.paint) {
        this.paint = paint
        paint.let {
            it.isStrikeThruText = textStyle.isDeleteLine
            it.isUnderlineText = textStyle.isUnderLine
            it.isFakeBoldText = textStyle.isTextBold
            it.textSkewX = if (textStyle.isTextItalic) PictureTextItem.ITALIC_SKEW else 0f
            //it.typeface = paintTypeface
            //it.textAlign = paintAlign
            //it.style = paintStyle
        }
    }

    /**图层预览的名称*/
    override var itemName: CharSequence? = null

    /**图层预览的图形*/
    override var itemDrawable: Drawable? = null
}

/**获取额外存储的数据
 * [com.angcyo.canvas.items.BaseItem.holdData]*/
inline fun <reified T> BaseItem.getHoldData(key: String): T? {
    val any = holdData?.get(key)
    if (any is T) {
        return any
    }
    return null
}

/**设置额外的数据存储*/
fun <T> BaseItem.setHoldData(key: String, value: T?) {
    if (holdData == null) {
        holdData = hashMapOf()
    }
    var data = holdData
    if (data is MutableMap<String, Any?>) {
        //no
    } else {
        holdData = hashMapOf<String, Any?>().apply {
            putAll(data!!)
        }
        data = holdData
    }

    if (data is MutableMap<String, Any?>) {
        data.put(key, value)
    }
}