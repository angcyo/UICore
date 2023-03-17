package com.angcyo.canvas.render.element

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.contains
import com.angcyo.library.ex.intersect

/**
 * 单个元素, 或者多个元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
interface IElement {

    companion object {
        val elementBoundsPath = Path()
        val elementTempRect = RectF()
    }

    //region---core---

    /**存档一个元素的状态, 后续用来恢复/重做 */
    fun createStateStack(): IStateStack

    /**更新元素, 并且支持回退*/
    fun updateElement(
        renderer: BaseRenderer?,
        delegate: CanvasRenderDelegate?,
        reason: Reason = Reason.user.apply {
            controlType = BaseControlPoint.CONTROL_TYPE_DATA
        },
        block: IElement.() -> Unit
    ) {
        renderer ?: return
        //用来恢复的状态
        val undoState = createStateStack()
        undoState.saveState(renderer)
        block()
        val redoState = createStateStack()
        redoState.saveState(renderer)
        renderer.requestUpdateDrawableAndProperty(reason, delegate)
        delegate?.addStateToStack(renderer, undoState, redoState, reason = reason)
    }

    /**请求获取元素渲染时, 相关的属性信息*/
    fun requestElementRenderProperty(): CanvasRenderProperty

    /**请求获取用来渲染在界面的[Drawable]
     * 当前的[Drawable]应该是相对于[0,0]位置绘制的*/
    fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable?

    /**元素的bounds是否完全包含point
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererContainsPoint]*/
    @Pixel
    fun elementContainsPoint(delegate: CanvasRenderDelegate?, point: PointF): Boolean {
        var result = getElementBoundsPath(delegate, elementBoundsPath).contains(point)
        if (!result) {
            val tempRect = elementTempRect
            tempRect.set(point.x, point.y, point.x + 1, point.y + 1)
            result = elementIntersectRect(delegate, tempRect) //此时使用1像素的矩形,进行碰撞
        }
        return result
    }

    /**元素的bounds是否完全包含rect
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererContainsRect]*/
    @Pixel
    fun elementContainsRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        getElementBoundsPath(delegate, elementBoundsPath).contains(rect)

    /**元素的bounds是否相交rect
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererIntersectRect]*/
    @Pixel
    fun elementIntersectRect(delegate: CanvasRenderDelegate?, rect: RectF): Boolean =
        getElementBoundsPath(delegate, elementBoundsPath).intersect(rect)

    /**获取元素用来碰撞检测的[Path]范围*/
    @Pixel
    fun getElementBoundsPath(delegate: CanvasRenderDelegate?, result: Path): Path

    /**获取元素的矩形, 宽高大小*/
    @Pixel
    fun getElementBounds(delegate: CanvasRenderDelegate?, result: RectF): RectF

    /**当编辑/操作完成后, 需要更新到对应的元素属性中*/
    fun updateElementRenderProperty(property: CanvasRenderProperty)

    /**[com.angcyo.canvas.render.renderer.BaseRenderer.isSupportControlPoint]*/
    fun isElementSupportControlPoint(type: Int): Boolean = true

    //endregion---core---

}