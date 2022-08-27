package com.angcyo.canvas.items

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.withPicture

/**
 * 用来渲染[bitmap]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
class PictureBitmapItem(
    /**原始的图片, 未修改前的数据*/
    var originBitmap: Bitmap,
    /**算法修改后的图片*/
    var modifyBitmap: Bitmap? = null,
    /**预览的Drawable, 可以实现障眼法*/
    var previewDrawable: Drawable? = null
) : PictureDrawableItem() {

    init {
        itemLayerName = "Bitmap"
        dataType = CanvasConstant.DATA_TYPE_BITMAP
        dataMode = CanvasConstant.DATA_MODE_GREY
    }

    /**更新Drawable*/
    override fun updateItem(paint: Paint) {
        val _modifyBitmap = modifyBitmap
        if (_modifyBitmap == null) {
            //没有修改后的图片, 则使用原图预览
            val width = originBitmap.width
            val height = originBitmap.height
            val bitmapDrawable = ScalePictureDrawable(withPicture(width, height) {
                drawBitmap(
                    originBitmap,
                    null,
                    RectF(0f, 0f, width.toFloat(), height.toFloat()),
                    null
                )
            })
            updateDrawable(bitmapDrawable)
        } else {
            //有修改后的图片则使用
            val width = _modifyBitmap.width
            val height = _modifyBitmap.height
            val bitmapDrawable = ScalePictureDrawable(withPicture(width, height) {
                drawBitmap(
                    _modifyBitmap,
                    null,
                    RectF(0f, 0f, width.toFloat(), height.toFloat()),
                    null
                )
            })
            updateDrawable(bitmapDrawable)
        }

        if (previewDrawable != null) {
            //强制指定了预览的drawable
            drawable = previewDrawable
        }
    }
}