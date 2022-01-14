package com.angcyo.acc2.control

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.bean.OperateBean
import com.angcyo.acc2.bean.TaskBean
import com.angcyo.acc2.parse.HandleResult

/**
 * 有可能在子线程回调
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface ControlListener {

    /**控制器状态改变通知*/
    fun onControlStateChanged(control: AccControl, oldState: Int, newState: Int) {

    }

    /**[ActionBean]开始运行前回调
     * [isPrimaryAction] 是否是主线的[ActionBean]*/
    fun onActionRunBefore(
        control: AccControl,
        actionBean: ActionBean,
        isPrimaryAction: Boolean
    ) {

    }

    /**主线[actionBean]运行时, 是否离开了目标程序窗口
     * [isPrimaryAction] 是否是主线任务
     * [leave] 是否不在主程序内*/
    fun onActionLeave(
        control: AccControl,
        actionBean: ActionBean,
        isPrimaryAction: Boolean,
        leave: Boolean
    ) {

    }

    /**当[actionBean]没有被有效处理时, 回调*/
    fun onActionNoHandle(
        control: AccControl,
        actionBean: ActionBean,
        isPrimaryAction: Boolean,
    ) {

    }

    /**
     * 操作记录解析回调
     * [com.angcyo.acc2.parse.OperateParse.parse]
     * */
    fun onHandleOperate(
        control: AccControl,
        handleBean: HandleBean,
        operateBean: OperateBean,
        handleResult: HandleResult
    ) {

    }

    /**
     * [HandleBean]处理完之后的回调
     * [com.angcyo.acc2.parse.HandleParse.parse]
     * */
    fun onHandleAction(
        controlContext: ControlContext,
        control: AccControl,
        handleBean: HandleBean,
        handleResult: HandleResult
    ) {

    }

    /**[ActionBean]运行后回调
     * [handleResult] 处理结果*/
    fun onActionRunAfter(
        control: AccControl,
        actionBean: ActionBean,
        isPrimaryAction: Boolean,
        handleResult: HandleResult
    ) {

    }

    /**控制器开始回调*/
    fun onControlStart(control: AccControl, taskBean: TaskBean) {

    }

    /**控制器结束回调*/
    fun onControlEnd(control: AccControl, taskBean: TaskBean, state: Int, reason: String?) {

    }

    /**控制器线程开始执行*/
    fun onControlThreadStart(control: AccControl) {

    }

    /**控制器线程调度*/
    fun onControlThreadSchedule(control: AccControl) {

    }

    /**控制器线程结束执行*/
    fun onControlThreadEnd(control: AccControl) {

    }

    /**事件通知[com.angcyo.acc2.action.EventAction] */
    fun onActionEvent(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String,
        event: String
    ) {

    }

    /**当创建动态class时, 的回调
     * [com.angcyo.acc2.dynamic.IHandleActionDynamic]
     * [com.angcyo.acc2.dynamic.IHandleDynamic]
     * [com.angcyo.acc2.dynamic.IInputProvider]
     * [com.angcyo.acc2.dynamic.ITaskDynamic]
     * */
    fun onCreateDynamicObj(obj: Any) {

    }

}