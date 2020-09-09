package com.angcyo.core.component.accessibility.base

import android.graphics.Color
import com.angcyo.core.R
import com.angcyo.core.component.accessibility.BaseAccessibilityAction
import com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.isActionStart
import com.angcyo.library.ex._color

/**
 * 自动显示浮窗的拦截器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseFloatInterceptor : BaseAccessibilityInterceptor() {

//    /**显示通知*/
//    open fun notify(title: CharSequence? = null, content: CharSequence? = null) {
//        if (!actionStatus.isActionInit()) {
//            sendNotify("${title}($actionIndex/${actionList.size})", content)
//        } else {
//            sendNotify(title, content)
//        }
//    }

    /**浮窗日志输出*/
    var onWindowLog: ((text: CharSequence?, summary: CharSequence?, duration: Long) -> Unit)? =
        { text, summary, duration ->
            AccessibilityWindowLayer.show(
                text, summary, duration,
                if (_isInFilterPackageNameApp) Color.WHITE else _color(R.color.warning)
            )
        }

    /**描述概要*/
    val currentActionSummary: CharSequence?
        get() {
            val action = currentAccessibilityAction
            return if (action is AutoParseAction) {
                action.actionBean?.summary
            } else {
                null
            }
        }

    override fun onServiceConnected(service: BaseAccessibilityService) {
        super.onServiceConnected(service)
        onWindowLog?.invoke("已准备", null, 0)
    }

    override fun onIntervalStart(delay: Long) {
        super.onIntervalStart(delay)
        updateWindow()
    }

    override fun onDoActionFinish(action: BaseAccessibilityAction?, error: ActionException?) {
        updateWindow()
        super.onDoActionFinish(action, error)
    }

    override fun onDestroy(reason: String?) {
        if (actionStatus.isActionStart()) {
            onWindowLog?.invoke("中止", null, 0)
        }
        super.onDestroy(reason)
    }

    fun updateWindow() {
        when (actionStatus) {
            ACTION_STATUS_INIT -> {
                if (actionIndex < 0) {
                    onWindowLog?.invoke("就绪", null, intervalDelay)
                } else {
                    onWindowLog?.invoke(
                        "$actionIndex/${actionList.size}",
                        currentActionSummary,
                        intervalDelay
                    )
                }
            }
            ACTION_STATUS_ING -> {
                if (actionIndex != -1) {
                    onWindowLog?.invoke(
                        "$actionIndex/${actionList.size}",
                        currentActionSummary,
                        intervalDelay
                    )
                } else {
                    onWindowLog?.invoke("等待", null, intervalDelay)
                }
            }
            ACTION_STATUS_FINISH -> onWindowLog?.invoke("已完成", null, 0)
            ACTION_STATUS_ERROR -> onWindowLog?.invoke("异常", null, 0)
            ACTION_STATUS_DESTROY -> onWindowLog?.invoke("结束", null, 0)
        }
    }
}