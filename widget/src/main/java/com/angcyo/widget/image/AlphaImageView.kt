package com.angcyo.widget.image

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.angcyo.drawable.CheckerboardDrawable

/**
 * 透明图片显示
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/15
 */
class AlphaImageView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatImageView(context, attributeSet) {

    /**透明棋盘*/
    var alphaDrawable: Drawable? = null

    init {
        alphaDrawable = CheckerboardDrawable.create()
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable != null) {
            alphaDrawable?.draw(canvas)
        }
        super.onDraw(canvas)
    }
}