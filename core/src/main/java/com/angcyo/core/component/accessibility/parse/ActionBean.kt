package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.parse.ActionBean.Companion.HANDLE_TYPE_NONE
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

    /**每个action对应的描述概要, 多用于浮窗提示*/
    var summary: String? = null,

    /**元素解析*/
    var check: CheckBean? = null,

    /**未指定[check]时, 可以通过[checkId]在[check]库中根据id查找对应的[CheckBean]*/
    var checkId: Long = -1,

    /**[handle]处理项的处理方式
     * 默认: [HANDLE_TYPE_NONE] 匹配执行, 匹配到谁, 谁执行.*/
    var handleType: Int = HANDLE_TYPE_NONE,

    /**默认当前页面检测x次, 都还不通过. 回退到上一步*/
    var rollbackCount: Int = 3,

    /**回滚x次后, 还是不通过, 则报错*/
    var rollbackMaxCount: Int = 3,

    /**当前action执行完成后, 间隔多久执行下一个[Action]. 毫秒
     * 格式[5000,500,5] 解释:5000+500*[1-5),
     * null 表示设备性能对应的默认值*/
    var interval: String? = null,

    /**允许[doAction]执行的最大次数, 超过后抛出异常*/
    var actionMaxRunCount: Int = 50,

    /**当action识别到并处理执行后的次数大于此值时, 强制完成*/
    var actionMaxCount: Int = -1,

    /**getText获取到的文本, 需要放在表单的那个key中*/
    var formKey: String? = null
) {
    companion object {
        const val HANDLE_TYPE_NONE = 0 //匹配执行, 匹配到谁, 谁就执行
        const val HANDLE_TYPE_RANDOM = 1 //随机执行
        const val HANDLE_TYPE_ORDER = 2 //顺序执行
    }
}

/**转成可以用于执行的[AutoParseAction]*/
fun ActionBean.toAction(packageName: String): AutoParseAction {
    return AutoParseAction().apply {
        title?.let { actionTitle = it }

        doActionCount.maxCountLimit = this@toAction.actionMaxRunCount
        checkEventOutCount.maxCountLimit = this@toAction.rollbackCount
        rollbackCount.maxCountLimit = this@toAction.rollbackMaxCount

        actionBean = this@toAction

        if (!isDebugType()) {
            actionInterval = interval
        }

        autoParser.idPackageName = packageName

        //日志输出
        onLogPrint
    }
}