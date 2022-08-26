package com.angcyo.canvas.items

import android.graphics.Bitmap
import android.graphics.Paint
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
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

    /**预览的图片*/
    var previewBitmap: Bitmap? = null

    //记录图片的真实bounds
    val bitmapBounds = emptyRectF()

    init {
        itemLayerName = "Bitmap"
        dataType = CanvasConstant.DATA_TYPE_BITMAP
        dataMode = CanvasConstant.DATA_MODE_GREY
    }

    /**更新Drawable*/
    override fun updateItem(paint: Paint) {
        val b = /*previewBitmap ?: */bitmap
        if (b == null) {
            super.updateItem(paint)
        } else {
            val itemWidth = b.width
            val itemHeight = b.height

            bitmapBounds.set(0f, 0f, itemWidth.toFloat(), itemHeight.toFloat())
            val drawable = ScalePictureDrawable(withPicture(itemWidth, itemHeight) {
                drawBitmap(b, null, bitmapBounds, null)
            })

            this.drawable = drawable
            this.itemWidth = bitmapBounds.width()
            this.itemHeight = bitmapBounds.height()
        }
    }
}