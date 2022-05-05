package com.angcyo.canvas.items

import android.graphics.drawable.PictureDrawable
import android.text.TextPaint

/**
 * [PictureDrawable]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
abstract class PictureItem : DrawableItem() {

    override fun updatePaint(paint: TextPaint) {
        super.updatePaint(paint)
        updatePictureDrawable()
    }

    /**重新更新[drawable]*/
    abstract fun updatePictureDrawable()

}