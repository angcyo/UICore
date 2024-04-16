package com.angcyo.canvas.render.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.RenderFlag
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.BaseElement
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer.Companion.createRenderBitmap
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer.Companion.createRenderDrawable
import com.angcyo.canvas.render.util.renderElement
import com.angcyo.drawable.loading.CircleScaleLoadingDrawable
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.Flag
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.annotation.CanvasOutsideCoordinate
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.Strategy
import com.angcyo.library.ex.*
import kotlin.math.max

/**
 * 绘制基类
 * [com.angcyo.canvas.render.core.CanvasRenderManager]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */
abstract class BaseRenderer : IRenderer {

    companion object {

        /**初始位*/
        const val RENDERER_FLAG_NORMAL = IRenderer.RENDERER_FLAG_LAST

        /**flag: 元素可见, 不可见不绘制*/
        @RenderFlag
        const val RENDERER_FLAG_VISIBLE = RENDERER_FLAG_NORMAL shl 1

        /**flag: 元素不锁定, 锁定后不允许touch操作*/
        @RenderFlag
        const val RENDERER_FLAG_UNLOCK = RENDERER_FLAG_VISIBLE shl 1

        /**flag: 锁定宽高比*/
        @RenderFlag
        const val RENDERER_FLAG_LOCK_SCALE = RENDERER_FLAG_UNLOCK shl 1

        /**flag: 是否正在异步加载中*/
        @RenderFlag
        const val RENDERER_FLAG_ASYNC = RENDERER_FLAG_LOCK_SCALE shl 1

        /**请求需要重新获取[IElement]的绘制属性, 绘制属性改变
         * 此标识会额外触发[com.angcyo.canvas.render.core.ICanvasRenderView.dispatchRendererPropertyChange]
         * */
        @RenderFlag
        const val RENDERER_FLAG_REQUEST_PROPERTY = RENDERER_FLAG_ASYNC shl 1

        /**最后一个标识位*/
        @RenderFlag
        const val RENDERER_FLAG_LAST = RENDERER_FLAG_REQUEST_PROPERTY shl 1
    }

    /**渲染器的唯一标识*/
    var uuid: String = uuid()

    /**当前类的flag标识位*/
    override var renderFlags: Int = 0xfffffff

    /**渲染属性*/
    var renderProperty: CanvasRenderProperty? = null

    /**异步加载的动画指示器*/
    var renderAsyncDrawable: Drawable? = null

    /**需要渲染的[Drawable]*/
    var renderDrawable: Drawable? = null

    //region---计算属性---

    /**渲染器是否可见, 不可见不绘制
     * 由[com.angcyo.canvas.render.core.CanvasRenderManager]控制是否绘制*/
    val isVisible: Boolean
        get() = renderFlags.have(RENDERER_FLAG_VISIBLE)

    /**渲染器是否锁定, 锁定后无法touch操作
     * 由[com.angcyo.canvas.render.core.CanvasSelectorManager]控制是否能touch操作*/
    val isLock: Boolean
        get() = !renderFlags.have(RENDERER_FLAG_UNLOCK)

    /**是否需要锁定宽高比
     * [updateLockScaleRatio]*/
    open val isLockScaleRatio: Boolean
        get() = renderFlags.have(RENDERER_FLAG_LOCK_SCALE)

    /**是否正在异步加载中
     * [updateAsync]*/
    open val isAsync: Boolean
        get() = renderFlags.have(RENDERER_FLAG_ASYNC)

    /**选中状态标识*/
    @Flag
    var isSelected = false

    /**分组展开状态标识*/
    @Flag
    var isExpand: Boolean? = null

    //endregion---计算属性---

    //region---临时变量---
    protected val _renderRect = RectF()      //缓存的渲染矩形, 不包含旋转
    protected val _renderBounds = RectF()   //缓存的渲染bounds
    protected val _renderMatrix = Matrix()  //缓存的渲染矩阵

    protected val _tempRect = RectF()

    //endregion---临时变量---

    init {
        renderFlags = renderFlags.remove(RENDERER_FLAG_ASYNC)
        //updateAsync(true, Reason.init, null)//test
    }

    //region---core---

    /**仅在[renderOnInside]中绘制*/
    open fun onlyRenderOnInside() {
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_OUTSIDE)
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_VIEW)
        renderFlags = renderFlags.add(IRenderer.RENDERER_FLAG_ON_INSIDE)
    }

    /**仅在[renderOnOutside]中绘制*/
    open fun onlyRenderOnOutside() {
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_INSIDE)
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_VIEW)
        renderFlags = renderFlags.add(IRenderer.RENDERER_FLAG_ON_OUTSIDE)
    }

    /**仅在[renderOnView]中绘制*/
    open fun onlyRenderOnView() {
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_INSIDE)
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_OUTSIDE)
        renderFlags = renderFlags.add(IRenderer.RENDERER_FLAG_ON_VIEW)
    }

    /**[com.angcyo.canvas.render.core.component.CanvasRenderProperty.getRenderRect]*/
    @CanvasInsideCoordinate
    open fun getRendererRect(result: RectF = _renderRect, includeRotate: Boolean = false) =
        renderProperty?.getRenderRect(result, includeRotate)

    /**[com.angcyo.canvas.render.core.component.CanvasRenderProperty.getRenderBounds]*/
    @CanvasInsideCoordinate
    open fun getRendererBounds(result: RectF = _renderBounds) =
        renderProperty?.getRenderBounds(result)

    /**[getRendererBounds]*/
    @CanvasOutsideCoordinate
    open fun getRendererBoundsOutside(
        delegate: CanvasRenderDelegate?,
        result: RectF = _renderBounds
    ): RectF? {
        val renderViewBox = delegate?.renderViewBox ?: return null
        val renderBounds = getRendererBounds(result) ?: return null
        renderViewBox.transformToOutside(renderBounds, result)
        return result
    }

    /**渲染之前的准备工作
     * [com.angcyo.canvas.render.element.IElement.requestElementRenderProperty]
     * [com.angcyo.canvas.render.element.IElement.requestElementDrawable]
     * */
    open fun readyRenderIfNeed(params: RenderParams?) {
        val requestProperty = renderFlags.have(RENDERER_FLAG_REQUEST_PROPERTY)
        if (renderProperty == null || requestProperty) {
            updateRenderProperty()
        }
    }

    /**更新[renderProperty]属性*/
    open fun updateRenderProperty() {
        renderFlags = renderFlags.remove(RENDERER_FLAG_REQUEST_PROPERTY)
        renderProperty?.apply {
            getRenderRect(_renderRect)
            getRenderBounds(_renderBounds)
            getDrawMatrix(_renderMatrix, true)
        }
    }

    /**
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnInside]
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnOutside]
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnView]
     * */
    @CallPoint
    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        readyRenderIfNeed(params)
        params.renderMatrix = _renderMatrix
        renderProperty?.let {
            val renderBounds = _renderBounds
            val drawable = renderDrawable
            if (drawable != null) {
                val boundsWidth = renderBounds.width()
                val width = if (boundsWidth <= 0) {
                    //实际宽度为0, 可能是线条
                    max(drawable.minimumWidth.toFloat(), params.drawMinWidth).ceilInt()
                } else {
                    max(boundsWidth, params.drawMinWidth).ceilInt()
                }

                val boundsHeight = renderBounds.height()
                val height = if (boundsHeight <= 0f) {
                    //实际高度为0, 可能是线条
                    max(drawable.minimumHeight.toFloat(), params.drawMinHeight).ceilInt()
                } else {
                    max(boundsHeight, params.drawMinHeight).ceilInt()
                }
                drawable.setBounds(0, 0, width, height)//设置绘制的宽高
                renderDrawable(canvas, renderBounds, drawable, params)
            }
        }
    }

    /**绘制指定的数据[drawable]
     * [renderOnInside]*/
    fun renderDrawable(
        canvas: Canvas,
        renderBounds: RectF,
        drawable: Drawable,
        params: RenderParams
    ) {
        canvas.withSave {
            translate(renderBounds.left, renderBounds.top)//平移到指定位置
            params.delegate?.dispatchRenderDrawable(this@BaseRenderer, params, false)
            drawable.draw(this)//绘制
            params.delegate?.dispatchRenderDrawable(this@BaseRenderer, params, true)
        }
    }

    /**
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnInside]
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnOutside]
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnView]
     * */
    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        readyRenderIfNeed(params)
        if (isAsync || params.delegate?.asyncManager?.hasAsyncTask(uuid) == true) {
            renderAsyncLoading(canvas, params)
        }
    }

    override fun renderOnView(canvas: Canvas, params: RenderParams) {
        readyRenderIfNeed(params)
    }

    /**异步动画绘制*/
    @CanvasOutsideCoordinate
    private fun renderAsyncLoading(canvas: Canvas, params: RenderParams) {
        val renderViewBox = params.delegate?.renderViewBox ?: return
        renderAsyncDrawable?.let { drawable ->
            renderProperty?.let { property ->
                val rect = property.getRenderRect(_tempRect)
                renderViewBox.transformToOutside(rect)

                val cx = rect.centerX()
                val cy = rect.centerY()

                val w = drawable.intrinsicWidth
                val h = drawable.intrinsicHeight

                drawable.setBounds(
                    (cx - w / 2).toInt(), (cy - h / 2).toInt(),
                    (cx + w / 2).toInt(), (cy + h / 2).toInt()
                )
                drawable.draw(canvas)

                params.delegate?.refresh()
            }
        }
    }

    /**无数据时, 渲染提示信息*/
    protected fun renderNoDrawable(canvas: Canvas, params: RenderParams) {
        renderElement?.let {
            if (it is BaseElement) {
                it.renderNoData(canvas, params)
            }
        }
    }

    /**移除一个渲染标识*/
    open fun removeRenderFlag(flag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        updateRenderFlags(renderFlags.remove(flag), reason.apply {
            renderFlag = (renderFlag ?: 0).add(flag)
        }, delegate)
    }

    /**添加一个渲染标识*/
    open fun addRenderFlag(flag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        updateRenderFlags(renderFlags.add(flag), reason.apply {
            renderFlag = (renderFlag ?: 0).add(flag)
        }, delegate)
    }

    /**更新一个新的渲染标识*/
    open fun updateRenderFlags(newFlags: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        val old = renderFlags
        renderFlags = newFlags
        if (old != renderFlags || reason.controlType != null) {
            delegate?.dispatchRendererFlagsChange(this, old, renderFlags, reason)
        }
    }

    /**目标点[point]是否在渲染器bounds范围内
     * [point] 点位坐标, 画板内部的坐标*/
    open fun rendererContainsPoint(delegate: CanvasRenderDelegate?, point: PointF): Boolean =
        getRendererBoundsPath(delegate)?.contains(point) == true

    /**渲染器bounds是否完全包含矩形[rect]
     * [rect] 矩形坐标, 画板内部的坐标*/
    open fun rendererContainsRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        getRendererBoundsPath(delegate)?.contains(rect) == true

    /**目标矩形[rect]是否与渲染器bounds相交
     * [rect] 矩形坐标, 画板内部的坐标*/
    open fun rendererIntersectRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        getRendererBoundsPath(delegate)?.intersect(rect) == true

    /**获取元素用来碰撞检测的[Path]范围*/
    open fun getRendererBoundsPath(delegate: CanvasRenderDelegate?, result: Path = Path()): Path? {
        val property = renderProperty ?: return null
        val renderMatrix = property.getRenderMatrix(includeRotate = true)
        val rect = RectF(0f, 0f, property.width, property.height)
        result.rewind()
        result.addRect(rect, Path.Direction.CW)
        result.transform(renderMatrix)
        return result
    }

    /**获取元素用来输出的路径, 用来实现矢量预览
     * 非矢量元素, 应高再次获取[getRendererBoundsPath]路径
     * */
    open fun getRendererOutputPath(): List<Path>? = null

    /**查找元素*/
    open fun findRendererByUuid(uuid: String?): BaseRenderer? {
        if (uuid != null && this.uuid == uuid) {
            return this
        }
        return null
    }

    /**获取渲染器对应的元素列表*/
    open fun getSingleElementList(): List<IElement> = emptyList()

    /**获取所有渲染器, 会包含所有的渲染器. 可见的, 不可见的, 锁定的.
     * [includeGroup] 是否要包含[CanvasGroupRenderer]自身
     * */
    open fun getSingleRendererList(includeGroup: Boolean): List<BaseRenderer> {
        val result = mutableListOf<BaseRenderer>()
        result.add(this)
        return result
    }

    /**是否支持指定的控制点操作
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_DELETE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_ROTATE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_SCALE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_LOCK]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_WIDTH]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_HEIGHT]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_KEEP_GROUP_PROPERTY]
     * */
    open fun isSupportControlPoint(type: Int): Boolean = true

    /**获取渲染器用来渲染的[Drawable]
     * [requestRenderBitmap]*/
    open fun requestRenderDrawable(overrideSize: Float? = null): Drawable? =
        createRenderDrawable(getSingleRendererList(false), overrideSize, ignoreVisible = true)

    /**获取渲染器用来渲染的[Bitmap]
     * [requestRenderDrawable]
     * */
    open fun requestRenderBitmap(
        overrideSize: Float? = null,
        backgroundColor: Int = Color.TRANSPARENT
    ): Bitmap? = createRenderBitmap(
        getSingleRendererList(false),
        overrideSize,
        backgroundColor = backgroundColor
    )

    /**是否在当前的可视坐标范围内可见
     * [fullIn] 是否要全部可见, 否则露出一部分也视为可见*/
    open fun isVisibleInRender(
        delegate: CanvasRenderDelegate?,
        fullIn: Boolean = false,
        def: Boolean = true
    ): Boolean {
        delegate ?: return def
        val bounds = _renderBounds
        return delegate.renderViewBox.isVisibleInRenderBox(bounds, fullIn)
    }

    //endregion---core---

    //region---操作---

    /**更新渲染器的异步状态
     * [isLockScaleRatio]*/
    @RenderFlag
    open fun updateAsync(async: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        if (async) {
            if (renderAsyncDrawable == null) {
                renderAsyncDrawable = CircleScaleLoadingDrawable().apply {
                    circleFromColor = _color(R.color.canvas_render_async)
                    fillStyle = true
                    startLoading()
                }
            }
            addRenderFlag(RENDERER_FLAG_ASYNC, reason, delegate)
        } else {
            removeRenderFlag(RENDERER_FLAG_ASYNC, reason, delegate)
        }
    }

    /**更新渲染器的可见状态
     * [isVisible]*/
    @RenderFlag
    open fun updateVisible(visible: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        if (visible) {
            addRenderFlag(RENDERER_FLAG_VISIBLE, reason, delegate)
        } else {
            removeRenderFlag(RENDERER_FLAG_VISIBLE, reason, delegate)
        }
    }

    /**更新渲染器的可见状态
     * [isLock]*/
    @RenderFlag
    open fun updateLock(lock: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        if (lock) {
            removeRenderFlag(RENDERER_FLAG_UNLOCK, reason, delegate)
        } else {
            addRenderFlag(RENDERER_FLAG_UNLOCK, reason, delegate)
        }
    }

    /**更新锁定宽高比的状态
     * [isLockScaleRatio]*/
    @RenderFlag
    open fun updateLockScaleRatio(lock: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        if (lock) {
            addRenderFlag(RENDERER_FLAG_LOCK_SCALE, reason, delegate)
        } else {
            removeRenderFlag(RENDERER_FLAG_LOCK_SCALE, reason, delegate)
        }
    }

    /**更新[renderProperty]时触发
     * [updateRenderPropertyTo]
     *
     * [com.angcyo.canvas.render.renderer.CanvasElementRenderer.updateRenderPropertyTo]
     * */
    @RenderFlag
    open fun updateRenderPropertyTo(
        target: CanvasRenderProperty?,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        if (renderProperty == null) {
            renderProperty = CanvasRenderProperty()
        }
        if (renderProperty != target) {
            target?.copyTo(renderProperty)
        }
        requestUpdatePropertyFlag(reason, delegate)
        delegate?.dispatchRendererPropertyChange(this, null, target, reason)
    }

    /**请求更新[renderProperty], 通知对应的状态发生了改变*/
    @RenderFlag
    open fun requestUpdatePropertyFlag(reason: Reason, delegate: CanvasRenderDelegate?) {
        addRenderFlag(RENDERER_FLAG_REQUEST_PROPERTY, reason, delegate)

        //更新_renderBounds缓存, 以便在 com.angcyo.canvas.render.renderer.BaseRenderer.isVisibleInRender 中判断及时生效
        getRendererBounds()
    }

    /**平移操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrix]
     * */
    open fun applyTranslateMatrix(matrix: Matrix, reason: Reason, delegate: CanvasRenderDelegate?) {
        reason.controlType = (reason.controlType
            ?: 0).add(BaseControlPoint.CONTROL_TYPE_DATA or BaseControlPoint.CONTROL_TYPE_TRANSLATE)
        delegate?.dispatchApplyMatrix(
            this,
            matrix,
            BaseControlPoint.CONTROL_TYPE_TRANSLATE
        )
        renderProperty?.applyTranslateMatrix(matrix)
        updateRenderPropertyTo(renderProperty, reason, delegate)
    }

    /**旋转操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrix]
     * */
    open fun applyRotateMatrix(matrix: Matrix, reason: Reason, delegate: CanvasRenderDelegate?) {
        reason.controlType = (reason.controlType
            ?: 0).add(BaseControlPoint.CONTROL_TYPE_DATA or BaseControlPoint.CONTROL_TYPE_ROTATE)
        delegate?.dispatchApplyMatrix(this, matrix, BaseControlPoint.CONTROL_TYPE_ROTATE)
        renderProperty?.applyRotateMatrix(matrix)
        updateRenderPropertyTo(renderProperty, reason, delegate)
    }

    /**缩放操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrix]
     * */
    open fun applyScaleMatrix(matrix: Matrix, reason: Reason, delegate: CanvasRenderDelegate?) {
        reason.controlType = (reason.controlType
            ?: 0).add(BaseControlPoint.CONTROL_TYPE_DATA or BaseControlPoint.CONTROL_TYPE_SCALE)
        delegate?.dispatchApplyMatrix(
            this,
            matrix,
            BaseControlPoint.CONTROL_TYPE_SCALE
        )
        renderProperty?.applyScaleMatrixWithValue(matrix)
        updateRenderPropertyTo(renderProperty, reason, delegate)
    }

    /**[applyScaleMatrix]*/
    open fun applyScaleMatrixWithCenter(
        matrix: Matrix,
        useQr: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        reason.controlType = (reason.controlType
            ?: 0).add(BaseControlPoint.CONTROL_TYPE_DATA or BaseControlPoint.CONTROL_TYPE_ROTATE)
        delegate?.dispatchApplyMatrix(this, matrix, BaseControlPoint.CONTROL_TYPE_SCALE)
        renderProperty?.applyScaleMatrixWithCenter(matrix, useQr)
        updateRenderPropertyTo(renderProperty, reason, delegate)
    }

    /**[com.angcyo.canvas.render.core.component.CanvasRenderProperty.applyFlip]*/
    open fun applyFlip(
        flipX: Boolean,
        flipY: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        reason.controlType = (reason.controlType
            ?: 0).add(BaseControlPoint.CONTROL_TYPE_DATA or BaseControlPoint.CONTROL_TYPE_FLIP)
        renderProperty?.applyFlip(flipX, flipY)
        updateRenderPropertyTo(renderProperty, reason, delegate)
    }

    //---

    /**平移元素[dx] [dy]本次的偏移量
     * [applyTranslateMatrix]*/
    @Pixel
    fun translate(
        dx: Float,
        dy: Float,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val matrix = Matrix()
        matrix.setTranslate(dx, dy)
        if (delegate == null) {
            applyTranslateMatrix(matrix, reason, null)
        } else {
            delegate.undoManager.addToStack(this, false, reason, strategy) {
                applyTranslateMatrix(matrix, reason, delegate)
            }
        }
    }

    /**缩放元素[sx] [sy]本次的缩放比例
     * [applyScaleMatrix]*/
    fun scale(
        sx: Float,
        sy: Float,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        if (sx == 0f || sy == 0f) {
            return
        }

        val px = renderProperty?.anchorX ?: 0f
        val py = renderProperty?.anchorY ?: 0f
        val matrix = Matrix()
        matrix.setScale(sx.ensure(1f), sy.ensure(1f), px, py)
        if (delegate == null) {
            applyScaleMatrix(matrix, reason, null)
        } else {
            delegate.undoManager.addToStack(this, false, reason, strategy) {
                applyScaleMatrix(matrix, reason, delegate)
            }
        }
    }

    /**直接设置旋转多少度, 角度单位
     * [rotateBy]*/
    fun rotate(
        value: Float,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val by = value - (renderProperty?.angle ?: 0f)
        rotateBy(by, reason, strategy, delegate)
    }

    /**在原有的基础上, 额外旋转多少度, 角度单位
     * [applyRotateMatrix]*/
    fun rotateBy(
        value: Float,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val center = renderProperty?.getRenderCenter()
        val px = center?.x ?: 0f
        val py = center?.y ?: 0f
        val matrix = Matrix()
        matrix.setRotate(value, px, py)
        if (delegate == null) {
            applyRotateMatrix(matrix, reason, null)
        } else {
            delegate.undoManager.addToStack(this, false, reason, strategy) {
                applyRotateMatrix(matrix, reason, delegate)
            }
        }
    }

    /**翻转元素
     * [applyFlip]
     * [flip]
     * [flipX]
     * [flipY]
     * */
    fun flip(
        flipX: Boolean = renderProperty?.flipX ?: false,
        flipY: Boolean = renderProperty?.flipY ?: false,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        if (delegate == null) {
            applyFlip(flipX, flipY, reason, null)
        } else {
            delegate.undoManager.addToStack(this, false, reason, strategy) {
                applyFlip(flipX, flipY, reason, delegate)
            }
        }
    }

    /**单独翻转x轴, 遍历所有子元素, 互斥修改
     * [flip]
     * [flipX]
     * [flipY]
     * */
    fun flipX(reason: Reason, strategy: Strategy, delegate: CanvasRenderDelegate?) {
        val list = getSingleRendererList(false)
        if (list.isEmpty()) return

        if (delegate == null) {
            for (renderer in list) {
                val flipX: Boolean = renderer.renderProperty?.flipX ?: false
                val flipY: Boolean = renderer.renderProperty?.flipY ?: false
                renderer.applyFlip(!flipX, flipY, reason, null)
            }
        } else {
            delegate.undoManager.addToStack(this, false, reason, strategy) {
                for (renderer in list) {
                    val flipX: Boolean = renderer.renderProperty?.flipX ?: false
                    val flipY: Boolean = renderer.renderProperty?.flipY ?: false
                    renderer.applyFlip(!flipX, flipY, reason, delegate)
                }
            }
        }
    }

    /**单独翻转y轴, 遍历所有子元素, 互斥修改
     * [flip]
     * [flipX]
     * [flipY]
     * */
    fun flipY(reason: Reason, strategy: Strategy, delegate: CanvasRenderDelegate?) {
        val list = getSingleRendererList(false)
        if (list.isEmpty()) return

        if (delegate == null) {
            for (renderer in list) {
                val flipX: Boolean = renderer.renderProperty?.flipX ?: false
                val flipY: Boolean = renderer.renderProperty?.flipY ?: false
                renderer.applyFlip(flipX, !flipY, reason, null)
            }
        } else {
            delegate.undoManager.addToStack(this, false, reason, strategy) {
                for (renderer in list) {
                    val flipX: Boolean = renderer.renderProperty?.flipX ?: false
                    val flipY: Boolean = renderer.renderProperty?.flipY ?: false
                    renderer.applyFlip(flipX, !flipY, reason, delegate)
                }
            }
        }
    }

    /**将元素的左上移动至指定位置*/
    @Pixel
    fun translateLeftTo(
        x: Float,
        y: Float,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val bounds = getRendererBounds()
        val dx = x - (bounds?.left ?: 0f)
        val dy = y - (bounds?.top ?: 0f)
        translate(dx, dy, reason, strategy, delegate)
        updateRenderProperty()
    }

    /**将元素的中点移动至指定位置*/
    @Pixel
    fun translateCenterTo(
        cx: Float,
        cy: Float,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val bounds = getRendererBounds()
        val dx = cx - (bounds?.centerX() ?: 0f)
        val dy = cy - (bounds?.centerY() ?: 0f)
        translate(dx, dy, reason, strategy, delegate)
        updateRenderProperty()
    }

    //endregion---操作---

}