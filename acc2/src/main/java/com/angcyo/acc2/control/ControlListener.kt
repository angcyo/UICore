package com.angcyo.acc2.control

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
open class ControlListener {

    /**控制器状态改变通知*/
    open fun onControlStateChanged(control: AccControl, oldState: Int, newState: Int) {

    }

    /**[ActionBean]开始运行前回调
     * [isPrimaryAction] 是否是主线的[ActionBean]*/
    open fun onActionRunBefore(actionBean: ActionBean, isPrimaryAction: Boolean) {

    }

    open fun onHandleOperate(
        handleBean: HandleBean,
        operateBean: OperateBean,
        handleResult: HandleResult
    ) {

    }

    /**[ActionBean]运行后回调
     * [handleResult] 处理结果*/
    open fun onActionRunAfter(
        actionBean: ActionBean,
        isPrimaryAction: Boolean,
        handleResult: HandleResult?
    ) {

    }

    /**控制器开始回调*/
    open fun onControlStart(control: AccControl, taskBean: TaskBean) {

    }

    /**控制器结束回调*/
    open fun onControlEnd(taskBean: TaskBean, state: Int, reason: String) {

    }
}