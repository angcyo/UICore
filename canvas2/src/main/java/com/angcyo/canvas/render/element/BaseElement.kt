package com.angcyo.canvas.render.element

import android.graphics.*
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.ElementHitComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.*
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.unit.toPixel

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

    override var elementHitComponent: ElementHitComponent = ElementHitComponent(this)

    //region---core---

    override fun requestElementRenderProperty(): CanvasRenderProperty = renderProperty

    override fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable? =
        renderDrawable

    override fun updateElementRenderProperty(property: CanvasRenderProperty) {
        property.copyTo(renderProperty)
    }

    //endregion---core---

    //region---方法---

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

    protected fun getOverrideMatrix(
        overrideWidth: Float?,
        overrideHeight: Float? = null
    ): Matrix {
        //原始目标需要绘制的大小
        val bounds = renderProperty.getRenderBounds()
        val originWidth = bounds.width()
        val originHeight = bounds.height()
        return createOverrideMatrix(
            originWidth,
            originHeight,
            overrideWidth,
            overrideHeight
        )
    }

    /**创建一个输出指定大小的[Canvas] [Picture]
     * [overrideSize] 等比输出到这个大小*/
    protected fun createOverrideCanvas(
        overrideSize: Float?,
        @Pixel
        minWidth: Float = 1f,
        @Pixel
        minHeight: Float = 1f,
        block: Canvas.() -> Unit
    ): Picture {
        //原始目标需要绘制的大小
        val bounds = renderProperty.getRenderBounds()
        val originWidth = bounds.width()
        val originHeight = bounds.height()
        return createOverridePictureCanvas(
            originWidth,
            originHeight,
            overrideSize,
            null,
            minWidth,
            minHeight,
            block
        )
    }

    /** [overrideWidth] [overrideHeight] 需要覆盖输出的宽度 */
    protected fun createPictureDrawable(
        overrideSize: Float?,
        @Pixel
        minWidth: Float = 1f, /*最小宽度*/
        @Pixel
        minHeight: Float = 1f,
        block: Canvas.() -> Unit
    ): Drawable {
        return PictureRenderDrawable(createOverrideCanvas(overrideSize, minWidth, minHeight, block))
    }

    /**[createPictureDrawable]*/
    protected fun createPictureDrawable(
        renderParams: RenderParams?,
        block: Canvas.() -> Unit
    ): Drawable {
        return createPictureDrawable(
            renderParams?.overrideSize,
            (renderParams ?: RenderParams()).drawMinWidth,
            (renderParams ?: RenderParams()).drawMinHeight,
            block
        )
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

    /**[createBitmapDrawable]
     * [isLinePath] 是否线段
     * */
    protected fun createPathDrawable(
        pathList: List<Path>?,
        paint: Paint,
        overrideSize: Float?,
        @Pixel
        minWidth: Float, /*最小宽度*/
        @Pixel
        minHeight: Float,
        isLinePath: Boolean
    ): Drawable {
        return createPictureDrawable(overrideSize, minWidth, minHeight) {
            if (pathList.isNullOrEmpty()) {
                //
            } else {
                val renderMatrix = renderProperty.getDrawMatrix(includeRotate = true)
                val newPathList = pathList.translateToOrigin()

                val oldStyle = paint.style
                val oldPathEffect = paint.pathEffect
                for (path in newPathList!!) {
                    path.transform(renderMatrix)
                    if (isLinePath) {
                        //画线必须使用STROKE模式, 否则画不出
                        paint.style = Paint.Style.STROKE

                        if (oldStyle == Paint.Style.STROKE) {
                            //描边的线段, 使用虚线绘制
                            paint.pathEffect = createDashPathEffect() //虚线
                        } else {
                            paint.pathEffect = null //实线
                        }
                    }

                    //draw
                    drawPath(path, paint)
                }
                paint.style = oldStyle
                paint.pathEffect = oldPathEffect
            }
        }
    }

    /**创建一个虚线效果*/
    protected open fun createDashPathEffect(): PathEffect {
        val dashWidth = 1f.toPixel()
        val dashGap = dashWidth
        return DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
    }

    //endregion---方法---

}