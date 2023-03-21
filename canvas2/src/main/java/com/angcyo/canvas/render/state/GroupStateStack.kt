package com.angcyo.canvas.render.state

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.renderer.BaseRenderer

/**
 * 群组状态
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/21
 */
class GroupStateStack : PropertyStateStack() {

    /**群组数据的存储*/
    val valueMap = hashMapOf<String, Any?>()

    val stateRendererList = mutableListOf<BaseRenderer>()

    override fun saveState(renderer: BaseRenderer, delegate: CanvasRenderDelegate?) {
        super.saveState(renderer, delegate)

        //所有元素
        stateRendererList.clear()
        delegate?.renderManager?.elementRendererList?.let {
            stateRendererList.addAll(it)
        }
    }

    override fun restoreState(
        renderer: BaseRenderer,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        super.restoreState(renderer, reason, strategy, delegate)
        delegate?.renderManager?.resetElementRenderer(stateRendererList, Strategy.preview)
        delegate?.selectorManager?.resetSelectorRenderer(stateRendererList, reason)
    }

}