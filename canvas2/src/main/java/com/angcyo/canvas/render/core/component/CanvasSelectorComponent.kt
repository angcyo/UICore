package com.angcyo.canvas.render.core.component

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.RenderFlag
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IComponent
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasElementRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.canvas.render.unit.IRenderUnit
import com.angcyo.canvas.render.util.canvasDecimal
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.canvas.render.util.createRenderTextPaint
import com.angcyo.library.component.DrawText
import com.angcyo.library.ex.*

/**
 * [CanvasElementRenderer]元素选中后的绘制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */
class CanvasSelectorComponent(val delegate: CanvasRenderDelegate) : CanvasGroupRenderer(),
    IComponent {

    companion object {

        /**多个元素时:是否要绘制子元素的边框*/
        const val RENDERER_FLAG_DRAW_ELEMENT_RECT = BaseRenderer.RENDERER_FLAG_LAST

        /**是否要绘制选中的边框*/
        const val RENDERER_FLAG_DRAW_SELECTOR_RECT = RENDERER_FLAG_DRAW_ELEMENT_RECT shl 1

        /**多个元素时:是否要绘制子元素的边框, 贴合的bounds*/
        const val RENDERER_FLAG_DRAW_ELEMENT_BOUNDS = RENDERER_FLAG_DRAW_SELECTOR_RECT shl 1

        /**是否要绘制选中的边框, 贴合的bounds*/
        const val RENDERER_FLAG_DRAW_SELECTOR_BOUNDS = RENDERER_FLAG_DRAW_ELEMENT_BOUNDS shl 1

        /**是否要绘制宽高*/
        const val RENDERER_FLAG_DRAW_FRAME_SIZE = RENDERER_FLAG_DRAW_SELECTOR_BOUNDS shl 1

        /**是否要绘制xy起始点*/
        const val RENDERER_FLAG_DRAW_FRAME_LOCATION = RENDERER_FLAG_DRAW_FRAME_SIZE shl 1

        /**是否要绘制旋转角度*/
        const val RENDERER_FLAG_DRAW_FRAME_ROTATE = RENDERER_FLAG_DRAW_FRAME_LOCATION shl 1

        /**最后一个标识位*/
        @RenderFlag
        const val RENDERER_FLAG_LAST = RENDERER_FLAG_DRAW_FRAME_ROTATE shl 1
    }

    override var isEnable: Boolean = true

    /**边框画笔*/
    val boundsPaint = createRenderPaint(_color(R.color.canvas_render_select), 1 * dp)

    /**元素边框的颜色*/
    var elementBoundsColor = _color(R.color.canvas_render_select)

    /**整体bounds的颜色*/
    var boundsColor = Color.RED

    /**文本画笔*/
    val textPaint = createRenderTextPaint(Color.WHITE)

    /**文字背景画笔*/
    val textBgPaint = createRenderPaint(_color(R.color.transparent50), style = Paint.Style.FILL)

    /**绘制文本时的padding值, 背景需要padding*/
    var textPadding = 2 * dp

    /**距离bounds的偏移量*/
    var textOffset = 2 * dp

    /**是否要解组Group中的元素,绘制Rect/Bounds*/
    var dissolveGroupElementDrawBounds = true

    /**是否有选中的元素*/
    val isSelectorElement: Boolean
        get() = rendererList.find { it.isVisible && !it.isLock } != null

    val valueUnit: IRenderUnit
        get() = delegate.axisManager.renderUnit

    /**如果只有1个元素, 则使用元素自身的属性
     * [updateLockScaleRatio]*/
    override val isLockScaleRatio: Boolean
        get() = if (rendererList.size() == 1) {
            rendererList.first().isLockScaleRatio
        } else {
            super.isLockScaleRatio
        }

    /**正常的元素Bounds范围*/
    protected val selectorRenderRect = RectF()
    protected val selectorRenderDrawRect = RectF()

    /**贴合元素的Bounds范围*/
    protected val selectorRenderBounds = RectF()
    protected val selectorRenderDrawBounds = RectF()

    init {
        //removeRenderFlag(RENDERER_FLAG_DRAW_ELEMENT_RECT, Reason.init, null)
        removeRenderFlag(RENDERER_FLAG_DRAW_ELEMENT_BOUNDS, Reason.init, null)
        //removeRenderFlag(RENDERER_FLAG_DRAW_SELECTOR_RECT, Reason.init, null)
        //removeRenderFlag(RENDERER_FLAG_DRAW_SELECTOR_BOUNDS, Reason.init, null)
        showSizeRender(Reason.init, null)
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        if (rendererList.size() == 1) {
            return rendererList.last().isSupportControlPoint(type)
        }
        return super.isSupportControlPoint(type)
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        if (isSelectorElement) {
            //绘制所有子元素的Rect

            if (getSingleRendererList().size() > 1) {
                //元素旋转的矩形
                if (renderFlags.have(RENDERER_FLAG_DRAW_ELEMENT_RECT)) {
                    boundsPaint.color = elementBoundsColor
                    drawElementRect(
                        canvas,
                        delegate.renderViewBox,
                        boundsPaint,
                        dissolveGroupElementDrawBounds
                    )
                }
                //bounds边界范围
                if (renderFlags.have(RENDERER_FLAG_DRAW_ELEMENT_BOUNDS)) {
                    boundsPaint.color = elementBoundsColor
                    drawElementBounds(
                        canvas,
                        delegate.renderViewBox,
                        boundsPaint,
                        dissolveGroupElementDrawBounds
                    )
                }
            }

            //绘制选择框的Rect
            renderProperty?.let { property ->
                property.getRenderRect(selectorRenderRect)
                //property.getRenderBounds(selectorRenderBounds)

                val groupRenderProperty = getGroupRenderProperty()
                groupRenderProperty.getRenderBounds(selectorRenderBounds)

                delegate.renderViewBox.transformToOutside(
                    selectorRenderRect,
                    selectorRenderDrawRect
                )
                delegate.renderViewBox.transformToOutside(
                    selectorRenderBounds,
                    selectorRenderDrawBounds
                )
                //元素的bounds范围
                if (property.angle != 0f && renderFlags.have(RENDERER_FLAG_DRAW_SELECTOR_BOUNDS)) {
                    boundsPaint.color = boundsColor
                    canvas.drawRect(selectorRenderDrawBounds, boundsPaint)
                }
                canvas.withRotation(
                    property.angle,
                    selectorRenderDrawRect.centerX(),
                    selectorRenderDrawRect.centerY()
                ) {
                    if (renderFlags.have(RENDERER_FLAG_DRAW_SELECTOR_RECT)) {
                        boundsPaint.color = elementBoundsColor
                        canvas.drawRect(selectorRenderDrawRect, boundsPaint)
                    }
                    if (renderFlags.have(RENDERER_FLAG_DRAW_FRAME_SIZE)) {
                        drawFrameSizeText(
                            this,
                            selectorRenderBounds,//selectorRenderRect
                            selectorRenderDrawRect,
                            property.angle
                        )
                    }
                    if (renderFlags.have(RENDERER_FLAG_DRAW_FRAME_LOCATION)) {
                        drawFrameLocationText(
                            this,
                            selectorRenderBounds,
                            selectorRenderDrawRect,
                            property.angle
                        )
                    }
                    if (renderFlags.have(RENDERER_FLAG_DRAW_FRAME_ROTATE)) {
                        drawFrameRotateText(this, selectorRenderDrawRect, property.angle)
                    }
                }
            }
        }
    }

    /**文本的绘制范围*/
    private val _textBounds = emptyRectF()

    /**文本背景的绘制范围*/
    private val _textBgBounds = emptyRectF()

    private fun _drawFrameBg(
        canvas: Canvas,
        drawBounds: RectF,
        textWidth: Int,
        textHeight: Int
    ) {
        val textLeft = drawBounds.centerX() - textWidth / 2
        val textTop = drawBounds.top - textOffset - textPadding - textHeight
        _textBounds.set(textLeft, textTop, textLeft + textWidth, textTop + textHeight)

        _textBgBounds.set(_textBounds)
        _textBgBounds.inset(-textPadding * 2, -textPadding)
        canvas.drawRoundRect(_textBgBounds, 4 * dp, 4 * dp, textBgPaint)
    }

    /**在[drawRect]上绘制文本信息*/
    private fun _drawFrameText(
        canvas: Canvas,
        text: CharSequence,
        drawRect: RectF,
        rotate: Float
    ) {
        DrawText().apply {
            textPaint = this@CanvasSelectorComponent.textPaint
            drawText = text
            val layout = makeLayout()

            _drawFrameBg(canvas, drawRect, layout.width, layout.height)

            //处理镜像缩放
            canvas.withTextScale(rotate, _textBounds) {
                //偏移到文本位置
                canvas.withTranslation(_textBounds.left, _textBounds.top) {
                    onDraw(canvas)
                }
            }
        }
    }

    /**[bounds] 数值提供矩形
     * [drawRect]文本定位矩形*/
    private fun drawFrameSizeText(canvas: Canvas, bounds: RectF, drawRect: RectF, rotate: Float) {
        val widthValue = valueUnit.convertPixelToValue(bounds.width())
        val widthUnit = valueUnit.formatValue(widthValue, true, true)
        val heightValue = valueUnit.convertPixelToValue(bounds.height())
        val heightUnit = valueUnit.formatValue(heightValue, true, true)
        val text = "w:$widthUnit\nh:$heightUnit"
        _drawFrameText(canvas, text, drawRect, rotate)
    }

    /**绘制xy坐标
     * [bounds] x y的取值对象
     * [drawRect] 用来定位xy文本的绘制矩形*/
    private fun drawFrameLocationText(
        canvas: Canvas,
        bounds: RectF,
        drawRect: RectF,
        rotate: Float
    ) {
        val xValue = valueUnit.convertPixelToValue(bounds.left)
        val xUnit = valueUnit.formatValue(xValue, true, true)
        val yValue = valueUnit.convertPixelToValue(bounds.top)
        val yUnit = valueUnit.formatValue(yValue, true, true)
        val text = "x:$xUnit\ny:$yUnit"

        _drawFrameText(canvas, text, drawRect, rotate)
    }

    private fun drawFrameRotateText(canvas: Canvas, drawRect: RectF, rotate: Float) {
        val text = "${rotate.canvasDecimal()}°"
        _drawFrameText(canvas, text, drawRect, rotate)
    }

    /**镜像翻转, 如果旋转的角度达到某个值时, 文本镜像一下*/
    private fun Canvas.withTextScale(rotate: Float, textBounds: RectF, block: Canvas.() -> Unit) {
        val angle = (rotate + 360) % 360
        if (angle > 90 && angle < 270) {
            withScale(-1f, -1f, textBounds.centerX(), textBounds.centerY(), block)
        } else {
            block()
        }
    }

    /**[isLockScaleRatio]
     * [renderFlags]
     * */
    override fun updateLockScaleRatio(
        lock: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        if (rendererList.size() == 1) {
            rendererList.first().updateLockScaleRatio(lock, reason, delegate)
        } else {
            super.updateLockScaleRatio(lock, reason, delegate)
        }
    }

    //region---操作---

    fun clearAllDrawFlag(reason: Reason, delegate: CanvasRenderDelegate?) {
        removeRenderFlag(RENDERER_FLAG_DRAW_FRAME_SIZE, reason, delegate)
        removeRenderFlag(RENDERER_FLAG_DRAW_FRAME_LOCATION, reason, delegate)
        removeRenderFlag(RENDERER_FLAG_DRAW_FRAME_ROTATE, reason, delegate)
        this.delegate.refresh()
    }

    /**只绘制宽高*/
    fun showSizeRender(reason: Reason, delegate: CanvasRenderDelegate?) {
        clearAllDrawFlag(reason, delegate)
        addRenderFlag(RENDERER_FLAG_DRAW_FRAME_SIZE, reason, delegate)
        this.delegate.refresh()
    }

    /**只绘制xy*/
    fun showLocationRender(reason: Reason, delegate: CanvasRenderDelegate?) {
        clearAllDrawFlag(reason, delegate)
        addRenderFlag(RENDERER_FLAG_DRAW_FRAME_LOCATION, reason, delegate)
        this.delegate.refresh()
    }

    /**只绘制旋转角度*/
    fun showRotateRender(reason: Reason, delegate: CanvasRenderDelegate?) {
        clearAllDrawFlag(reason, delegate)
        addRenderFlag(RENDERER_FLAG_DRAW_FRAME_ROTATE, reason, delegate)
        this.delegate.refresh()
    }

    /**清除选中元素*/
    fun clearSelectorRenderer(reason: Reason) {
        resetSelectorRenderer(null, reason)
    }

    /**[resetSelectorRenderer]*/
    fun resetSelectorRenderer(renderer: BaseRenderer?, reason: Reason) {
        if (renderer == null) {
            resetSelectorRenderer(emptyList(), reason)
        } else {
            resetSelectorRenderer(listOf(renderer), reason)
        }
    }

    /**重置所有选中的元素*/
    fun resetSelectorRenderer(list: List<BaseRenderer>, reason: Reason) {
        val old = rendererList.toList()
        resetGroupRendererList(list, reason, delegate)
        onSelfSelectorRendererChange(old)
    }

    /**添加一个元素到选择器*/
    fun addSelectorRenderer(renderer: BaseRenderer, reason: Reason) {
        val old = rendererList.toList()
        addRendererToGroup(renderer, reason, delegate)
        onSelfSelectorRendererChange(old)
    }

    /**从选择器中移除一个元素*/
    fun removeSelectorRenderer(renderer: BaseRenderer, reason: Reason) {
        val old = rendererList.toList()
        removeRendererFromGroup(renderer, reason, delegate)
        onSelfSelectorRendererChange(old)
    }

    /**选中的渲染器改变*/
    private fun onSelfSelectorRendererChange(old: List<BaseRenderer>) {
        delegate.controlManager.updateControlPointLocation()
        delegate.dispatchSelectorRendererChange(old, rendererList)
    }

    //endregion---操作---

}