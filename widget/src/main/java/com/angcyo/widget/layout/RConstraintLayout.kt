package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.angcyo.widget.R

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
    val layoutDelegate = RLayoutDelegate()

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RConstraintLayout)
        layoutDelegate.initAttribute(this, attributeSet)

        typedArray.recycle()
    }

    override fun draw(canvas: Canvas) {
        layoutDelegate.maskLayout(canvas) {
            layoutDelegate.draw(canvas)
            super.draw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        layoutDelegate.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}