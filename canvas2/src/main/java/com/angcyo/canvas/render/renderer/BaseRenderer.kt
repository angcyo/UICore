package com.angcyo.canvas.render.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.CanvasOutsideCoordinate
import com.angcyo.canvas.render.annotation.RenderFlag
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.drawable.loading.CircleScaleLoadingDrawable
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.*

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

        /**请求需要重新获取[IElement]的绘制属性
         * 此标识会额外触发[com.angcyo.canvas.render.core.ICanvasRenderView.dispatchRendererPropertyChange]
         * */
        @RenderFlag
        const val RENDERER_FLAG_REQUEST_PROPERTY = RENDERER_FLAG_ASYNC shl 1

        /**请求需要重新获取[IElement]的绘制Drawable */
        @RenderFlag
        const val RENDERER_FLAG_REQUEST_DRAWABLE = RENDERER_FLAG_REQUEST_PROPERTY shl 1

        /**最后一个标识位*/
        @RenderFlag
        const val RENDERER_FLAG_LAST = RENDERER_FLAG_REQUEST_DRAWABLE shl 1
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

    //endregion---计算属性---

    //region---临时变量---

    protected val _tempRect = RectF()

    //endregion---临时变量---

    init {
        renderFlags = renderFlags.remove(RENDERER_FLAG_ASYNC)
        //updateAsync(true, Reason.init, null)//test
    }

    //region---core---

    /**渲染之前的准备工作*/
    protected open fun readyRenderIfNeed(params: RenderParams?) {

    }

    /**
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnInside]
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnOutside]
     * [com.angcyo.canvas.render.core.CanvasRenderManager.renderOnView]
     * */
    @CallPoint
    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        readyRenderIfNeed(params)
        renderProperty?.let { property ->
            renderDrawable?.let { drawable ->
                val renderBounds = property.getRenderBounds()
                canvas.withSave {
                    translate(renderBounds.left, renderBounds.top)//平移到指定位置
                    drawable.setBounds(
                        0,
                        0,
                        renderBounds.width().ceilInt(),
                        renderBounds.height().ceilInt()
                    )//设置绘制的宽高
                    drawable.draw(canvas)//绘制
                }
            }
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

    /**移除一个渲染标识*/
    open fun removeRenderFlag(flag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        updateRenderFlag(renderFlags.remove(flag), reason.apply {
            this.renderFlag = flag
        }, delegate)
    }

    /**添加一个渲染标识*/
    open fun addRenderFlag(flag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        updateRenderFlag(renderFlags.add(flag), reason.apply {
            this.renderFlag = flag
        }, delegate)
    }

    /**更新一个新的渲染标识*/
    open fun updateRenderFlag(newFlag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        val old = renderFlags
        renderFlags = newFlag
        if (old != renderFlags) {
            delegate?.dispatchRendererFlagsChange(this, old, renderFlags, reason)
        }
    }

    /**目标点[point]是否在渲染器bounds范围内
     * [point] 点位坐标, 画板内部的坐标*/
    open fun rendererContainsPoint(point: PointF): Boolean =
        getRendererBoundsPath()?.contains(point) == true

    /**渲染器bounds是否完全包含矩形[rect]
     * [rect] 矩形坐标, 画板内部的坐标*/
    open fun rendererContainsRect(rect: RectF): Boolean =
        getRendererBoundsPath()?.contains(rect) == true

    /**目标矩形[rect]是否与渲染器bounds相交
     * [rect] 矩形坐标, 画板内部的坐标*/
    open fun rendererIntersectRect(rect: RectF): Boolean =
        getRendererBoundsPath()?.intersect(rect) == true

    /**获取元素用来碰撞检测的[Path]范围*/
    open fun getRendererBoundsPath(result: Path = Path()): Path? {
        val property = renderProperty ?: return null
        val renderMatrix = property.getRenderMatrix(includeRotate = true)
        val rect = RectF(0f, 0f, property.width, property.height)
        result.rewind()
        result.addRect(rect, Path.Direction.CW)
        result.transform(renderMatrix)
        return result
    }

    /**获取渲染器对应的元素列表*/
    open fun getElementList(): List<IElement> = emptyList()

    /**获取所有渲染器, 不包含[CanvasGroupRenderer]自身*/
    open fun getRendererList(): List<BaseRenderer> {
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
     * [isLockScaleRatio]*/
    @RenderFlag
    open fun updateVisible(visible: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        if (visible) {
            addRenderFlag(RENDERER_FLAG_VISIBLE, reason, delegate)
        } else {
            removeRenderFlag(RENDERER_FLAG_VISIBLE, reason, delegate)
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

    /**更新[renderProperty]时
     * [updateRenderProperty]
     * */
    @RenderFlag
    open fun updateRenderProperty(
        target: CanvasRenderProperty?,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        if (renderProperty == null) {
            renderProperty = target
        } else {
            target?.copyTo(renderProperty)
        }
        addRenderFlag(RENDERER_FLAG_REQUEST_PROPERTY, reason, delegate)
        delegate?.dispatchRendererPropertyChange(this, null, target, reason)
    }

    /**请求更新[renderDrawable], 通知对应的状态发生了改变*/
    open fun requestUpdateDrawable(reason: Reason, delegate: CanvasRenderDelegate?) {
        addRenderFlag(RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
    }

    /**请求更新[renderProperty], 通知对应的状态发生了改变*/
    open fun requestUpdateProperty(reason: Reason, delegate: CanvasRenderDelegate?) {
        addRenderFlag(RENDERER_FLAG_REQUEST_PROPERTY, reason, delegate)
    }

    /**平移操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrix]
     * */
    open fun applyTranslateMatrix(matrix: Matrix, reason: Reason, delegate: CanvasRenderDelegate?) {
        renderProperty?.applyTranslateMatrix(matrix)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    /**旋转操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrix]
     * */
    open fun applyRotateMatrix(matrix: Matrix, reason: Reason, delegate: CanvasRenderDelegate?) {
        renderProperty?.applyRotateMatrix(matrix)
        addRenderFlag(RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    /**缩放操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrix]
     * */
    open fun applyScaleMatrix(matrix: Matrix, reason: Reason, delegate: CanvasRenderDelegate?) {
        renderProperty?.applyScaleMatrixWithValue(matrix)
        addRenderFlag(RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    /**[applyScaleMatrix]*/
    open fun applyScaleMatrixWithCenter(
        matrix: Matrix,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        renderProperty?.applyScaleMatrixWithCenter(matrix)
        addRenderFlag(RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    /**[com.angcyo.canvas.render.core.component.CanvasRenderProperty.applyFlip]*/
    open fun applyFlip(
        flipX: Boolean,
        flipY: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        renderProperty?.applyFlip(flipX, flipY)
        addRenderFlag(RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    /**平移元素[dx] [dy]本次的偏移量
     * [applyTranslateMatrix]*/
    fun translate(
        dx: Float,
        dy: Float,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        val matrix = Matrix()
        matrix.setTranslate(dx, dy)
        delegate?.undoManager?.addToStack(this, false, reason, strategy) {
            applyTranslateMatrix(matrix, reason, delegate)
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
        val px = renderProperty?.anchorX ?: 0f
        val py = renderProperty?.anchorY ?: 0f
        val matrix = Matrix()
        matrix.setScale(sx, sy, px, py)
        delegate?.undoManager?.addToStack(this, false, reason, strategy) {
            applyScaleMatrix(matrix, reason, delegate)
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
        delegate?.undoManager?.addToStack(this, false, reason, strategy) {
            applyRotateMatrix(matrix, reason, delegate)
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
        delegate?.undoManager?.addToStack(this, false, reason, strategy) {
            applyFlip(flipX, flipY, reason, delegate)
        }
    }

    /**单独翻转x轴, 遍历所有子元素, 互斥修改
     * [flip]
     * [flipX]
     * [flipY]
     * */
    fun flipX(reason: Reason, strategy: Strategy, delegate: CanvasRenderDelegate?) {
        val list = getRendererList()
        if (list.isEmpty()) return
        delegate?.undoManager?.addToStack(this, false, reason, strategy) {
            for (renderer in list) {
                val flipX: Boolean = renderer.renderProperty?.flipX ?: false
                val flipY: Boolean = renderer.renderProperty?.flipY ?: false
                renderer.applyFlip(!flipX, flipY, reason, delegate)
            }
        }
    }

    /**单独翻转y轴, 遍历所有子元素, 互斥修改
     * [flip]
     * [flipX]
     * [flipY]
     * */
    fun flipY(reason: Reason, strategy: Strategy, delegate: CanvasRenderDelegate?) {
        val list = getRendererList()
        if (list.isEmpty()) return
        delegate?.undoManager?.addToStack(this, false, reason, strategy) {
            for (renderer in list) {
                val flipX: Boolean = renderer.renderProperty?.flipX ?: false
                val flipY: Boolean = renderer.renderProperty?.flipY ?: false
                renderer.applyFlip(flipX, !flipY, reason, delegate)
            }
        }
    }

    //endregion---操作---

}