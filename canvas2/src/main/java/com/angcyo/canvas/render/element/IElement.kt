package com.angcyo.canvas.render.element

import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.core.component.ElementHitComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack

/**
 * 单个元素, 或者多个元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
interface IElement {

    /**碰撞检测*/
    var elementHitComponent: ElementHitComponent

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
        undoState.saveState(renderer, delegate)
        block()
        val redoState = createStateStack()
        redoState.saveState(renderer, delegate)
        renderer.requestUpdateDrawableAndProperty(reason, delegate)
        delegate?.addStateToStack(renderer, undoState, redoState, reason = reason)
    }

    /**请求获取元素渲染时, 相关的属性信息*/
    fun requestElementRenderProperty(): CanvasRenderProperty

    /**请求获取用来渲染在界面的[Drawable]
     * 当前的[Drawable]应该是相对于[0,0]位置绘制的*/
    fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable?


    /**当编辑/操作完成后, 需要更新到对应的元素属性中*/
    fun updateElementRenderProperty(property: CanvasRenderProperty)

    /**[com.angcyo.canvas.render.renderer.BaseRenderer.isSupportControlPoint]*/
    fun isElementSupportControlPoint(type: Int): Boolean = true

    //endregion---core---

}