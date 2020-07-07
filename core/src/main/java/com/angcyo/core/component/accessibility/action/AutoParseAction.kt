package com.angcyo.core.component.accessibility.action

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ParseParams
import com.angcyo.core.component.accessibility.parse.isEmpty
import com.angcyo.library.ex.isListEmpty

/**
 * 智能识别的[Action], 通过配置一些关键字, 快速创建对应的[Action]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParseAction : BaseAccessibilityAction() {

    /**是否需要当前事件[checkEvent]解析时的关键数据*/
    var eventParams: ParseParams? = null

    /**目中目标界面后[doAction]解析时的关键数据*/
    var clickParams: ParseParams? = null

    /**当[Action]被作为回退处理时[doActionWidth]解析时的关键数据*/
    var backParams: ParseParams? = null

    /**日志输出*/
    var onLog: ((CharSequence) -> Unit)? = null

    override fun checkEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        val params = eventParams
        if (params.isEmpty()) {
            doActionFinish(ActionException("eventParams is null."))
        }
        return params?.run {
            service.parse(this)
        } ?: super.checkEvent(service, event)
    }

    override fun doAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        super.doAction(service, event)
        val params = clickParams
        if (params.isEmpty()) {
            doActionFinish(ActionException("clickParams is null."))
        } else {
            service.parse(params!!) {
                it.clickAll {
                    log(buildString {
                        append("点击[")
                        it.logText(this)
                        append("]")
                    })
                }
            }.also {
                if (it) {
                    //完成
                    doActionFinish()
                } else {
                    //未完成
                }
            }
        }
    }

    override fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        val params = backParams
        if (params.isEmpty()) {
            var result = false
            if (eventParams == null) {
                //无界面约束匹配, 则不检查. 直接处理
            } else {
                //匹配当前界面, 匹配成功后, 再处理
                if (service.parse(eventParams!!)) {
                    //匹配成功
                    service.parse(params!!) {
                        result = it.clickAll {
                            log(buildString {
                                append("关闭页面, 点击[")
                                it.logText(this)
                                append("]")
                            })
                        }
                    }
                }
            }
            return result
        }
        return super.doActionWidth(action, service, event)
    }

    open fun log(charSequence: CharSequence) {
        onLog?.invoke(charSequence)
    }
}

/**返回当前界面, 是否包好[params]指定的标识信息
 * [onTargetResult] 当找到目标时, 通过此方法回调目标给调用者*/
fun BaseAccessibilityService.parse(
    params: ParseParams,
    onTargetResult: (List<AccessibilityNodeInfoCompat>) -> Unit = {}
): Boolean {
    val rootNodeInfo: AccessibilityNodeInfo = rootNodeInfo() ?: return false

    val packageName: String = packageName
    val targetList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

    val tempList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

    var haveNode = false

    //优先判断 id
    params.ids?.forEach { paramConstraint ->
        try {
            haveNode = false
            tempList.clear()
            if (!paramConstraint.text.isListEmpty()) {
                for (i: Int in paramConstraint.text!!.indices) {
                    val subId: String = packageName.id(paramConstraint.text!![i]) //完整id 是需要包含包名的
                    val result: List<AccessibilityNodeInfoCompat> = rootNodeInfo.findNode {
                        val idName = it.viewIdName()
                        if (subId == idName) {
                            val cls = paramConstraint.cls?.getOrNull(i)

                            if (cls != null && !cls.contains(it.className)) {
                                //id相同, 但是类名不同
                                -1
                            } else {
                                //命中
                                1
                            }
                        } else {
                            -1
                        }
                    }

                    if (result.isEmpty()) {
                        haveNode = false
                        break
                    } else {
                        tempList.addAll(result)
                        haveNode = true
                    }
                }
                if (haveNode) {
                    targetList.addAll(tempList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (targetList.isNotEmpty()) {
        //通过id, 已经找到了目标

        onTargetResult(targetList)
        return true
    }

    //其次判断 text
    params.texts?.forEach { paramConstraint ->
        try {
            haveNode = false
            tempList.clear()

            if (!paramConstraint.text.isListEmpty()) {

                for (i: Int in paramConstraint.text!!.indices) {
                    val subText: String = paramConstraint.text!![i]
                    val result: List<AccessibilityNodeInfoCompat> = rootNodeInfo.findNode {
                        if (it.haveText(subText)) {
                            val cls = paramConstraint.cls?.getOrNull(i)

                            if (cls != null && !cls.contains(it.className)) {
                                //包含文本相同, 但是类名不同
                                -1
                            } else {
                                //命中
                                1
                            }
                        } else {
                            -1
                        }
                    }

                    if (result.isEmpty()) {
                        haveNode = false
                        break
                    } else {
                        tempList.addAll(result)
                        haveNode = true
                    }
                }
                if (haveNode) {
                    targetList.addAll(tempList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (targetList.isNotEmpty()) {
        //通过id, 已经找到了目标

        onTargetResult(targetList)
        return true
    }

    return false
}

/**点击一组节点中的所有可点击的节点, 并返回是否点击成功.
 * [onClickResult] 回调 执行点击的[AccessibilityNodeInfoCompat]和文本提示的[CharSequence]
 * */
fun List<AccessibilityNodeInfoCompat>.clickAll(onClickResult: (List<Pair<AccessibilityNodeInfoCompat, CharSequence?>>) -> Unit = {}): Boolean {
    var result = false
    val list = mutableListOf<Pair<AccessibilityNodeInfoCompat, CharSequence?>>()
    forEach {
        it.getClickParent()?.let { click ->
            if (click.click()) {
                result = true
                list.add(click to (it.text() ?: click.text()))
            }
        }
    }
    if (result) {
        onClickResult(list)
    }
    return result
}

fun List<Pair<AccessibilityNodeInfoCompat, CharSequence?>>.logText(builder: StringBuilder = StringBuilder()): String {
    forEach { pair ->
        if (!pair.second.isNullOrEmpty()) {
            builder.append(pair.second)
            builder.append(" ")
        }
    }
    return builder.toString()
}

