package com.angcyo.canvas.render.element

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.ElementHitComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.*
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.SupportUndo
import com.angcyo.library.component.hawk.HawkPropertyValue
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex._color
import com.angcyo.library.ex.createOverrideBitmapCanvas
import com.angcyo.library.ex.createOverrideMatrix
import com.angcyo.library.ex.createOverridePictureCanvas
import com.angcyo.library.ex.createTextPaint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.translateToOrigin
import com.angcyo.library.unit.toPixel

/**
 * 元素的基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
abstract class BaseElement : IElement {

    @Keep
    companion object {
        var elementNoDataTip: String by HawkPropertyValue<Any, String>("No Data!")
    }

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

    /**错误提示画笔*/
    protected val tipPaint: Paint by lazy {
        createTextPaint(_color(R.color.error))
    }

    //region---core---

    /**在更新[updateElementAction]之前调用*/
    open fun onUpdateElementBefore() {

    }

    /**
     * 更新元素, 并且支持回退
     * [onUpdateElementBefore]
     * [onUpdateElementAfter]*/
    @SupportUndo
    override fun updateElementAction(
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

    /**在更新[updateElementAction]之后调用*/
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

    /**无数据时, 渲染提示信息.
     * 在元素0,0的位置绘制
     * */
    fun renderNoData(canvas: Canvas, params: RenderParams?) {
        val property = renderProperty
        val renderBounds = property.getRenderBounds()

        canvas.withTranslation(-renderBounds.left, -renderBounds.top) {
            val scale = params?.delegate?.renderViewBox?.getScale() ?: 1f
            val width = 1 * dp
            //绘制一根线, 从左上角到右下角
            tipPaint.strokeWidth = width / scale
            tipPaint.style = Paint.Style.STROKE
            canvas.drawRect(renderBounds, tipPaint)
            tipPaint.style = Paint.Style.FILL
            canvas.drawLine(
                renderBounds.left,
                renderBounds.top,
                renderBounds.right,
                renderBounds.bottom,
                tipPaint
            )
            //绘制一根线, 从右上角到左下角
            canvas.drawLine(
                renderBounds.right,
                renderBounds.top,
                renderBounds.left,
                renderBounds.bottom,
                tipPaint
            )
            tipPaint.strokeWidth = width
            val text = elementNoDataTip
            tipPaint.textSize = 12 * dp / scale
            val x = renderBounds.centerX() - tipPaint.textWidth(text) / 2
            val y = renderBounds.centerY() + tipPaint.textHeight() / 2
            canvas.drawText(text, x, y, tipPaint)
        }
    }

    //endregion---core---

    //region---方法---

    /**更新原始数据的宽高, 并且保持看起来的宽高一直.
     * 直接更新渲染属性, 此时bean的数据可能还未更新
     * [keepVisibleSize] 是否要保持可见的大小一致
     * [newWidth] 新的渲染宽度
     * [newHeight] 新的渲染高度
     * */
    @Pixel
    open fun updateRenderWidthHeight(
        @Pixel newWidth: Float,
        @Pixel newHeight: Float,
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
     * [overrideSize] 等比输出到这个大小
     *
     * [offsetWidth] [offsetHeight] 宽高增益量
     * */
    protected fun createPictureCanvas(
        overrideSize: Float?,
        @Pixel
        minWidth: Float = 1f,
        @Pixel
        minHeight: Float = 1f,
        @Pixel
        offsetWidth: Float = 0f,
        @Pixel
        offsetHeight: Float = 0f,
        block: Canvas.() -> Unit
    ): Picture {
        //原始目标需要绘制的大小
        val bounds = acquireTempRectF()
        renderProperty.getRenderBounds(bounds)
        val originWidth = bounds.width() + offsetWidth
        val originHeight = bounds.height() + offsetHeight
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

    /**
     * [createPictureDrawable]
     * [overrideSize] 需要覆盖输出的宽度
     * [offsetWidth] [offsetHeight] 额外增加的宽高 */
    protected fun createPictureDrawable(
        overrideSize: Float?,
        @Pixel
        minWidth: Float = 1f, /*最小宽度*/
        @Pixel
        minHeight: Float = 1f,
        @Pixel
        offsetWidth: Float = 0f,
        @Pixel
        offsetHeight: Float = 0f,
        block: Canvas.() -> Unit
    ): Drawable {
        return PictureRenderDrawable(
            createPictureCanvas(
                overrideSize,
                minWidth,
                minHeight,
                offsetWidth,
                offsetHeight,
                block
            )
        )
    }

    /**[createPictureDrawable]
     *
     * [com.angcyo.canvas.render.renderer.CanvasGroupRenderer.Companion.renderRenderer]
     * */
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
        val params = renderParams ?: RenderParams()
        return createPictureDrawable(
            overrideSize,
            params.drawMinWidth,
            params.drawMinHeight,
            params.drawOffsetWidth,
            params.drawOffsetHeight
        ) {
            //translate(params.drawOffsetWidth / 2, params.drawOffsetHeight / 2)
            block()
        }
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