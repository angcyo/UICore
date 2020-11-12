package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityAction
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.currentAccessibilityAction
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.toUnwrapList
import kotlin.math.absoluteValue

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class JumpAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_JUMP
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        val arg = arg

        //执行跳转指令
        var value = false
        val interceptor = autoParseAction.accessibilityInterceptor

        //跳转次数
        val defaultJumpCount = if ((autoParseAction.actionBean?.actionMaxCount ?: -1) > 0) {
            autoParseAction.actionBean!!.actionMaxCount
        } else {
            BaseAccessibilityAction.DEFAULT_JUMP_MAX_COUNT
        }

        if (arg.isNullOrEmpty() || interceptor == null) {
            //没有跳转参数,直接完成action
            value = true
            handleResult.finish = true
        } else {
            //[:1:3] [:-1] [:<2]前 [:>3]后 [:actionId;actionId;:4]
            val indexOf = arg.indexOf(":", 0, true)
            val arg1: String?
            val arg2: String?

            val actionIdList = mutableListOf<Long>()
            if (indexOf == -1) {
                //未找到
                arg1 = arg
                arg2 = "$defaultJumpCount"
            } else {
                //找到
                arg1 = arg.substring(0, indexOf)
                arg2 = arg.substring(indexOf + 1, arg.length)
            }

            arg1.split(";").forEach {
                it.toLongOrNull()
                    ?.let { actionId -> actionIdList.add(actionId) }
            }

            val maxCount = arg2.toLongOrNull() ?: defaultJumpCount

            autoParseAction.jumpCount.maxCountLimit = maxCount
            autoParseAction.jumpCount.start()

            if (autoParseAction.jumpCount.isMaxLimit()) {
                //超限后, 不跳转, 直接完成
                autoParseAction.jumpCount.clear()

                val jumpOut = autoParseAction.actionBean?.check?.jumpOut
                if (jumpOut == null) {
                    //未指定跳转超限的处理, 则直接完成
                    value = true
                    handleResult.finish = true
                } else {
                    value = autoParseAction.parseHandleAction(
                        service,
                        autoParseAction.currentAccessibilityAction(),
                        handleNodeList.toUnwrapList(),
                        autoParseAction.actionBean?.check?.jumpOut
                    )
                }

            } else {
                value = true

                if (actionIdList.isEmpty()) {
                    // [:<2]前2个 [:>3]后3个
                    val num = arg1.substring(1, arg1.length).toIntOrNull() ?: 0
                    if (arg1.startsWith("<")) {
                        interceptor._targetAction =
                            interceptor.actionList.getOrNull(interceptor.actionIndex - num)
                    } else if (arg1.startsWith(">")) {
                        interceptor._targetAction =
                            interceptor.actionList.getOrNull(interceptor.actionIndex + num)
                    } else {
                        value = false
                        handleResult.finish = true
                    }
                } else {
                    var findAction = false
                    for (i in 0 until actionIdList.size) {
                        if (findAction) {
                            break
                        }
                        val targetIndex = actionIdList[i]

                        val size = interceptor.actionList.size
                        if (targetIndex.absoluteValue in 0 until size) {
                            //处理[:1] [:-1]的情况, 第多少个
                            if (targetIndex > 0) {
                                interceptor._targetAction =
                                    interceptor.actionList.getOrNull(targetIndex.toInt())
                            } else {
                                interceptor._targetAction =
                                    interceptor.actionList.getOrNull((size + targetIndex).toInt())
                            }
                            findAction = true
                        } else {
                            //寻找指定[actionId;actionId;]
                            interceptor.actionList.forEachIndexed { _, baseAccessibilityAction ->
                                if (baseAccessibilityAction is AutoParseAction) {
                                    if (baseAccessibilityAction.actionBean?.actionId == targetIndex) {
                                        interceptor._targetAction =
                                            baseAccessibilityAction
                                        findAction = true
                                    }
                                }
                            }
                        }
                    }
                }

                autoParseAction.jumpCount.doCount()
            }
        }

        autoParseAction.handleActionLog("跳转[$arg]:$value")
        return value
    }
}