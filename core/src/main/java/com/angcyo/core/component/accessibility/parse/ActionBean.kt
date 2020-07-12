package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.library.ex.isDebugType

/**
 * 每个任务由多个action构建, 每个action由actionBean构建
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class ActionBean(

    /**每个action对应的描述title*/
    var title: String? = null,

    /**界面识别所需要解析数据bean
     * 是否需要当前事件[checkEvent]解析时的关键数据
     * */
    var event: List<ConstraintBean>? = null,

    /**识别到界面到, 触发事件的解析数据结构
     * 目中目标界面后[doAction]解析时的关键数据*/
    var handle: List<ConstraintBean>? = null,

    /**当这个界面需要被返回时, 点击关闭的数据解析结构. 如果这个值不为空, 那么次Action会被添加到拦截器的otherList
     * 当[Action]被作为回退处理时[doActionWidth]解析时的关键数据*/
    var back: List<ConstraintBean>? = null,

    /**随机从[handle]列表中, 取出一个约束进行处理操作
     * [actionMaxCount] 也会进行随机变化*/
    var randomHandle: Boolean = false,

    /**默认当前页面检测x次, 都还不通过. 回退到上一步*/
    var rollbackCount: Int = 3,

    /**回滚x次后, 还是不通过, 则报错*/
    var rollbackMaxCount: Int = 3,

    /**当前action执行完成后, 间隔多久执行下一个[Action]. 毫秒
     * 格式[5000,500,5] 解释:5000+500*[1-5) */
    var interval: String? = null,

    /**当action执行次数大于此值时, 强制完成*/
    var actionMaxCount: Int = -1
)

/**转成可以用于执行的[AutoParseAction]*/
fun ActionBean.toAction(packageName: String): AutoParseAction {
    return AutoParseAction().apply {
        title?.let { actionTitle = it }
        rollbackCount = this@toAction.rollbackCount
        rollbackMaxCount = this@toAction.rollbackMaxCount

        actionBean = this@toAction

        if (!isDebugType()) {
            actionInterval = interval
        }

        autoParse.idPackageName = packageName

        //日志输出
        onLogPrint
    }
}