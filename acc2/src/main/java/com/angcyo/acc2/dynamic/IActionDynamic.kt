package com.angcyo.acc2.dynamic

import androidx.annotation.Keep
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.control.AccSchedule
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult

/**
 * 直接通过代码运行的[ActionBean]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/10/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
@Keep
interface IActionDynamic {

    /**[com.angcyo.acc2.control.AccSchedule.runActionInner]*/
    fun runAction(
        accSchedule: AccSchedule,
        controlContext: ControlContext,
        actionBean: ActionBean,
        otherActionList: List<ActionBean>?,
        isPrimaryAction: Boolean,
        handleActionResult: HandleResult
    ) {
        //no op
    }

}