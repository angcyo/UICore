package com.angcyo.acc2.dynamic

import androidx.annotation.Keep
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.handleResult
import com.angcyo.acc2.action.toNodeTextList
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.bean.putListMap
import com.angcyo.acc2.bean.putMap
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.decode

/**
 * 采集信息
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

@Keep
open class InfoCollectHandleDynamic : IHandleDynamic {

    override fun handle(
        control: AccControl,
        controlContext: ControlContext,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleBean: HandleBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>?
    ): HandleResult {
        val result = handleResult {
            //op
        }
        val accParse = control.accSchedule.accParse
        handleBean.findList?.forEach { findBean ->
            val findResult = accParse.findParse.parse(controlContext, handleNodeList, findBean)
            if (findResult.success) {
                //节点提示
                control.accPrint.findNode(findResult.nodeList)
                control.accPrint.handleNode(findResult.nodeList)

                val regex = findBean.regex?.run {
                    val decode = " decode:true"
                    if (this.endsWith(decode)) {
                        dropLast(decode.length).decode()
                    } else {
                        this
                    }
                }

                //收集节点文本
                val textList = findResult.nodeList.toNodeTextList(regex)

                //保存起来
                if (textList.isNotEmpty()) {
                    if (findBean.putArray == true) {
                        control._taskBean?.putListMap(findBean.key, textList, true)
                    } else {
                        val lastText = textList.lastOrNull()
                        control._taskBean?.putMap(findBean.key ?: lastText, lastText)
                    }
                    //获取成功
                    result.success = true

                }
            }
        }
        return result
    }
}