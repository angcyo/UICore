package com.angcyo.acc2.control

import com.angcyo.selenium.auto.Count

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/20
 */
class ActionCount {
    /**运行次数, 进行了多少次运行调度, 调度之前就统计了
     * [com.angcyo.acc2.auto.ActionSchedule.scheduleAction]*/
    val runCount = Count()

    /**跳转次数, 进行了多少次[com.angcyo.acc2.auto.action.Action.ACTION_JUMP]指令*/
    val jumpCount = Count()
}