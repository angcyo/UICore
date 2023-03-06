package com.angcyo.canvas.render.renderer

import android.graphics.*
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.CanvasRenderViewBox
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RendererParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.size
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * 群组渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */
open class CanvasGroupRenderer : BaseRenderer() {

    /**组内所有渲染器*/
    val rendererList = CopyOnWriteArrayList<BaseRenderer>()

    //region---core---

    override fun renderOnOutside(canvas: Canvas, params: RendererParams) {
        super.renderOnOutside(canvas, params)
        for (renderer in rendererList) {
            renderer.renderOnOutside(canvas, params)
        }
    }

    override fun renderOnView(canvas: Canvas, params: RendererParams) {
        super.renderOnView(canvas, params)
        for (renderer in rendererList) {
            renderer.renderOnView(canvas, params)
        }
    }

    override fun renderOnInside(canvas: Canvas, params: RendererParams) {
        super.renderOnInside(canvas, params)
        for (renderer in rendererList) {
            renderer.renderOnInside(canvas, params)
        }
    }

    override fun getElementList(): List<IElement> {
        val result = mutableListOf<IElement>()
        for (renderer in rendererList) {
            renderer?.let { result.addAll(renderer.getElementList()) }
        }
        return result
    }

    override fun getRendererList(): List<BaseRenderer> {
        val result = mutableListOf<BaseRenderer>()
        result.add(this)//包含自己
        for (renderer in rendererList) {
            renderer?.let { result.addAll(renderer.getRendererList()) }
        }
        return result
    }

    /**正常的元素矩形范围, +单独旋转*/
    private val _elementRenderRect = RectF()
    private val _elementRenderDrawRect = RectF()

    /**绘制元素的Rect范围+旋转
     * [drawElementRect]*/
    protected fun drawElementRect(
        canvas: Canvas,
        renderViewBox: CanvasRenderViewBox,
        paint: Paint
    ) {
        for (renderer in rendererList) {
            renderer.renderProperty?.let { property ->
                drawElementRect(canvas, renderViewBox, property, paint)
            }
        }
    }

    /**[drawElementRect]*/
    protected fun drawElementRect(
        canvas: Canvas,
        renderViewBox: CanvasRenderViewBox,
        property: CanvasRenderProperty,
        paint: Paint
    ) {
        property.getRenderRect(_elementRenderRect)
        renderViewBox.transformToOutside(_elementRenderRect, _elementRenderDrawRect)
        canvas.withRotation(
            property.angle,
            _elementRenderDrawRect.centerX(),
            _elementRenderDrawRect.centerY()
        ) {
            canvas.drawRect(_elementRenderDrawRect, paint)
        }
    }

    //---

    /**贴合元素的Bounds范围*/
    private val _elementBoundsRect = RectF()
    private val _elementBoundsDrawRect = RectF()

    /**绘制贴合元素的Bounds范围
     * [drawElementBounds]*/
    protected fun drawElementBounds(
        canvas: Canvas,
        renderViewBox: CanvasRenderViewBox,
        paint: Paint
    ) {
        for (renderer in rendererList) {
            renderer.renderProperty?.let { property ->
                drawElementBounds(canvas, renderViewBox, property, paint)
            }
        }
    }

    /**[drawElementBounds]*/
    protected fun drawElementBounds(
        canvas: Canvas,
        renderViewBox: CanvasRenderViewBox,
        property: CanvasRenderProperty,
        paint: Paint
    ) {
        property.getRenderBounds(_elementBoundsRect)
        renderViewBox.transformToOutside(_elementBoundsRect, _elementBoundsDrawRect)
        canvas.withSave {
            canvas.drawRect(_elementBoundsDrawRect, paint)
        }
    }

    /**[renderProperty]更新时, 不需要同步到Child*/
    override fun updateRenderProperty(
        target: CanvasRenderProperty?,
        reason: Reason,
        delegate: CanvasRenderDelegate?,
    ) {
        super.updateRenderProperty(target, reason, delegate)
    }

    override fun applyTranslateMatrix(
        matrix: Matrix,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.applyTranslateMatrix(matrix, reason, delegate)
        for (renderer in rendererList) {
            renderer.applyTranslateMatrix(matrix, reason, delegate)
        }
    }

    override fun applyRotateMatrix(
        matrix: Matrix,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.applyRotateMatrix(matrix, reason, delegate)
        for (renderer in rendererList) {
            renderer.applyRotateMatrix(matrix, reason, delegate)
        }
    }

    private val elementMatrix = Matrix()
    private val rotateMatrix = Matrix()
    private val scaleMatrix = Matrix()
    private val invertRotateMatrix = Matrix()
    private val _invertAnchorPoint = PointF()

    override fun applyScaleMatrix(
        matrix: Matrix,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        renderProperty?.let { property ->
            property.getRenderRect(_tempRect)
            rotateMatrix.setRotate(property.angle, _tempRect.centerX(), _tempRect.centerY())
            rotateMatrix.invert(invertRotateMatrix)

            _invertAnchorPoint.set(property.anchorX, property.anchorY)
            invertRotateMatrix.mapPoint(_invertAnchorPoint)

            scaleMatrix.setScale(
                matrix.getScaleX(),
                matrix.getScaleY(),
                _invertAnchorPoint.x,
                _invertAnchorPoint.y
            )

            elementMatrix.set(invertRotateMatrix)
            elementMatrix.postConcat(scaleMatrix)
            elementMatrix.postConcat(rotateMatrix)

            //self
            super.applyScaleMatrix(matrix, reason, delegate)

            //sub
            for (renderer in rendererList) {
                if (renderer is CanvasGroupRenderer) {
                    renderer.applyScaleMatrix(matrix, reason, delegate)
                } else {
                    renderer.applyScaleMatrixWithCenter(
                        elementMatrix,
                        reason,
                        delegate
                    )
                }
            }
        }
    }

    override fun applyScaleMatrixWithCenter(
        matrix: Matrix,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.applyScaleMatrixWithCenter(matrix, reason, delegate)
        for (renderer in rendererList) {
            renderer.applyScaleMatrixWithCenter(matrix, reason, delegate)
        }
    }

    fun getGroupRenderProperty(): CanvasRenderProperty {
        val result = CanvasRenderProperty()
        if (rendererList.size() == 1) {
            //只有一个元素
            val element = rendererList.first()
            element.renderProperty?.let {
                it.copyTo(result)
                /*val rect = it.getRenderRect()
                result.initWithRect(rect, it.angle)*/
            }
        } else {
            //多个元素
            val rect = RectF(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
            for (renderer in rendererList) {
                renderer.renderProperty?.let {
                    val bounds = it.getRenderBounds()
                    //val bounds = it.getRenderBounds(afterRotate = true)
                    rect.left = min(rect.left, bounds.left)
                    rect.top = min(rect.top, bounds.top)
                    rect.right = max(rect.right, bounds.right)
                    rect.bottom = max(rect.bottom, bounds.bottom)
                }
            }
            result.initWithRect(rect, 0f)
        }
        return result
    }

    //endregion---core---

    //region---操作---

    /**根据[rendererList]确定能包裹所有选中元素的渲染信息
     * [CanvasRenderProperty]*/
    fun updateRenderProperty() {
        if (rendererList.isEmpty()) {
            renderProperty = null
            return
        }
        renderProperty = getGroupRenderProperty()
    }

    /**重置所有的元素*/
    open fun resetRendererList(list: List<BaseRenderer>) {
        rendererList.clear()
        rendererList.addAll(list)
        updateRenderProperty()
    }

    /**添加一个元素到组内*/
    open fun addRenderer(elementRenderer: BaseRenderer) {
        rendererList.add(elementRenderer)
        updateRenderProperty()
    }

    //endregion---操作---
}