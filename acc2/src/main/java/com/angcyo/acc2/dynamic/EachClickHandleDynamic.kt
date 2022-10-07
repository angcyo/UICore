package com.angcyo.acc2.dynamic

import androidx.annotation.Keep
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.action.handleResult
import com.angcyo.acc2.bean.FindBean
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.sleep

/**
 * 依次点击指定的文本按钮, 用于密码输入
 * 文本参数放在[handleClsParams]的[eachClickText]中, 多个文本用[Action.ARG_SPLIT_SPACE]空格隔开
 *
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/10/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

@Keep
open class EachClickHandleDynamic : IHandleDynamic {

    override fun handle(
        control: AccControl,
        controlContext: ControlContext,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleBean: HandleBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>?
    ): HandleResult = handleResult {
        val eachClickText = handleBean.handleClsParams?.get("eachClickText")?.toString()

        val accParse = control.accSchedule.accParse
        val text = accParse.textParse.parse(eachClickText)
        if (text.isNotEmpty()) {
            //需要点击的文本不为空
            var haveFail = false
            text.forEach {
                it?.split(Action.ARG_SPLIT_SPACE)?.forEach { nodeText ->
                    //开始查找文本对应的节点, 并且点击

                    val findBean = handleBean.findList?.firstOrNull() ?: FindBean()
                    findBean.textList = listOf("^${nodeText}$")

                    //rootNode
                    val rootNodeList = if (handleBean.rootNode == Action.RESULT) {
                        originList
                    } else {
                        val actionBean = controlContext.action
                        val windowBean = actionBean?.check?.window ?: actionBean?.window
                        accParse.findParse.rootWindowNode(windowBean)
                    }

                    val findResult =
                        accParse.findParse.parse(controlContext, rootNodeList, listOf(findBean))
                    val findHandleNodeList = findResult.nodeList
                    val targetActionList = listOf(Action.ACTION_CLICK)

                    //开始点击处理
                    val handleResult = accParse.handleParse.handleAction(
                        controlContext,
                        handleBean,
                        null,
                        findHandleNodeList,
                        targetActionList
                    )

                    if (!handleResult.success) {
                        haveFail = true
                    }

                    //wait
                    val wait = handleBean.wait
                    if (!wait.isNullOrEmpty()) {
                        sleep(accParse.parseTime(wait))
                    }
                }
            }
            success = !haveFail
        }
    }

}