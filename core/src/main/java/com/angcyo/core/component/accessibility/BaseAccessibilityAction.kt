package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.ex.simpleHash
import kotlin.random.Random

/**
 * 每个无障碍拦截后需要执行的动作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

abstract class BaseAccessibilityAction {

    /**关联的拦截器*/
    var accessibilityInterceptor: BaseAccessibilityInterceptor? = null

    /**当完成了[Action], 需要调用此方法, 触发下一个[Action]*/
    var actionFinish: ((error: ActionException?) -> Unit)? = null

    /**[doAction]执行时的次数统计*/
    var actionDoCount = 0

    /**[checkEvent]执行时的次数统计, 如果check的次数过多, 可以将action提到上一个级别*/
    var actionCheckOutCount = 0

    /**当[actionCheckOutCount]大于一定值时, 回滚到上一步*/
    var rollbackCount = -1

    /**用于控制下一次[Action]检查执行的延迟时长, 毫秒. 负数表示使用[Interceptor]的默认值*/
    var actionIntervalDelay: Long = -1

    /**自动在每个[doActionFinish]结束之后, 随机调整[actionIntervalDelay]的时间*/
    var autoIntervalDelay: Boolean = true

    /**一个名字, 用于日志输出, 或者通知栏提示*/
    var actionTitle: String = this.simpleHash()

    //<editor-fold desc="核心回调">

    /**是否需要事件[event],返回true表示需要处理*/
    open fun checkEvent(service: BaseAccessibilityService, event: AccessibilityEvent?): Boolean {
        return false
    }

    /**未处理[checkEvent]事件*/
    @CallSuper
    open fun onCheckEventOut(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        actionCheckOutCount++

        if (rollbackCount in 1 until actionCheckOutCount) {
            accessibilityInterceptor?.apply {
                actionIndex -= 1
            }
            actionCheckOutCount = 0
        }
    }

    /**执行action*/
    @CallSuper
    open fun doAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        actionDoCount++
    }

    /**执行action来自其他action不需要处理, 返回true表示处理了事件*/
    open fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        return false
    }

    /**[Action]首次执行开始*/
    @CallSuper
    open fun onActionStart(interceptor: BaseAccessibilityInterceptor) {
        accessibilityInterceptor = interceptor
    }

    /**当前[Action]是否开始了*/
    open fun isActionStart(): Boolean {
        return accessibilityInterceptor != null && actionDoCount > 0
    }

    //</editor-fold desc="核心回调">

    /**[Action]执行完成, 可以用于释放一些数据*/
    @CallSuper
    open fun doActionFinish(error: ActionException? = null) {
        if (autoIntervalDelay) {
            //debug模式下关闭随机延迟, 快速测试代码
            if (!isDebugType()) {
                onRandomIntervalDelay()
            }
        }
        actionFinish?.invoke(error)
        actionFinish = null
        accessibilityInterceptor = null
        actionDoCount = 0
        actionCheckOutCount = 0
    }

    /**随机产生一个间隔时间*/
    open fun onRandomIntervalDelay() {
        randomIntervalDelay()
    }
}

/**随机间隔延迟时长*/
fun BaseAccessibilityAction.randomIntervalDelay() {
    actionIntervalDelay =
        (accessibilityInterceptor?.initialIntervalDelay ?: -1) * Random.nextInt(1, 10)
}

fun BaseAccessibilityAction.intervalDelay(): Long =
    accessibilityInterceptor?.initialIntervalDelay ?: -1
