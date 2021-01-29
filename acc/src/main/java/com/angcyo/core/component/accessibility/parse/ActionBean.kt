package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.BaseAccessibilityAction
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_CHECK_OUT_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_FINISH_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_LEAVE_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ACTION_OTHER_MAX_COUNT
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.DEFAULT_ROLLBACK_MAX_COUNT
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.parse.ActionBean.Companion.HANDLE_TYPE_NONE

/**
 * 每个任务由多个action构建, 每个action由actionBean构建
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class ActionBean(

    /**标识[ActionBean]*/
    var actionId: Long = -1,

    /**是否激活[Action], 不激活不会被添加到[actionList]中*/
    var enable: Boolean = true,

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

    /**默认当前页面检测x次, 都还不通过. 回退到上一步, 触发指令[rollback]
     * [com/angcyo/core/component/accessibility/parse/CheckBean.kt:48]*/
    var rollbackCount: Long = DEFAULT_ACTION_CHECK_OUT_MAX_COUNT,

    /**回滚x次后, 还是不通过, 则报错*/
    var rollbackMaxCount: Long = DEFAULT_ROLLBACK_MAX_COUNT,

    /**[checkOtherEvent]允许执行的最大次数, 触发指令[otherOut]
     * [com/angcyo/core/component/accessibility/parse/CheckBean.kt:51]*/
    var checkOtherCount: Long = DEFAULT_ACTION_OTHER_MAX_COUNT,

    /**允许[doAction]执行的最大次数, 超过后抛出异常, 触发指令[doAction]
     * [com/angcyo/core/component/accessibility/parse/CheckBean.kt:54]*/
    var actionMaxRunCount: Long = DEFAULT_ACTION_MAX_COUNT,

    /**当action识别到并处理执行后的次数大于此值时, 强制完成*/
    var actionMaxCount: Long = DEFAULT_ACTION_FINISH_MAX_COUNT,

    /**当拦截器离开主程序界面多少次后,触发指令[leave]
     * [com/angcyo/core/component/accessibility/parse/CheckBean.kt:57]
     * */
    var leaveCount: Long = DEFAULT_ACTION_LEAVE_COUNT,

    /**当前action执行完成后, 间隔多久执行下一个[Action]. 毫秒
     * 格式[5000,500,5] 解释:5000+500*[1-5),
     * null/空字符:表示设备性能对应的默认值*/
    var interval: String? = null,

    /**等同于[interval]
     * 不同在于[start]用来控制自身执行的延迟
     * */
    var start: String? = null,

    /**[Action]完成后, 需要执行的网络请求*/
    var form: FormBean? = null,

    /**[com.angcyo.core.component.accessibility.parse.ConstraintBean.ACTION_TASK]需要启动的[FormBean]*/
    var task: FormBean? = null,

    /**getText获取到的文本, 需要放在表单的那个key中*/
    var formKey: String? = null,

    /**当[BaseAccessibilityAction]执行异常时, 处理的方式*/
    var errorHandleType: Int = ERROR_HANDLE_TYPE_STOP,

    /**[com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor.getOnlyFilterTopWindow]*/
    var onlyTopWindow: Boolean = false
) {
    companion object {

        /**[handle]中[constraintList]的处理方式*/
        const val HANDLE_TYPE_NONE = 0    //匹配执行, 匹配到谁, 谁就执行
        const val HANDLE_TYPE_RANDOM = 1  //随机执行, 这个模式下, [actionMaxCount]也会随机设置
        const val HANDLE_TYPE_ORDER = 2   //顺序执行, 根据[doActionCount]计数
        const val HANDLE_TYPE_ORDER2 = 3  //累积顺序执行, 不清空计数

        const val ERROR_HANDLE_TYPE_NEXT = 1  //异常后, 继续流程
        const val ERROR_HANDLE_TYPE_STOP = -1 //异常后, 停止处理
    }
}

/**转成可以用于执行的[AutoParseAction]*/
fun ActionBean.toAction(packageName: String): AutoParseAction {
    return AutoParseAction().apply {
        //当前的[BaseAccessibilityAction], 允许执行[doAction]的最大次数, 超过后异常
        doActionCount.maxCountLimit = this@toAction.actionMaxRunCount

        //[checkOtherEvent]未识别, [actionOtherList]未处理, 超过此最大次数, 会回滚到上一个[BaseAccessibilityAction]
        checkEventOutCount.maxCountLimit = this@toAction.rollbackCount

        //允许回滚的最大次数
        rollbackCount.maxCountLimit = this@toAction.rollbackMaxCount

        //当前的[BaseAccessibilityAction], 允许执行[checkOtherEvent]的最大次数, 超过[actionOtherList]才有机会执行
        checkOtherEventCount.maxCountLimit = this@toAction.checkOtherCount

        actionBean = this@toAction

        //设置 AutoParseAction 的 actionTitle
        title?.let { actionTitle = "$it($actionId)" }

        //时间间隔
        if (!interval.isNullOrEmpty()) {
            actionInterval = interval
        }

        //使用 id时, 的包名前缀
        autoParser.idPackageName = packageName

        //日志输出
        actionLog
    }
}