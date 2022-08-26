package com.angcyo.canvas.items

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.withPicture

/**
 * [PictureDrawable]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
open class PictureItem : DrawableItem() {

    init {
        dataType = CanvasConstant.DATA_TYPE_BITMAP
        dataMode = CanvasConstant.DATA_MODE_GREY
    }

    override fun updateItem(paint: Paint) {
        super.updateItem(paint)
    }

    override fun updateDrawable(drawable: Drawable?) {
        super.updateDrawable(drawable)
        drawable?.let {
            updatePictureDrawable(it)
        }
    }

    /**更新Drawable 使用[ScalePictureDrawable]*/
    fun updatePictureDrawable(drawable: Drawable) {
        val width = drawable.minimumWidth
        val height = drawable.minimumHeight

        val pictureDrawable = ScalePictureDrawable(withPicture(width, height) {
            drawable.setBounds(0, 0, width, height)
            drawable.draw(this)
        })

        this.drawable = pictureDrawable
        this.itemWidth = width.toFloat()
        this.itemHeight = height.toFloat()
    }

}