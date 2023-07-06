package com.angcyo.library.component.flow

import androidx.annotation.WorkerThread
import com.angcyo.library.annotation.CallPoint

/**
 * 流程
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/03
 */
interface IFlow {

    companion object {
        /**流程的状态*/
        const val FLOW_STATE_NONE = 0
        const val FLOW_STATE_START = 1
        const val FLOW_STATE_RUNNING = 2
        const val FLOW_STATE_END = 3
        const val FLOW_STATE_ERROR = 4
    }

    /**流程的标题*/
    var flowTitle: CharSequence?

    /**流程的描述*/
    var flowDes: CharSequence?

    /**当前流程的状态*/
    var flowState: Int

    /**最后的错误*/
    var flowLastError: Throwable?

    /**流程状态改变回调*/
    var flowStateChangedAction: (old: Int, new: Int) -> Unit

    /**流程的执行*/
    @CallPoint
    @WorkerThread
    fun flowRun()

    /**流程被中断*/
    fun interrupt(error: Throwable) {
        if (isRunning) {
            flowLastError = error
            changeFlowState(FLOW_STATE_ERROR)
        }
    }

    /**改变流程状态, 并触发下一个*/
    @CallPoint
    fun changeFlowState(new: Int) {
        val old = flowState
        flowState = new
        flowStateChangedAction(old, new)
    }
}

/**流程是否正在运行*/
val IFlow.isRunning: Boolean
    get() = flowState == IFlow.FLOW_STATE_START || flowState == IFlow.FLOW_STATE_RUNNING

/**是否全部流程执行完毕*/
val IFlow.isFinish: Boolean
    get() = flowState == IFlow.FLOW_STATE_END