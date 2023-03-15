package com.angcyo.canvas.render.element

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.BitmapStateStack
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.util.createRenderPaint

/**
 * 用来绘制[Bitmap]元素的对象
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
open class BitmapElement : BaseElement() {

    protected val paint = createRenderPaint()

    /**1:1 原始图片*/
    var originBitmap: Bitmap? = null

    /**[originBitmap]1:1修改后渲染的图片, 界面上看到的图片*/
    var renderBitmap: Bitmap? = null

    override fun createStateStack(renderer: BaseRenderer): IStateStack = BitmapStateStack(renderer)

    override fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable? {
        val bitmap = renderBitmap ?: originBitmap ?: return null
        return createBitmapDrawable(bitmap, paint, renderParams?.overrideSize)
    }

    /**更新原始的[bitmap]对象, 并保持可视化的宽高一致
     * [updateOriginWidthHeight]*/
    fun updateOriginBitmap(bitmap: Bitmap, keepVisibleSize: Boolean = true) {
        this.originBitmap = bitmap
        updateOriginWidthHeight(bitmap.width.toFloat(), bitmap.height.toFloat(), keepVisibleSize)
    }

}