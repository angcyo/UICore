package com.angcyo.canvas.render.element

import android.graphics.*
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.ElementHitComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.*
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.SupportUndo
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.createOverrideBitmapCanvas
import com.angcyo.library.ex.createOverrideMatrix
import com.angcyo.library.ex.createOverridePictureCanvas
import com.angcyo.library.ex.translateToOrigin
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

    /**[Matrix]*/
    protected val RenderParams._renderMatrix
        get() = renderMatrix ?: renderProperty.getDrawMatrix(includeRotate = true)

    override var elementHitComponent: ElementHitComponent = ElementHitComponent(this)

    //region---core---

    /**
     * 更新元素, 并且支持回退
     * [onUpdateElementBefore]
     * [onUpdateElementAfter]*/
    @SupportUndo
    override fun updateElement(
        renderer: BaseRenderer?,
        delegate: CanvasRenderDelegate?,
        reason: Reason,
        block: IElement.() -> Unit
    ) {
        renderer ?: return
        //用来恢复的状态
        val undoState = createStateStack()
        undoState.saveState(renderer, delegate)
        onUpdateElementBefore()
        block()
        onUpdateElementAfter()
        val redoState = createStateStack()
        redoState.saveState(renderer, delegate)
        renderer.requestUpdatePropertyFlag(reason, delegate)
        delegate?.addStateToStack(renderer, undoState, redoState, reason = reason)
    }

    /**在更新[updateElement]之前调用*/
    open fun onUpdateElementBefore() {

    }

    /**在更新[updateElement]之后调用*/
    open fun onUpdateElementAfter() {

    }

    override fun requestElementRenderProperty(): CanvasRenderProperty = renderProperty

    override fun requestElementDrawable(
        renderer: BaseRenderer?,
        renderParams: RenderParams?
    ): Drawable? =
        renderDrawable ?: createPictureDrawable(renderParams) {
            onRenderInside(renderer, this, renderParams ?: RenderParams())
        }

    override fun requestElementBitmap(
        renderer: BaseRenderer?,
        renderParams: RenderParams?
    ): Bitmap? = createBitmapCanvas(renderParams) {
        onRenderInside(renderer, this, renderParams ?: RenderParams())
    }

    override fun updateElementRenderProperty(property: CanvasRenderProperty) {
        if (property != renderProperty) {
            property.copyTo(renderProperty)
        }
    }

    /**获取用来绘制的图片, 未经过[CanvasRenderProperty]处理的
     *
     * [createBitmapDrawable]
     * */
    open fun getDrawBitmap(): Bitmap? = null

    /**获取用来绘制的原始路径集合, 未经过[CanvasRenderProperty]处理的
     *
     * [createPathDrawable]
     * */
    open fun getDrawPathList(): List<Path>? = null

    //endregion---core---

    //region---方法---

    /**更新原始数据的宽高, 并且保持看起来的宽高一直
     * [keepVisibleSize] 是否要保持可见的大小一致*/
    @Pixel
    open fun updateOriginWidthHeight(
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
        val bounds = acquireTempRectF()
        renderProperty.getRenderBounds(bounds)
        val originWidth = bounds.width()
        val originHeight = bounds.height()
        bounds.release()
        return createOverrideMatrix(
            originWidth,
            originHeight,
            overrideWidth,
            overrideHeight
        )
    }

    /**创建一个输出指定大小的[Canvas] [Picture]
     * [overrideSize] 等比输出到这个大小*/
    protected fun createPictureCanvas(
        overrideSize: Float?,
        @Pixel
        minWidth: Float = 1f,
        @Pixel
        minHeight: Float = 1f,
        block: Canvas.() -> Unit
    ): Picture {
        //原始目标需要绘制的大小
        val bounds = acquireTempRectF()
        renderProperty.getRenderBounds(bounds)
        val originWidth = bounds.width()
        val originHeight = bounds.height()
        bounds.release()
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

    /**创建一个输出指定大小的[Canvas] [Bitmap]
     * [overrideSize] 等比输出到这个大小*/
    protected fun createBitmapCanvas(
        overrideSize: Float?,
        @Pixel
        minWidth: Float = 1f,
        @Pixel
        minHeight: Float = 1f,
        block: Canvas.() -> Unit
    ): Bitmap {
        //原始目标需要绘制的大小
        val bounds = acquireTempRectF()
        renderProperty.getRenderBounds(bounds)
        val originWidth = bounds.width()
        val originHeight = bounds.height()
        bounds.release()
        return createOverrideBitmapCanvas(
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
        return PictureRenderDrawable(
            createPictureCanvas(
                overrideSize,
                minWidth,
                minHeight,
                block
            )
        )
    }

    /**[createPictureDrawable]*/
    protected fun createPictureDrawable(
        renderParams: RenderParams?,
        block: Canvas.() -> Unit
    ): Drawable {
        var overrideSize = renderParams?.overrideSize
        if (renderParams?.overrideSizeNotZoomIn == true) {
            //需要阻止放大
            if (overrideSize != null) {
                val bounds = acquireTempRectF()
                renderProperty.getRenderBounds(bounds)
                val originWidth = bounds.width()
                val originHeight = bounds.height()
                bounds.release()

                if (overrideSize > originWidth || overrideSize > originHeight) {
                    overrideSize = null
                }
            }
        }

        return createPictureDrawable(
            overrideSize,
            (renderParams ?: RenderParams()).drawMinWidth,
            (renderParams ?: RenderParams()).drawMinHeight,
            block
        )
    }

    /**[createPictureDrawable]*/
    protected fun createBitmapCanvas(
        renderParams: RenderParams?,
        block: Canvas.() -> Unit
    ): Bitmap {
        var overrideSize = renderParams?.overrideSize
        if (renderParams?.overrideSizeNotZoomIn == true) {
            //需要阻止放大
            if (overrideSize != null) {
                val bounds = acquireTempRectF()
                renderProperty.getRenderBounds(bounds)
                val originWidth = bounds.width()
                val originHeight = bounds.height()
                bounds.release()

                if (overrideSize > originWidth || overrideSize > originHeight) {
                    overrideSize = null
                }
            }
        }

        return createBitmapCanvas(
            overrideSize,
            (renderParams ?: RenderParams()).drawMinWidth,
            (renderParams ?: RenderParams()).drawMinHeight,
            block
        )
    }

    /**渲染图片
     * [bitmap] 要绘制的原始数据*/
    protected fun renderBitmap(
        canvas: Canvas,
        paint: Paint,
        bitmap: Bitmap?,
        renderMatrix: Matrix
    ) {
        bitmap ?: return
        canvas.drawBitmap(bitmap, renderMatrix, paint)
    }

    /**渲染Path
     * [pathList] 要绘制的原始数据*/
    protected fun renderPath(
        canvas: Canvas,
        paint: Paint,
        isLinePath: Boolean,
        pathList: List<Path>?,
        renderMatrix: Matrix
    ) {
        pathList ?: return
        if (pathList.isEmpty()) return

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
            canvas.drawPath(path, paint)
        }
        paint.style = oldStyle
        paint.pathEffect = oldPathEffect
    }


    /**创建一个虚线效果*/
    protected open fun createDashPathEffect(): PathEffect {
        val dashWidth = 2f.toPixel()
        val dashGap = 1f.toPixel()
        return DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
    }

    //endregion---方法---

}