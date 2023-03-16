package com.angcyo.canvas.render.element

import android.graphics.*
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.PictureRenderDrawable
import com.angcyo.canvas.render.util.RenderHelper
import com.angcyo.canvas.render.util.createOverridePictureCanvas
import com.angcyo.canvas.render.util.createRenderTextPaint
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.contains
import com.angcyo.library.ex.intersect

/**
 * 元素的基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
abstract class BaseElement : IElement {

    /**画笔*/
    var paint = createRenderTextPaint()

    /**描述的渲染属性, 包含坐标/缩放/倾斜信息*/
    var renderProperty = CanvasRenderProperty()

    /**需要在界面上渲染的[Drawable]*/
    var renderDrawable: Drawable? = null

    //region---core---

    override fun requestElementRenderProperty(): CanvasRenderProperty = renderProperty

    override fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable? =
        renderDrawable

    override fun elementContainsPoint(point: PointF): Boolean {
        var result = getElementBoundsPath().contains(point)
        if (!result) {
            val tempRect = RectF()
            tempRect.set(point.x, point.y, point.x + 1, point.y + 1)
            result = elementIntersectRect(tempRect) //此时使用1像素的矩形,进行碰撞
        }
        return result
    }

    override fun elementContainsRect(rect: RectF): Boolean = getElementBoundsPath().contains(rect)

    override fun elementIntersectRect(rect: RectF): Boolean = getElementBoundsPath().intersect(rect)

    override fun updateElementRenderProperty(property: CanvasRenderProperty) {
        property.copyTo(renderProperty)
    }

    //endregion---core---

    //region---方法---

    /**获取元素用来碰撞检测的[Path]范围*/
    fun getElementBoundsPath(result: Path = Path()): Path {
        val property = renderProperty
        val renderMatrix = property.getRenderMatrix(includeRotate = true)
        val rect = RectF(0f, 0f, property.width, property.height)
        result.rewind()
        result.addRect(rect, Path.Direction.CW)
        result.transform(renderMatrix)
        return result
    }

    /**更新原始数据的宽高, 并且保持看起来的宽高一直
     * [keepVisibleSize] 是否要保持可见的大小一致*/
    @Pixel
    fun updateOriginWidthHeight(
        newWidth: Float,
        newHeight: Float,
        keepVisibleSize: Boolean = true
    ) {
        if (newWidth == 0f || newHeight == 0f) {
            return
        }
        val oldWidth = renderProperty.width
        val oldHeight = renderProperty.height

        renderProperty.width = newWidth
        renderProperty.height = newHeight

        if (keepVisibleSize && oldWidth > 0 && oldHeight > 0) {
            renderProperty.scaleX *= oldWidth / newWidth
            renderProperty.scaleY *= oldHeight / newHeight
        }
    }

    /**创建一个输出指定大小的[Canvas] [Picture]
     * [overrideSize] 等比输出到这个大小*/
    protected fun createOverrideCanvas(overrideSize: Float?, block: Canvas.() -> Unit): Picture {
        //原始目标需要绘制的大小
        val bounds = renderProperty.getRenderBounds()
        val originWidth = bounds.width()
        val originHeight = bounds.height()
        return createOverridePictureCanvas(
            originWidth,
            originHeight,
            overrideSize,
            null,
            block
        )
    }

    /** [overrideWidth] [overrideHeight] 需要覆盖输出的宽度 */
    protected fun createPictureDrawable(overrideSize: Float?, block: Canvas.() -> Unit): Drawable {
        return PictureRenderDrawable(createOverrideCanvas(overrideSize, block))
    }

    /**[createPictureDrawable]*/
    protected fun createPictureDrawable(
        renderParams: RenderParams?,
        block: Canvas.() -> Unit
    ): Drawable {
        return createPictureDrawable(renderParams?.overrideSize, block)
    }

    /**根据当前的属性, 绘制一个[bitmap]
     * [overrideWidth] [overrideHeight] 需要覆盖输出的宽度
     * */
    protected fun createBitmapDrawable(
        bitmap: Bitmap,
        paint: Paint,
        overrideSize: Float?
    ): Drawable {
        return createPictureDrawable(overrideSize) {
            val renderMatrix = renderProperty.getDrawMatrix(includeRotate = true)
            drawBitmap(bitmap, renderMatrix, paint)
        }
    }

    /**[createBitmapDrawable] */
    protected fun createPathDrawable(
        pathList: List<Path>?,
        paint: Paint,
        overrideSize: Float?
    ): Drawable {
        return createPictureDrawable(overrideSize) {
            if (pathList.isNullOrEmpty()) {
                //
            } else {
                val renderMatrix = renderProperty.getDrawMatrix(includeRotate = true)
                val newPathList = RenderHelper.translateToOrigin(pathList)
                for (path in newPathList!!) {
                    path.transform(renderMatrix)
                    drawPath(path, paint)
                }
            }
        }
    }

    //endregion---方法---

}