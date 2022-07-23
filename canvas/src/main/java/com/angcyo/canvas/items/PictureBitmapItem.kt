package com.angcyo.canvas.items

import android.graphics.Bitmap
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.withPicture

/**
 * 用来渲染[bitmap]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
class PictureBitmapItem : PictureItem() {

    /**原始的图片, 未修改前的数据*/
    var originBitmap: Bitmap? = null

    /**绘制的图片, 可能是修改后的数据*/
    var bitmap: Bitmap? = null

    //记录图片的真实bounds
    val bitmapBounds = emptyRectF()

    init {
        itemLayerName = "Bitmap"
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