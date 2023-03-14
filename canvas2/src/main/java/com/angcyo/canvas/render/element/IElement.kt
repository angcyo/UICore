package com.angcyo.canvas.render.element

import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer

/**
 * 单个元素, 或者多个元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
interface IElement {

    //region---core---

    /**存档一个元素的状态, 后续用来恢复/重做*/
    fun createStateStack(renderer: BaseRenderer): IStateStack

    /**请求获取元素渲染时, 相关的属性信息*/
    fun requestElementRenderProperty(): CanvasRenderProperty

    /**请求获取用来渲染在界面的[Drawable]
     * 当前的[Drawable]应该是相对于[0,0]位置绘制的*/
    fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable?

    /**元素的bounds是否完全包含point
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererContainsPoint]*/
    fun elementContainsPoint(point: PointF): Boolean

    /**元素的bounds是否完全包含rect
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererContainsRect]*/
    fun elementContainsRect(rect: RectF): Boolean

    /**元素的bounds是否相交rect
     * [com.angcyo.canvas.render.renderer.BaseRenderer.rendererIntersectRect]*/
    fun elementIntersectRect(rect: RectF): Boolean

    /**当编辑/操作完成后, 需要更新到对应的元素属性中*/
    fun updateElementRenderProperty(property: CanvasRenderProperty)

    //endregion---core---

}