package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.*
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.control.log
import com.angcyo.library.ex.size

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class HandleParse(val accParse: AccParse) {

    val registerActionList = mutableListOf<BaseAction>()

    init {

        registerActionList.add(StartAction())
        registerActionList.add(SleepAction())
        registerActionList.add(CopyAction())
        registerActionList.add(TrueAction())
        registerActionList.add(FalseAction())

        registerActionList.add(BackAction())
        registerActionList.add(HomeAction())

        registerActionList.add(ClickAction())
        registerActionList.add(ScrollBackwardAction())
        registerActionList.add(ScrollForwardAction())
        registerActionList.add(FocusAction())

        registerActionList.add(ClickTouchAction())
        registerActionList.add(LongClickAction())
        registerActionList.add(TouchAction())
        registerActionList.add(DoubleAction())
        registerActionList.add(MoveAction())
        registerActionList.add(FlingAction())

        registerActionList.add(AppendTextAction())
        registerActionList.add(GetTextAction())
        registerActionList.add(ClearTextAction())

        registerActionList.add(FinishAction())
        registerActionList.add(ErrorAction())
        registerActionList.add(JumpAction())
    }

    /**解析, 并处理[handleList]*/
    fun parse(
        originList: List<AccessibilityNodeInfoCompat>?,
        handleList: List<HandleBean>?
    ): HandleResult {
        var result = HandleResult()
        if (handleList.isNullOrEmpty()) {
            //no op
        } else {
            val handleNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
            for (handBean in handleList) {
                val handleResult = parse(originList, handBean)
                if (handleResult.success) {
                    result = handleResult
                    handleResult.nodeList?.forEach {
                        if (!handleNodeList.contains(it)) {
                            handleNodeList.add(it)
                        }
                    }
                    result.nodeList = handleNodeList
                }
                if (handBean.jump) {
                    //跳过后续处理
                    break
                }
                if (handBean.jumpOnSuccess && handleResult.success) {
                    //成功之后跳过后续处理
                    break
                }
            }
        }
        return result
    }

    /**单个处理*/
    fun parse(
        originList: List<AccessibilityNodeInfoCompat>?,
        handleBean: HandleBean
    ): HandleResult {
        var result = HandleResult()

        //待处理的元素节点集合
        var handleNodeList = if (handleBean.findList != null) {
            //需要明确重新指定
            accParse.findParse.parse(
                accParse.findParse.rootWindowNode(),
                handleBean.findList
            ).nodeList
        } else {
            originList
        }

        //过滤
        if (handleBean.filter != null) {
            handleNodeList = handleNodeList?.toMutableList()?.apply {
                removeAll(accParse.filterParse.parse(handleNodeList, handleBean.filter))
            }
        }

        //待处理的事件列表
        val handleActionList = if (handleNodeList.isNullOrEmpty()) {
            //无元素需要处理时, 优先使用[noActionList], 否则[actionList]
            handleBean.noActionList ?: handleBean.actionList
        } else {
            handleBean.actionList
        }

        //---------------开始处理----------------

        if (handleActionList.isNullOrEmpty()) {
            result.success = handleActionList != null
        } else {
            result = handleAction(handleNodeList, handleActionList)
        }

        //如果处理成功,
        if (result.success) {
            if (handleBean.successActionList != null) {
                result = handleAction(handleNodeList, handleBean.successActionList)
            }
        } else {
            if (handleBean.failActionList != null) {
                result = handleAction(handleNodeList, handleBean.failActionList)
            }
        }

        //后置处理
        if (handleBean.ignore) {
            accParse.accControl.log("忽略[handle]结果:${handleBean}")
            result.success = false
        }

        return result
    }

    /**处理动作集合*/
    fun handleAction(
        nodeList: List<AccessibilityNodeInfoCompat>?,
        actionList: List<String>?
    ): HandleResult {
        val result = HandleResult()
        val handledNodeList = mutableListOf<AccessibilityNodeInfoCompat>()

        //枚举actionList
        actionList?.forEach { action ->
            //处理action
            handleAction(nodeList, action).apply {
                result.success = success || result.success
                if (success) {
                    //把处理成功的元素收集起来
                    result.handleBean = handleBean
                    this.nodeList?.forEach {
                        if (!handledNodeList.contains(it)) {
                            handledNodeList.add(it)
                        }
                    }
                }
            }
        }

        return result
    }

    /**处理分发*/
    fun handleAction(nodeList: List<AccessibilityNodeInfoCompat>?, action: String): HandleResult {
        val result = HandleResult()

        val handledNodeList = mutableListOf<AccessibilityNodeInfoCompat>()

        var isActionIntercept = false

        val accControl = accParse.accControl
        if (!nodeList.isNullOrEmpty()) {
            accControl.log(nodeList.toLog("处理节点[${nodeList.size()}][$action]↓"))
            accControl.accPrint.handleNode(nodeList)
        }

        registerActionList.forEach {
            //是否要处理指定的action
            if (it.interceptAction(accControl, action)) {
                isActionIntercept = true
                //运行处理
                it.runAction(accControl, nodeList, action).apply {
                    result.success = success || result.success
                    if (success) {
                        //把处理成功的元素收集起来
                        this.nodeList?.forEach {
                            if (!handledNodeList.contains(it)) {
                                handledNodeList.add(it)
                            }
                        }
                    }
                }
            }
        }

        if (!isActionIntercept) {
            //动作未识别, 或者无法处理
            result.success = true
            accControl.log("无法处理[$action],跳过.")
        }
        if (result.success) {
            result.nodeList = handledNodeList
        }

        return result
    }

}