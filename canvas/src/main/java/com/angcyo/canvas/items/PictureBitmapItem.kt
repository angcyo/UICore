package com.angcyo.canvas.items

import android.graphics.Bitmap
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.withPicture

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
class PictureBitmapItem : PictureItem() {

    /**绘制的图片*/
    var bitmap: Bitmap? = null

    val bitmapBounds = emptyRectF()

    init {
        itemName = "Bitmap"
    }

    override fun updatePictureDrawable(resetSize: Boolean) {
        bitmap?.let { bitmap ->
            val itemWidth = bitmap.width
            val itemHeight = bitmap.height

            bitmapBounds.set(0f, 0f, itemWidth.toFloat(), itemHeight.toFloat())
            val drawable = ScalePictureDrawable(withPicture(itemWidth, itemHeight) {
                drawBitmap(bitmap, null, bitmapBounds, paint)
            })

            this.drawable = drawable
            this.itemWidth = bitmapBounds.width()
            this.itemHeight = bitmapBounds.height()
        }
    }
}