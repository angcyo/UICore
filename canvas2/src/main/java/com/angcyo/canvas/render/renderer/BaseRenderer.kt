package com.angcyo.canvas.render.renderer

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.render.annotation.RenderFlag
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.element.IElement
import com.angcyo.library.ex.*

/**
 * 绘制基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */
abstract class BaseRenderer : IRenderer {

    companion object {

        /**初始位*/
        const val RENDERER_FLAG_NORMAL = 0b1

        /**当前的[BaseRenderer]渲染时, 是否需要作用[renderMatrix]
         * [com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]*/
        @RenderFlag
        const val RENDERER_FLAG_BOX_MATRIX = RENDERER_FLAG_NORMAL shl 1

        /**flag: 元素可见, 不可见不绘制*/
        @RenderFlag
        const val RENDERER_FLAG_VISIBLE = RENDERER_FLAG_BOX_MATRIX shl 1

        /**flag: 元素不锁定, 锁定后不允许touch操作*/
        @RenderFlag
        const val RENDERER_FLAG_UNLOCK = RENDERER_FLAG_VISIBLE shl 1

        /**flag: 锁定宽高比*/
        @RenderFlag
        const val RENDERER_FLAG_LOCK_SCALE = RENDERER_FLAG_UNLOCK shl 1
    }

    /**渲染器的唯一标识*/
    var uuid: String = uuid()

    /**当前类的flag标识位*/
    var renderFlags: Int = 0xfffffff

    /**渲染属性*/
    var renderProperty: CanvasRenderProperty? = null

    //region---计算属性---

    /**渲染器是否可见, 不可见不绘制
     * 由[CanvasRenderManager]控制是否绘制*/
    val isVisible: Boolean
        get() = renderFlags.have(RENDERER_FLAG_VISIBLE)

    /**渲染器是否锁定, 锁定后无法touch操作
     * 由[CanvasSelectorManager]控制是否能touch操作*/
    val isLock: Boolean
        get() = !renderFlags.have(RENDERER_FLAG_UNLOCK)

    /**是否需要锁定宽高比
     * [updateLockScaleRatio]*/
    open val isLockScaleRatio: Boolean
        get() = renderFlags.have(RENDERER_FLAG_LOCK_SCALE)

    //endregion---计算属性---

    //region---core---

    /**移除一个渲染标识*/
    open fun removeRenderFlag(flag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        updateRenderFlag(renderFlags.remove(flag), reason, delegate)
    }

    /**添加一个渲染标识*/
    open fun addRenderFlag(flag: Int, reason: Reason, delegate: CanvasRenderDelegate?) {
        updateRenderFlag(renderFlags.add(flag), reason, delegate)
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

    /**获取所有渲染器*/
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
     * */
    open fun isSupportControlPoint(type: Int): Boolean = true

    //endregion---core---

    //region---操作---

    /**更新锁定宽高比的状态
     * [isLockScaleRatio]*/
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
        addRenderFlag(CanvasElementRenderer.RENDERER_FLAG_REQUEST_PROPERTY, reason, delegate)
        delegate?.dispatchRendererPropertyChange(this, null, target, reason)
    }

    /**平移操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrixWithAnchor]
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
        addRenderFlag(CanvasElementRenderer.RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    /**缩放操作结束之后, 需要将矩阵[matrix]作用到[renderProperty]
     * [applyTranslateMatrix]
     * [applyRotateMatrix]
     * [applyScaleMatrix]
     * */
    open fun applyScaleMatrix(matrix: Matrix, reason: Reason, delegate: CanvasRenderDelegate?) {
        renderProperty?.applyScaleMatrixWithValue(matrix)
        addRenderFlag(CanvasElementRenderer.RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    /**[applyScaleMatrix]*/
    open fun applyScaleMatrixWithCenter(
        matrix: Matrix,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        renderProperty?.applyScaleMatrixWithCenter(matrix)
        addRenderFlag(CanvasElementRenderer.RENDERER_FLAG_REQUEST_DRAWABLE, reason, delegate)
        updateRenderProperty(renderProperty, reason, delegate)
    }

    //endregion---操作---

}