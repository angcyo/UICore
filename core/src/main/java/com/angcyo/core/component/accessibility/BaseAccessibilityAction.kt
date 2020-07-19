package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.CallSuper
import com.angcyo.core.component.accessibility.action.ActionCount
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.library.ex.simpleHash
import kotlin.random.Random.Default.nextLong

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
    val doActionCount = ActionCount().apply {
        maxCountLimit = 50
    }

    /**[checkOtherEvent]执行次数统计*/
    val checkOtherEventCount = ActionCount().apply {
        maxCountLimit = 10
    }

    /**[onCheckEventOut]执行统计,无法识别到界面,同时又无法back处理
     * 次数过多, 可以将[action]提到上一个级别*/
    val checkEventOutCount = ActionCount().apply {
        maxCountLimit = 3
    }

    /**回滚次数统计*/
    val rollbackCount = ActionCount().apply {
        maxCountLimit = 3
    }

    /**用于控制下一次[Action]检查执行的延迟时长, 毫秒. 负数表示使用[Interceptor]的默认值
     * 格式[5000,500,5] :5000+500*[1-5)
     * */
    var actionInterval: String? = null

    /**一个名字, 用于日志输出, 或者通知栏提示*/
    var actionTitle: String = this.simpleHash()

    //<editor-fold desc="核心回调">

    /**是否需要事件[event],返回[true]表示需要处理*/
    open fun checkEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        return false
    }

    /**当[checkEvent]没有处理时, 回调此方法进行一些处理.返回[true]表示处理了*/
    @CallSuper
    open fun checkOtherEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        checkOtherEventCount.doCount()
        return false
    }

    /**未处理[checkEvent]事件*/
    @CallSuper
    open fun onCheckEventOut(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {

        checkEventOutCount.doCount()

        if (checkEventOutCount.isMaxLimit()) {
            //界面检查超限, 开始回滚

            if (rollbackCount.isMaxLimit()) {
                //回滚超限
                doActionFinish(ActionException("回滚次数[${rollbackCount.count}]超限[max:${rollbackCount.maxCountLimit}]"))
            } else {
                checkEventOutCount.clear()
                rollbackCount.doCount()

                //执行回滚
                accessibilityInterceptor?.apply {
                    actionIndex -= 1
                }
            }
        }
    }

    /**执行action*/
    @CallSuper
    open fun doAction(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        checkOtherEventCount.clear()
        doActionCount.doCount()

        if (doActionCount.isMaxLimit()) {
            doActionFinish(ActionException("[doAction]执行次数, 已达到最大限制:${doActionCount.maxCountLimit}"))
        }
    }

    /**执行action来自其他action不需要处理, 返回true表示处理了事件*/
    open fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
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
        return accessibilityInterceptor != null && doActionCount.count > 0
    }

    //</editor-fold desc="核心回调">

    /**[Action]执行完成, 可以用于释放一些数据*/
    @CallSuper
    open fun doActionFinish(error: ActionException? = null) {
        actionFinish?.invoke(error)
        actionFinish = null
        accessibilityInterceptor = null

        doActionCount.clear()
        checkOtherEventCount.clear()
        checkEventOutCount.clear()
        rollbackCount.clear()
    }

    /**获取拦截器下一次间隔回调的时长*/
    open fun getInterceptorIntervalDelay(): Long {
        val interval = actionInterval
        val time = if (interval.isNullOrEmpty()) {
            (accessibilityInterceptor?.initialIntervalDelay ?: -1L)
        } else {
            val split = interval.split(",")
            val start = split.getOrNull(0)?.toLongOrNull()
                ?: (accessibilityInterceptor?.initialIntervalDelay
                    ?: BaseAccessibilityInterceptor.defaultIntervalDelay)
            val base = split.getOrNull(1)?.toLongOrNull() ?: 500L
            val factor = split.getOrNull(2)?.toLongOrNull() ?: 1

            start + base * nextLong(1, factor)
        }
        return time
    }
}
