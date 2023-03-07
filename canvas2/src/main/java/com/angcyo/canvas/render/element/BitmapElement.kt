package com.angcyo.canvas.render.element

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.util.PictureRenderDrawable
import com.angcyo.canvas.render.util.withPicture
import com.angcyo.library.ex.ceilInt

/**
 * 用来绘制[Bitmap]元素的对象
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
open class BitmapElement : BaseElement() {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected var bitmap: Bitmap? = null

    override fun requestElementRenderDrawable(): Drawable? {
        val bitmap = bitmap ?: return null
        return PictureRenderDrawable(
            withPicture(
                renderProperty.width.ceilInt(),
                renderProperty.height.ceilInt()
            ) {
                val renderMatrix = renderProperty.getDrawMatrix(includeRotate = true)
                drawBitmap(bitmap, renderMatrix, paint)
            })
    }

    /**使用[bitmap]更新对象*/
    fun updateBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        renderProperty.apply {
            width = bitmap.width.toFloat()
            height = bitmap.height.toFloat()
        }
    }

}