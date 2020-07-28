package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_CHECK_OUT_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_FINISH_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_OTHER_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ROLLBACK_MAX_COUNT
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
    var rollbackCount: Int = DEFAULT_ACTION_CHECK_OUT_MAX_COUNT,

    /**[checkOtherEvent]允许执行的最大次数*/
    var checkOtherCount: Int = DEFAULT_ACTION_OTHER_MAX_COUNT,

    /**回滚x次后, 还是不通过, 则报错*/
    var rollbackMaxCount: Int = DEFAULT_ROLLBACK_MAX_COUNT,

    /**当前action执行完成后, 间隔多久执行下一个[Action]. 毫秒
     * 格式[5000,500,5] 解释:5000+500*[1-5),
     * null/空字符:表示设备性能对应的默认值*/
    var interval: String? = null,

    /**允许[doAction]执行的最大次数, 超过后抛出异常*/
    var actionMaxRunCount: Int = DEFAULT_ACTION_MAX_COUNT,

    /**当action识别到并处理执行后的次数大于此值时, 强制完成*/
    var actionMaxCount: Int = DEFAULT_ACTION_FINISH_MAX_COUNT,

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

        //当前的[BaseAccessibilityAction], 允许执行[doAction]的最大次数, 超过后异常
        doActionCount.maxCountLimit = this@toAction.actionMaxRunCount

        //[checkOtherEvent]未识别, [actionOtherList]未处理, 超过此最大次数, 会回滚到上一个[BaseAccessibilityAction]
        checkEventOutCount.maxCountLimit = this@toAction.rollbackCount

        //允许回滚的最大次数
        rollbackCount.maxCountLimit = this@toAction.rollbackMaxCount

        //当前的[BaseAccessibilityAction], 允许执行[checkOtherEvent]的最大次数, 超过[actionOtherList]才有机会执行
        checkOtherEventCount.maxCountLimit = this@toAction.checkOtherCount

        actionBean = this@toAction

        //时间间隔
        if (!isDebugType()) {
            actionInterval = interval
        }

        //使用 id时, 的包名前缀
        autoParser.idPackageName = packageName

        //日志输出
        onLogPrint
    }
}