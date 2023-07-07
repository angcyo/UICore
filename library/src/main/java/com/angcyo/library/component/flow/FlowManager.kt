package com.angcyo.library.component.flow

import androidx.annotation.WorkerThread
import com.angcyo.library.L

/**
 * 流程管理, 用于控制流程的执行
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/03
 */
class FlowManager {

    //---

    /**总共需要执行的流程*/
    val flowList = mutableListOf<IFlow>()

    /**已经完成的流程*/
    val finishFlowList = mutableListOf<IFlow>()

    /**运行失败的流程*/
    val errorFlowList = mutableListOf<IFlow>()

    /**当前流程的状态*/
    var flowState: Int = IFlow.FLOW_STATE_NONE

    /**当前正在执行的流程*/
    var _currentFlow: IFlow? = null

    /**流程是否正在运行*/
    val isRunning: Boolean
        get() = flowState == IFlow.FLOW_STATE_START || flowState == IFlow.FLOW_STATE_RUNNING

    /**是否全部流程执行完毕*/
    val isFinish: Boolean
        get() {
            var finish = true
            for (f in flowList) {
                if (finishFlowList.contains(f) || errorFlowList.contains(f)) {
                    continue
                }
                finish = false
                break
            }
            return finish
        }

    /**最后的错误*/
    var lastError: Throwable? = null

    //---

    /**子流程状态改变回调*/
    var subFlowStateChangedAction: (flow: IFlow, old: Int, new: Int) -> Unit = { _, _, _ -> }

    /**总流程状态改变回调*/
    var flowStateChangedAction: (state: Int) -> Unit = { }

    /**流程异常后, 是否拦截处理*/
    var flowErrorAction: (error: Throwable) -> Boolean = { false }

    //---

    /**添加一个流程*/
    fun addFlow(flow: IFlow) {
        if (flowList.contains(flow)) {
            return
        }
        flowList.add(flow)
    }

    /**开始执行流程*/
    @WorkerThread
    fun startFlow() {
        if (flowList.isEmpty()) {
            L.w("没有需要执行的流程")
            return
        }
        lastError = null
        changeFlowState(IFlow.FLOW_STATE_START)
        nextFlow()
    }

    /**完成流程*/
    @WorkerThread
    fun finishFlow(error: Throwable? = null) {
        lastError = error
        _currentFlow?.apply {
            if (isRunning) {
                if (error == null) {
                    finishFlowList.add(this)
                    changeFlowState(IFlow.FLOW_STATE_END)
                } else {
                    errorFlowList.add(this)
                    changeFlowState(IFlow.FLOW_STATE_ERROR)
                }
                interrupt(error ?: IllegalStateException("流程被中断"))
            }
        }
        if (error == null) {
            changeFlowState(IFlow.FLOW_STATE_END)
        } else {
            changeFlowState(IFlow.FLOW_STATE_ERROR)
        }
    }

    /**重置流程管理*/
    fun resetFlow() {
        for (flow in flowList) {
            flow.flowReset()
        }
        finishFlowList.clear()
        errorFlowList.clear()
        flowState = IFlow.FLOW_STATE_NONE
        _currentFlow = null
        lastError = null
    }

    private fun changeFlowState(new: Int) {
        val old = flowState
        flowState = new
        flowStateChangedAction(new)
    }

    @WorkerThread
    private fun nextFlow() {
        if (!isRunning) {
            return
        }
        var flow: IFlow? = null
        if (_currentFlow == null) {
            flow = flowList.firstOrNull()
        } else {
            for (f in flowList) {
                if (finishFlowList.contains(f) || errorFlowList.contains(f)) {
                    continue
                }
                flow = f
                break
            }
        }
        if (flow == null) {
            finishFlow()
        } else {
            flow.flowStateChangedAction = { old, new ->
                subFlowStateChangedAction(flow, old, new)
                if (new == IFlow.FLOW_STATE_END) {
                    L.i("流程执行完毕:${flow.flowTitle}")
                    finishFlowList.add(flow)
                    nextFlow()
                }
            }
            _currentFlow = flow
            try {
                L.d("开始执行流程:${flow.flowTitle}")
                flow.changeFlowState(IFlow.FLOW_STATE_START)
                _currentFlow?.flowRun()
            } catch (e: Exception) {
                e.printStackTrace()
                errorFlowList.add(flow)
                if (flowErrorAction(e)) {
                    //拦截后, 不再执行下一个流程
                } else {
                    nextFlow()
                }
            }
        }
    }
}