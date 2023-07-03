package com.angcyo.library.component.flow

import com.angcyo.library.annotation.CallPoint

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/03
 */
abstract class BaseFlow : IFlow {

    override var flowTitle: CharSequence? = null
    override var flowDes: CharSequence? = null
    override var flowState: Int = IFlow.FLOW_STATE_NONE

    override var flowStateChangedAction: (old: Int, new: Int) -> Unit = { _, _ -> }

    /**改变流程状态, 进行next
     * [IFlow.FLOW_STATE_END]*/
    @CallPoint
    override fun changeFlowState(new: Int) {
        super.changeFlowState(new)
    }

    /**完成当前的流程*/
    fun finishFlow() {
        changeFlowState(IFlow.FLOW_STATE_END)
    }
}