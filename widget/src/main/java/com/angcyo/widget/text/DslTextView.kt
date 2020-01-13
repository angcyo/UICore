package com.angcyo.widget.text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.angcyo.widget.drawable.DslAttrBadgeDrawable

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */
open class DslTextView : DslScrollTextView {

    /**角标绘制*/
    var dslBadeDrawable = DslAttrBadgeDrawable()

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        dslBadeDrawable.initAttribute(context, attributeSet)
        dslBadeDrawable.callback = this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        dslBadeDrawable.apply {
            setBounds(0, 0, measuredWidth, measuredHeight)
            draw(canvas)
        }
    }
}