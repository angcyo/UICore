package com.angcyo.canvas.render.element

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.data.RenderParams

/**
 * 用来绘制[Bitmap]元素的对象
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
open class BitmapElement : BaseElement() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**1:1 原始图片*/
    var originBitmap: Bitmap? = null

    /**[originBitmap]1:1修改后渲染的图片, 界面上看到的图片*/
    var renderBitmap: Bitmap? = null

    override fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable? {
        val bitmap = renderBitmap ?: originBitmap ?: return null
        return createBitmapDrawable(
            bitmap,
            paint,
            renderParams?.overrideWidth,
            renderParams?.overrideHeight
        )
    }

    /**使用[bitmap]初始化对象*/
    fun initOriginBitmap(bitmap: Bitmap) {
        this.originBitmap = bitmap
        renderProperty.apply {
            width = bitmap.width.toFloat()
            height = bitmap.height.toFloat()
        }
    }

}