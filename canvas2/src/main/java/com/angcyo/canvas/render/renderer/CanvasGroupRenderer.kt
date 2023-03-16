package com.angcyo.canvas.render.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.CanvasRenderViewBox
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.util.PictureRenderDrawable
import com.angcyo.canvas.render.util.createOverrideBitmapCanvas
import com.angcyo.canvas.render.util.createOverridePictureCanvas
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * 群组渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */
open class CanvasGroupRenderer : BaseRenderer() {

    companion object {

        /**计算[rendererList]的bounds范围
         * [bounds] 强制指定, 指定后不计算*/
        fun computeBounds(rendererList: List<BaseRenderer>?, @Pixel bounds: RectF? = null): RectF {
            if (bounds == null) {
                if (rendererList == null) {
                    return RectF(0f, 0f, 0f, 0f)
                }
                val rect =
                    RectF(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
                var isSet = false
                for (renderer in rendererList) {
                    renderer.renderProperty?.getRenderBounds()?.let {
                        isSet = true
                        rect.set(
                            min(it.left, rect.left),
                            min(it.top, rect.top),
                            max(it.right, rect.right),
                            max(it.bottom, rect.bottom)
                        )
                    }
                }
                if (!isSet) {
                    rect.set(0f, 0f, 0f, 0f)
                }
                return rect
            } else {
                return bounds
            }
        }

        /**[rendererList] 需要绘制的渲染器
         * [overrideSize] 需要等比覆盖输出的大小
         * [bounds] 指定输出的绘制位置*/
        fun createRenderDrawable(
            rendererList: List<BaseRenderer>?,
            overrideSize: Float? = null,
            @Pixel bounds: RectF? = null
        ): Drawable? {
            rendererList ?: return null
            val rect = computeBounds(rendererList, bounds)
            return PictureRenderDrawable(createOverridePictureCanvas(
                rect.width(),
                rect.height(),
                overrideSize
            ) {
                translate(-rect.left, -rect.top)
                val params = RenderParams()
                for (renderer in rendererList) {
                    renderer.renderOnInside(this, params)
                }
            })
        }

        /**[rendererList] 需要绘制的渲染器
         * [overrideSize] 需要等比覆盖输出的大小
         * [bounds] 指定输出的绘制位置*/
        fun createRenderBitmap(
            rendererList: List<BaseRenderer>?,
            overrideSize: Float? = null,
            @Pixel bounds: RectF? = null
        ): Bitmap? {
            rendererList ?: return null
            val rect = computeBounds(rendererList, bounds)
            return createOverrideBitmapCanvas(rect.width(), rect.height(), overrideSize) {
                translate(-rect.left, -rect.top)
                val params = RenderParams()
                for (renderer in rendererList) {
                    renderer.renderOnInside(this, params)
                }
            }
        }
    }

    /**组内所有渲染器*/
    val rendererList = CopyOnWriteArrayList<BaseRenderer>()

    //region---core---

    /**更新渲染时, 需要的一些数据*/
    override fun readyRenderIfNeed(params: RenderParams?) {
        super.readyRenderIfNeed(params)
        if (renderProperty == null || renderFlags.have(RENDERER_FLAG_REQUEST_PROPERTY)) {
            updateGroupRenderProperty(Reason.code, null)
            renderFlags = renderFlags.remove(RENDERER_FLAG_REQUEST_PROPERTY)
        }
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        super.renderOnOutside(canvas, params)
        for (renderer in rendererList) {
            renderer.renderOnOutside(canvas, params)
        }
    }

    override fun renderOnView(canvas: Canvas, params: RenderParams) {
        super.renderOnView(canvas, params)
        for (renderer in rendererList) {
            renderer.renderOnView(canvas, params)
        }
    }

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
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

            //子元素因为需要qr计算, 所以需要重新生成新的适用于子元素的矩阵
            elementMatrix.set(invertRotateMatrix)
            elementMatrix.postConcat(scaleMatrix)
            elementMatrix.postConcat(rotateMatrix)

            //self
            super.applyScaleMatrix(matrix, reason, delegate)

            //sub
            for (renderer in rendererList) {
                if (renderer is CanvasGroupRenderer || rendererList.size() <= 1) {
                    renderer.applyScaleMatrix(matrix, reason, delegate)
                } else {
                    renderer.applyScaleMatrixWithCenter(elementMatrix, true, reason, delegate)
                }
            }
        }
    }

    override fun applyScaleMatrixWithCenter(
        matrix: Matrix,
        useQr: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.applyScaleMatrixWithCenter(matrix, false, reason, delegate)
        for (renderer in rendererList) {
            //所有子元素, 需要使用qr
            renderer.applyScaleMatrixWithCenter(matrix, true, reason, delegate)
        }
    }

    /**同步映射到子元素中, 如果需要单独控制, 请主动遍历子元素并设置*/
    override fun applyFlip(
        flipX: Boolean,
        flipY: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        super.applyFlip(flipX, flipY, reason, delegate)
        for (renderer in rendererList) {
            renderer.applyFlip(flipX, flipY, reason, delegate)
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

    override fun updateLock(lock: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        super.updateLock(lock, reason, delegate)
        for (renderer in rendererList) {
            renderer.updateLock(lock, reason, delegate)
        }
    }

    override fun updateVisible(visible: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        super.updateVisible(visible, reason, delegate)
        for (renderer in rendererList) {
            renderer.updateVisible(visible, reason, delegate)
        }
    }

    //endregion---core---

    //region---操作---

    /**根据[rendererList]确定能包裹所有选中元素的渲染信息
     * [CanvasRenderProperty]*/
    fun updateGroupRenderProperty(reason: Reason, delegate: CanvasRenderDelegate?) {
        val target = if (rendererList.isEmpty()) null else getGroupRenderProperty()
        updateRenderProperty(target, reason, delegate)
    }

    /**重置所有的元素*/
    open fun resetGroupRendererList(
        list: List<BaseRenderer>,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        rendererList.clear()
        rendererList.addAll(list)
        updateGroupRenderProperty(reason, delegate)
    }

    /**添加一个元素到组内*/
    open fun addRendererToGroup(
        elementRenderer: BaseRenderer,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        rendererList.add(elementRenderer)
        updateGroupRenderProperty(reason, delegate)
    }

    /**从组内移除一个元素*/
    open fun removeRendererFromGroup(
        elementRenderer: BaseRenderer,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        rendererList.remove(elementRenderer)
        updateGroupRenderProperty(reason, delegate)
    }

    //endregion---操作---
}