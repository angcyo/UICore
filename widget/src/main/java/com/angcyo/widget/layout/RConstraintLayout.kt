package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.angcyo.widget.R
import com.angcyo.widget.base.InvalidateProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/26
 */
open class RConstraintLayout(
    context: Context,
    attributeSet: AttributeSet? = null
) : ConstraintLayout(context, attributeSet) {
    var bDrawable: Drawable? by InvalidateProperty(null)

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RConstraintLayout)
        bDrawable = typedArray.getDrawable(R.styleable.RConstraintLayout_r_background)
        typedArray.recycle()
    }

    override fun draw(canvas: Canvas) {
        bDrawable?.run {
            setBounds(0, 0, measuredWidth, measuredHeight)
            draw(canvas)
        }
        super.draw(canvas)
    }

}