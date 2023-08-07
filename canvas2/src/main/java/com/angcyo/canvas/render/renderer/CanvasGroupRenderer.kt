package com.angcyo.canvas.render.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.CanvasRenderViewBox
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.element.PathElement
import com.angcyo.canvas.render.state.GroupStateStack
import com.angcyo.canvas.render.state.PropertyStateStack
import com.angcyo.canvas.render.util.PictureRenderDrawable
import com.angcyo.canvas.render.util.isOnlyGroupRenderer
import com.angcyo.drawable.isGravityBottom
import com.angcyo.drawable.isGravityCenter
import com.angcyo.drawable.isGravityCenterHorizontal
import com.angcyo.drawable.isGravityCenterVertical
import com.angcyo.drawable.isGravityLeft
import com.angcyo.drawable.isGravityRight
import com.angcyo.drawable.isGravityTop
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.Strategy
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.add
import com.angcyo.library.ex.c
import com.angcyo.library.ex.createOverrideBitmapCanvas
import com.angcyo.library.ex.createOverridePictureCanvas
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.isInvalid
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.resetAll
import com.angcyo.library.ex.size
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * 群组渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */

/**额外的渲染动作*/
typealias RenderAction = (renderer: CanvasElementRenderer, canvas: Canvas, renderProperty: CanvasRenderProperty, params: RenderParams) -> Unit

open class CanvasGroupRenderer : BaseRenderer() {

    companion object {

        /**群组*/
        const val GROUP_TYPE_GROUP = 1

        /**解组*/
        const val GROUP_TYPE_DISSOLVE = 2

        /**计算[rendererList]的bounds范围
         * [bounds] 强制指定, 指定后不计算*/
        fun computeBounds(
            rendererList: List<BaseRenderer>?,
            @Pixel bounds: RectF? = null,
            ignoreVisible: Boolean = false
        ): RectF? {
            if (bounds == null) {
                if (rendererList == null) {
                    return null
                }
                val rect =
                    RectF(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
                var isSet = false
                val bounds = acquireTempRectF()
                for (renderer in rendererList) {
                    if (ignoreVisible || renderer.isVisible) {
                        renderer.renderProperty?.getRenderBounds(bounds)?.let {
                            if (!it.isInvalid()) {
                                isSet = true
                                rect.set(
                                    min(it.left, rect.left),
                                    min(it.top, rect.top),
                                    max(it.right, rect.right),
                                    max(it.bottom, rect.bottom)
                                )
                            }
                        }
                    }
                }
                bounds.release()
                if (!isSet) {
                    return null
                }
                return rect
            } else {
                return bounds
            }
        }

        /**[rendererList] 需要绘制的渲染器, 必须是简单的渲染器
         * [overrideSize] 需要等比覆盖输出的大小
         * [bounds] 指定输出的绘制位置*/
        fun createRenderDrawable(
            rendererList: List<BaseRenderer>?,
            overrideSize: Float? = null,
            @Pixel bounds: RectF? = null,
            ignoreVisible: Boolean = false
        ): Drawable? {
            rendererList ?: return null
            val rect = computeBounds(rendererList, bounds, ignoreVisible) ?: return null
            return PictureRenderDrawable(createOverridePictureCanvas(
                rect.width(),
                rect.height(),
                overrideSize
            ) {
                translate(-rect.left, -rect.top)
                val params = RenderParams()
                params.apply {
                    if (overrideSize != null) {
                        renderDst = overrideSize / rect.width()
                    }
                }
                renderRenderer(this, rendererList, params, ignoreVisible)
            })
        }

        /**[rendererList] 需要绘制的渲染器
         * [overrideSize] 需要等比覆盖输出的大小
         * [bounds] 指定输出的绘制位置
         * [ignoreVisible] 是否要忽略可见性, false: 只有可见的才会绘制
         * [backgroundColor] 背景颜色
         * */
        fun createRenderBitmap(
            rendererList: List<BaseRenderer>?,
            overrideSize: Float? = null,
            @Pixel bounds: RectF? = null,
            ignoreVisible: Boolean = false,
            backgroundColor: Int = Color.TRANSPARENT,
            renderAfterAction: RenderAction? = null
        ): Bitmap? {
            rendererList ?: return null
            val rect = computeBounds(rendererList, bounds) ?: return null
            if (rect.isInvalid()) {
                return null
            }
            return createOverrideBitmapCanvas(
                rect.width(),
                rect.height(),
                overrideSize
            ) {
                if (backgroundColor != Color.TRANSPARENT) {
                    drawColor(backgroundColor)
                }
                translate(-rect.left, -rect.top)
                val params = RenderParams()
                params.apply {
                    if (overrideSize != null) {
                        renderDst = overrideSize / rect.width()
                    }
                }
                renderRenderer(this, rendererList, params, ignoreVisible, renderAfterAction)
            }
        }

        /**使用[canvas]绘制[rendererList]
         *
         * [renderAfterAction] 元素渲染后的额外动作*/
        fun renderRenderer(
            canvas: Canvas,
            rendererList: List<BaseRenderer>,
            params: RenderParams,
            ignoreVisible: Boolean = false,
            renderAfterAction: RenderAction? = null
        ) {
            val bounds = acquireTempRectF()
            for (renderer in rendererList) {
                if (ignoreVisible || renderer.isVisible) {
                    if (renderer is CanvasElementRenderer) {
                        val renderProperty = renderer.renderProperty ?: continue
                        val renderElement = renderer.renderElement
                        if (renderElement is PathElement && renderElement.paint.style == Paint.Style.STROKE) {
                            params.drawOffsetWidth = renderElement.paint.strokeWidth
                            params.drawOffsetHeight = params.drawOffsetWidth
                        } else {
                            params.drawOffsetWidth = 0f
                            params.drawOffsetHeight = 0f
                        }
                        val drawable = renderElement?.requestElementDrawable(renderer, params)
                            ?: continue
                        renderer.renderDrawable(
                            canvas,
                            renderProperty.getRenderBounds(bounds),
                            drawable,
                            params
                        )
                        //
                        renderAfterAction?.invoke(renderer, canvas, renderProperty, params)
                    } else if (renderer is CanvasGroupRenderer) {
                        renderRenderer(
                            canvas,
                            renderer.rendererList,
                            params,
                            ignoreVisible,
                            renderAfterAction
                        )
                    }
                }
            }
            bounds.release()
        }

        /**获取一组渲染器对应的边界矩形
         * [keepSingle] 只有一个元素时, 是否保持原样
         * [CanvasRenderProperty]*/
        fun getRendererListRenderProperty(
            rendererList: List<BaseRenderer>,
            keepSingle: Boolean = true,
            result: CanvasRenderProperty = CanvasRenderProperty()
        ): CanvasRenderProperty {
            result.reset()
            if (rendererList.size() == 1 && keepSingle) {
                //只有一个元素
                val renderer = rendererList.first()
                renderer.renderProperty?.let {
                    it.copyTo(result)
                    /*val rect = it.getRenderRect()
                    result.initWithRect(rect, it.angle)*/
                }
            } else {
                //多个元素
                val rect = acquireTempRectF()
                val bounds = acquireTempRectF()
                rect.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
                for (renderer in rendererList) {
                    renderer.renderProperty?.let {
                        it.getRenderBounds(bounds)
                        //val bounds = it.getRenderBounds(afterRotate = true)
                        rect.left = min(rect.left, bounds.left)
                        rect.top = min(rect.top, bounds.top)
                        rect.right = max(rect.right, bounds.right)
                        rect.bottom = max(rect.bottom, bounds.bottom)
                    }
                }
                result.initWithRect(rect, 0f)
                rect.release()
                bounds.release()
            }
            return result
        }
    }

    /**组内所有渲染器*/
    val rendererList = CopyOnWriteArrayList<BaseRenderer>()

    private val _bounds = RectF()

    //region---core---

    override fun updateRenderProperty() {
        super.updateRenderProperty()
        for (renderer in rendererList) {
            renderer.updateRenderProperty()
        }
        updateGroupRenderProperty(Reason.code, null)
    }

    override fun requestUpdatePropertyFlag(reason: Reason, delegate: CanvasRenderDelegate?) {
        super.requestUpdatePropertyFlag(reason, delegate)
        if (isOnlyGroupRenderer()) {
            for (renderer in rendererList) {
                renderer.requestUpdatePropertyFlag(reason, delegate)
            }
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

    override fun getSingleElementList(): List<IElement> {
        val result = mutableListOf<IElement>()
        for (renderer in rendererList) {
            renderer?.let { result.addAll(renderer.getSingleElementList()) }
        }
        return result
    }

    override fun getSingleRendererList(includeGroup: Boolean): List<BaseRenderer> {
        val result = mutableListOf<BaseRenderer>()
        if (includeGroup && isOnlyGroupRenderer()) {
            result.add(this)
        }
        for (renderer in rendererList) {
            renderer?.let { result.addAll(renderer.getSingleRendererList(includeGroup)) }
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
        paint: Paint,
        dissolveGroup: Boolean,
        list: List<BaseRenderer> = rendererList
    ) {
        for (renderer in list) {
            if (dissolveGroup) {
                //拆组, 那么自身不绘制
                if (renderer is CanvasGroupRenderer) {
                    drawElementRect(canvas, renderViewBox, paint, true, renderer.rendererList)
                } else {
                    renderer.renderProperty?.let { property ->
                        drawElementRect(canvas, renderViewBox, property, paint)
                    }
                }
            } else {
                renderer.renderProperty?.let { property ->
                    drawElementRect(canvas, renderViewBox, property, paint)
                }
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
        paint: Paint,
        dissolveGroup: Boolean,
        list: List<BaseRenderer> = rendererList
    ) {
        for (renderer in list) {
            if (dissolveGroup) {
                //拆组, 那么自身不绘制
                if (renderer is CanvasGroupRenderer) {
                    drawElementBounds(canvas, renderViewBox, paint, true, renderer.rendererList)
                } else {
                    renderer.renderProperty?.let { property ->
                        drawElementBounds(canvas, renderViewBox, property, paint)
                    }
                }
            } else {
                renderer.renderProperty?.let { property ->
                    drawElementBounds(canvas, renderViewBox, property, paint)
                }
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
    override fun updateRenderPropertyTo(
        target: CanvasRenderProperty?,
        reason: Reason,
        delegate: CanvasRenderDelegate?,
    ) {
        super.updateRenderPropertyTo(target, reason, delegate)
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
        super.applyRotateMatrix(matrix, reason, delegate)//自身
        for (renderer in rendererList) {//所有子元素
            renderer.applyRotateMatrix(matrix, reason, delegate)
        }
        //2023-3-22 旋转之后, 需要摆正角度到0度, 方便Bounds的计算及显示
        checkUpdateGroupRenderProperty(reason, delegate)
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

            //self
            super.applyScaleMatrix(matrix, reason, delegate)

            //applyScaleMatrix 可能会被limit修改
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

            //sub
            applyGroupScaleMatrix(this, elementMatrix, reason, delegate)
        }
    }

    /**group内的其他元素需要特殊处理*/
    private fun applyGroupScaleMatrix(
        renderer: BaseRenderer,
        elementMatrix: Matrix,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        //sub
        if (renderer is CanvasGroupRenderer) {
            for (subRenderer in renderer.rendererList) {
                applyGroupScaleMatrix(subRenderer, elementMatrix, reason, delegate)
            }
            if (this != renderer) {
                renderer.updateGroupRenderProperty(reason, delegate)
            }
        } else {
            //element
            renderer.applyScaleMatrixWithCenter(elementMatrix, true, reason, delegate)
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
        checkUpdateGroupRenderProperty(reason, delegate)
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

    /**当前的渲染器是否包含指定的渲染器
     * [renderer] 判断的目标
     * [recursively] 是否递归判断
     * */
    fun containsRenderer(
        renderer: BaseRenderer,
        recursively: Boolean,
        list: List<BaseRenderer> = rendererList
    ): Boolean {
        var result = false
        for (sub in list) {
            result = when (sub) {
                renderer -> true
                is CanvasGroupRenderer -> if (recursively) sub.containsRenderer(
                    renderer,
                    true,
                    sub.rendererList
                ) else false

                else -> false
            }
            if (result) {
                break
            }
        }
        return result
    }

    /**临时变量*/
    private val _tempGroupRenderProperty = CanvasRenderProperty()

    /**重新计算渲染属性*/
    fun getGroupRenderProperty(result: CanvasRenderProperty = _tempGroupRenderProperty): CanvasRenderProperty {
        return getRendererListRenderProperty(
            rendererList,
            LibHawkKeys.keepSingleRenderProperty,
            result
        )
    }

    /**摆正角度到0度, 方便Bounds的计算及显示*/
    protected fun checkUpdateGroupRenderProperty(reason: Reason, delegate: CanvasRenderDelegate?) {
        if (reason.reason == Reason.REASON_USER) {
            updateGroupRenderProperty(reason, delegate)
        }
    }

    //endregion---core---

    //region---操作---

    /**根据[rendererList]确定能包裹所有选中元素的渲染信息
     * [CanvasRenderProperty]*/
    fun updateGroupRenderProperty(reason: Reason, delegate: CanvasRenderDelegate?) {
        val target = if (rendererList.isEmpty()) null else getGroupRenderProperty()
        updateRenderPropertyTo(target, reason, delegate)
    }

    /**重置所有的元素*/
    open fun resetGroupRendererList(
        list: List<BaseRenderer>,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        val elementRendererList = delegate?.getElementRendererListOf(list) ?: list
        rendererList.resetAll(elementRendererList)

        val isAllVisible =
            elementRendererList.sumOf { if (it.isVisible) 1L else 0 } == elementRendererList.size()
                .toLong()
        val isAllLock =
            elementRendererList.sumOf { if (it.isLock) 1L else 0 } == elementRendererList.size()
                .toLong()

        updateVisible(isAllVisible, reason, delegate)
        updateLock(isAllLock, reason, delegate)

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

    //---

    /**更新选中子项的对齐方式
     * [align] [Gravity.LEFT]*/
    fun updateRendererAlign(
        align: Int,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val list = rendererList
        if (list.size() <= 1) {
            return
        }
        val property = renderProperty ?: return
        val groupBounds = acquireTempRectF()
        val bounds = acquireTempRectF()
        property.getRenderBounds(groupBounds)

        //寻找定位锚点item
        var anchorItemRenderer: BaseRenderer? = null
        if (align.isGravityCenter()) {
            //找出距离中心点最近的Item
            val centerX = groupBounds.centerX()
            val centerY = groupBounds.centerY()

            //2点之间的最小距离
            var minR = Float.MAX_VALUE

            list.forEach {
                val bounds = it.renderProperty?.getRenderBounds(bounds)
                if (bounds != null) {
                    val r = c(centerX, centerY, bounds.centerX(), bounds.centerY()).absoluteValue
                    if (r < minR) {
                        anchorItemRenderer = it
                        minR = r
                    }
                }
            }
        } else if (align.isGravityCenterHorizontal()) {
            //水平居中, 找出最大的高度item
            var maxHeight = Float.MIN_VALUE
            list.forEach {
                val bounds = it.renderProperty?.getRenderBounds(bounds)
                if (bounds != null) {
                    if (bounds.height() > maxHeight) {
                        anchorItemRenderer = it
                        maxHeight = bounds.height()
                    }
                }
            }
        } else if (align.isGravityCenterVertical()) {
            //垂直居中, 找出最大的宽度item
            var maxWidth = Float.MIN_VALUE
            list.forEach {
                val bounds = it.renderProperty?.getRenderBounds(bounds)
                if (bounds != null) {
                    if (bounds.width() > maxWidth) {
                        anchorItemRenderer = it
                        maxWidth = bounds.width()
                    }
                }
            }
        } else if (align.isGravityTop()) {
            var minTop = Float.MAX_VALUE
            list.forEach {
                val bounds = it.renderProperty?.getRenderBounds(bounds)
                if (bounds != null) {
                    if (bounds.top < minTop) {
                        anchorItemRenderer = it
                        minTop = bounds.top
                    }
                }
            }
        } else if (align.isGravityBottom()) {
            var maxBottom = Float.MIN_VALUE
            list.forEach {
                val bounds = it.renderProperty?.getRenderBounds(bounds)
                if (bounds != null) {
                    if (bounds.bottom > maxBottom) {
                        anchorItemRenderer = it
                        maxBottom = bounds.bottom
                    }
                }
            }
        } else if (align.isGravityLeft()) {
            var minLeft = Float.MAX_VALUE
            list.forEach {
                val bounds = it.renderProperty?.getRenderBounds(bounds)
                if (bounds != null) {
                    if (bounds.left < minLeft) {
                        anchorItemRenderer = it
                        minLeft = bounds.left
                    }
                }
            }
        } else if (align.isGravityRight()) {
            var maxRight = Float.MIN_VALUE
            list.forEach {
                val bounds = it.renderProperty?.getRenderBounds(bounds)
                if (bounds != null) {
                    if (bounds.right > maxRight) {
                        anchorItemRenderer = it
                        maxRight = bounds.right
                    }
                }
            }
        }

        if (anchorItemRenderer == null) {
            groupBounds.release()
            bounds.release()
            return
        }

        //保存一个状态
        val undoState = PropertyStateStack()
        undoState.saveState(this, delegate)

        val anchorBounds = acquireTempRectF()
        anchorItemRenderer!!.renderProperty?.getRenderBounds(anchorBounds) ?: return
        for (item in list) {
            if (item != anchorItemRenderer) {
                val itemBounds = item.renderProperty?.getRenderBounds(bounds) ?: continue
                //开始调整
                var dx = 0f
                var dy = 0f

                if (align.isGravityCenter()) {
                    dx = anchorBounds.centerX() - itemBounds.centerX()
                    dy = anchorBounds.centerY() - itemBounds.centerY()
                } else if (align.isGravityCenterHorizontal()) {
                    dy = anchorBounds.centerY() - itemBounds.centerY()
                } else if (align.isGravityCenterVertical()) {
                    dx = anchorBounds.centerX() - itemBounds.centerX()
                } else if (align.isGravityTop()) {
                    dy = anchorBounds.top - itemBounds.top
                } else if (align.isGravityBottom()) {
                    dy = anchorBounds.bottom - itemBounds.bottom
                } else if (align.isGravityLeft()) {
                    dx = anchorBounds.left - itemBounds.left
                } else if (align.isGravityRight()) {
                    dx = anchorBounds.right - itemBounds.right
                }

                val matrix = Matrix()
                matrix.setTranslate(dx, dy)
                item.applyTranslateMatrix(matrix, reason.apply {
                    controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_TRANSLATE)
                }, delegate)
            }
        }

        //保存一个状态
        val redoState = PropertyStateStack()
        redoState.saveState(this, delegate)

        //回退栈
        delegate?.addStateToStack(this, undoState, redoState, false, reason, strategy)

        anchorBounds.release()
        groupBounds.release()
        bounds.release()
    }

    /**水平分布/垂直分布*/
    fun updateRendererFlat(
        flat: Int,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val list = rendererList
        val count = list.size()
        if (count <= 2) {
            return
        }

        val sortList = list.toMutableList()
        //先排序
        when (flat) {
            LinearLayout.VERTICAL -> sortList.sortBy {
                it.renderProperty?.getRenderBounds(_bounds)?.centerY()
            }

            LinearLayout.HORIZONTAL -> sortList.sortBy {
                it.renderProperty?.getRenderBounds(_bounds)?.centerX()
            }

            else -> return
        }

        val first = sortList.first()
        val last = sortList.last()

        val firstBounds = acquireTempRectF()
        val lastBounds = acquireTempRectF()
        first?.renderProperty?.getRenderBounds(firstBounds) ?: return
        last?.renderProperty?.getRenderBounds(lastBounds) ?: return

        val firstX = firstBounds.centerX()
        val firstY = firstBounds.centerY()
        //步长
        val step = when (flat) {
            LinearLayout.HORIZONTAL -> lastBounds.centerX() - firstX
            LinearLayout.VERTICAL -> lastBounds.centerY() - firstY
            else -> 0f
        } / (count - 1)

        //保存一个状态
        val undoState = PropertyStateStack()
        undoState.saveState(this, delegate)

        sortList.forEachIndexed { index, renderer ->
            if (renderer != first && renderer != last) {
                val rendererBounds = renderer.renderProperty?.getRenderBounds(_bounds)
                if (rendererBounds != null) {
                    when (flat) {
                        LinearLayout.VERTICAL -> {
                            val dy = firstY + (step * index) - rendererBounds.centerY()

                            val matrix = Matrix()
                            matrix.setTranslate(0f, dy)
                            renderer.applyTranslateMatrix(matrix, reason.apply {
                                controlType =
                                    (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_TRANSLATE)
                            }, delegate)
                        }

                        LinearLayout.HORIZONTAL -> {
                            val dx = firstX + (step * index) - rendererBounds.centerX()

                            val matrix = Matrix()
                            matrix.setTranslate(dx, 0f)
                            renderer.applyTranslateMatrix(matrix, reason.apply {
                                controlType =
                                    (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_TRANSLATE)
                            }, delegate)
                        }
                    }
                }
            }
        }

        //保存一个状态
        val redoState = PropertyStateStack()
        redoState.saveState(this, delegate)

        //回退栈
        delegate?.addStateToStack(this, undoState, redoState, false, reason, strategy)

        firstBounds.release()
        lastBounds.release()
    }

    /**创建组合
     * @return 返回一个新的群组渲染器*/
    fun groupRendererGroup(
        delegate: CanvasRenderDelegate?,
        reason: Reason,
        strategy: Strategy,
    ): BaseRenderer {
        delegate ?: return this

        if (rendererList.size() <= 1) {
            return this
        }

        //将所有子元素渲染器, 放在一个新的群组中
        val subRendererList = getSingleRendererList(false)
        val index = delegate.renderManager.elementRendererList.indexOf(rendererList.first()) //保持位置

        val groupRenderer = CanvasGroupRenderer()
        groupRenderer.resetGroupRendererList(subRendererList, Reason.init, null)

        val undoState = GroupStateStack()
        undoState.saveState(this, delegate)

        //新的数据渲染器集合
        val newElementRendererList = delegate.renderManager.elementRendererList.toMutableList()
        newElementRendererList.add(
            if (index == -1) newElementRendererList.size() else index,
            groupRenderer
        )
        newElementRendererList.remove(this)
        newElementRendererList.removeAll(rendererList)

        //回调
        delegate.dispatchRendererGroupChange(groupRenderer, subRendererList, GROUP_TYPE_GROUP)

        //通知
        delegate.renderManager.resetElementRenderer(
            newElementRendererList,
            reason,
            Strategy.preview
        )

        val redoState = GroupStateStack()
        redoState.saveState(this, delegate)

        //回退
        delegate.addStateToStack(this, undoState, redoState, false, reason, strategy)

        //重新选中元素
        delegate.selectorManager.resetSelectorRenderer(listOf(groupRenderer), Reason.user)

        return groupRenderer
    }

    /**解散组合
     * @return 返回解散后渲染器集合*/
    fun groupRendererDissolve(
        delegate: CanvasRenderDelegate?,
        reason: Reason,
        strategy: Strategy,
    ): List<BaseRenderer> {
        val subRendererList = getSingleRendererList(false)
        delegate ?: return subRendererList
        val index = delegate.renderManager.elementRendererList.indexOf(this) //保持位置

        val undoState = GroupStateStack()
        undoState.saveState(this, delegate)

        //新的数据渲染器集合
        val newElementRendererList = delegate.renderManager.elementRendererList.toMutableList()
        newElementRendererList.remove(this)
        newElementRendererList.removeAll(rendererList)
        newElementRendererList.addAll(
            if (index == -1) newElementRendererList.size() else index,
            subRendererList
        )

        //回调
        delegate.dispatchRendererGroupChange(this, subRendererList, GROUP_TYPE_DISSOLVE)

        //通知
        delegate.renderManager.resetElementRenderer(
            newElementRendererList,
            reason,
            Strategy.preview
        )

        val redoState = GroupStateStack()
        redoState.saveState(this, delegate)

        //回退
        delegate.addStateToStack(this, undoState, redoState, false, reason, strategy)

        //重新选中元素
        delegate.selectorManager.resetSelectorRenderer(subRendererList, Reason.user)

        return subRendererList
    }

    //endregion---操作---
}