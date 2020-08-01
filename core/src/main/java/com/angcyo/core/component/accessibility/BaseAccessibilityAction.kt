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

typealias ActionFinishObserve = (error: ActionException?) -> Unit

abstract class BaseAccessibilityAction {

    companion object {
        /**当前的[BaseAccessibilityAction], 允许执行[doAction]的最大次数, 超过后异常*/
        const val DEFAULT_ACTION_MAX_COUNT = 10L

        /**当前的[BaseAccessibilityAction], 执行[doAction]的次数超过此值后,强制完成*/
        const val DEFAULT_ACTION_FINISH_MAX_COUNT = -1L

        /**当前的[BaseAccessibilityAction], 允许执行[checkOtherEvent]的最大次数, 超过[actionOtherList]才有机会执行*/
        const val DEFAULT_ACTION_OTHER_MAX_COUNT = 3L

        /**[checkOtherEvent]未识别, [actionOtherList]未处理, 超过此最大次数, 会回滚到上一个[BaseAccessibilityAction]*/
        const val DEFAULT_ACTION_CHECK_OUT_MAX_COUNT = 3L

        /**允许回滚的最大次数*/
        const val DEFAULT_ROLLBACK_MAX_COUNT = 3L

        /**[ACTION_JUMP]指令, 默认允许执行的次数*/
        const val DEFAULT_JUMP_MAX_COUNT = 10L
    }

    /**关联的拦截器*/
    var accessibilityInterceptor: BaseAccessibilityInterceptor? = null

    /**当完成了[Action], 需要调用此方法, 触发下一个[Action]*/
    var _actionFinish: ((error: ActionException?) -> Unit)? = null

    /**外部使用的监听器*/
    val actionFinishObserve: MutableList<ActionFinishObserve> = mutableListOf()

    /**[doAction]执行时的次数统计*/
    val doActionCount: ActionCount = ActionCount().apply {
        maxCountLimit = DEFAULT_ACTION_MAX_COUNT
    }

    /**[checkOtherEvent]执行次数统计*/
    val checkOtherEventCount: ActionCount = ActionCount().apply {
        maxCountLimit = DEFAULT_ACTION_OTHER_MAX_COUNT
    }

    /**[onCheckEventOut]执行统计,无法识别到界面,同时又无法back处理
     * 次数过多, 可以将[action]提到上一个级别*/
    val checkEventOutCount: ActionCount = ActionCount().apply {
        maxCountLimit = DEFAULT_ACTION_CHECK_OUT_MAX_COUNT
    }

    /**回滚次数统计*/
    val rollbackCount: ActionCount = ActionCount().apply {
        maxCountLimit = DEFAULT_ROLLBACK_MAX_COUNT
    }

    /**指令[ACTION_JUMP]执行次数统计*/
    val jumpCount: ActionCount = ActionCount()

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
        //全未处理event计数
        checkEventOutCount.doCount()

        if (checkEventOutCount.isMaxLimit()) {
            //界面检查超限, 开始回滚

            if (rollbackCount.isMaxLimit()) {
                //回滚超限
                doActionFinish(ActionException("回滚次数[${rollbackCount.count}]超限[max:${rollbackCount.maxCountLimit}]"))
            } else {
                //other event 处理统计清除
                checkOtherEventCount.clear()

                //全未处理event计数
                checkEventOutCount.clear()

                //回滚计数统计
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
        //完成回调
        _actionFinish?.invoke(error)
        _actionFinish = null

        //完成外部回调
        if (actionFinishObserve.isNotEmpty()) {
            val list = ArrayList(actionFinishObserve)
            actionFinishObserve.clear()

            list.forEach {
                it.invoke(error)
            }
        }

        //清空
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
