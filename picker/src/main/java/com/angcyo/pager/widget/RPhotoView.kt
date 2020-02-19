package com.angcyo.pager.widget

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.chrisbanes.photoview.PhotoView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/23
 */

class RPhotoView : PhotoView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet?) : super(context, attr)
    constructor(context: Context, attr: AttributeSet?, defStyle: Int) : super(
        context,
        attr,
        defStyle
    )

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(event)
    }

    override fun getImageMatrix(): Matrix {
        return super.getImageMatrix()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
    }

    override fun setScale(scale: Float) {
        super.setScale(scale)
    }

    override fun setScale(scale: Float, animate: Boolean) {
        super.setScale(scale, animate)
    }
}