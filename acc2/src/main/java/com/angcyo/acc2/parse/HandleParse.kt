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
        registerActionList.add(EnableAction())
        registerActionList.add(DisableAction())
        registerActionList.add(ToastAction())

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

        registerActionList.add(InputAction())
        registerActionList.add(GetTextAction())
        registerActionList.add(FullscreenAction())
        registerActionList.add(NotTouchableAction())
        registerActionList.add(HideWindowAction())

        registerActionList.add(AppendTextAction())
        registerActionList.add(ClearTextAction())
        registerActionList.add(PutTextAction())

        registerActionList.add(JumpAction())
        registerActionList.add(ClearRunCountAction())
        registerActionList.add(ClearJumpCountAction())
        registerActionList.add(ClearRunTimeAction())
        registerActionList.add(FinishAction())
        registerActionList.add(ErrorAction())
        registerActionList.add(DisableHandleAction())
        registerActionList.add(StopAction())
        registerActionList.add(PauseAction())
        registerActionList.add(ResumeAction())
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
                result.forceSuccess = handleResult.forceSuccess || result.forceSuccess

                if (handleResult.success) {
                    if (!handleResult.forceFail) {
                        result = handleResult
                        handleResult.nodeList?.forEach {
                            if (!handleNodeList.contains(it)) {
                                handleNodeList.add(it)
                            }
                        }
                        result.nodeList = handleNodeList
                    }
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

        //---------------条件处理----------------

        if (!handleBean.enable) {
            return result
        }

        //condition
        var conditionActionList: List<String>? = null

        if (handleBean.conditionList != null) {
            //有约束条件需要满足
            if (!accParse.conditionParse.parse(handleBean.conditionList).success) {
                //未满足条件
                if (handleBean.conditionActionList == null) {
                    return result
                }
                conditionActionList = handleBean.conditionActionList
            }
        }

        //待处理的元素节点集合
        var handleNodeList = if (handleBean.findList != null) {
            //需要明确重新指定
            val findResult = accParse.findParse.parse(
                accParse.findParse.rootWindowNode(),
                handleBean.findList
            )
            findResult.nodeList
        } else {
            originList
        }

        //过滤
        if (handleBean.filter != null) {
            handleNodeList = handleNodeList?.toMutableList()?.apply {
                removeAll(accParse.filterParse.parse(handleNodeList, handleBean.filter))
            }
        }

        //index筛选
        if (handleBean.index != null && !handleNodeList.isNullOrEmpty()) {
            val filterFindNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
            val index = accParse.parseText(handleBean.index).firstOrNull()
            filterFindNodeList.addAll(handleNodeList.eachRangeItem(index))
            handleNodeList = filterFindNodeList
        }

        //---------------开始处理----------------

        if (conditionActionList != null) {
            //不满足约束条件时,又指定了对应的actionList, 优先执行
            result = handleAction(handleBean, handleNodeList, conditionActionList)
        } else if (handleBean.findList != null) {
            //需要重新选择
            if (handleNodeList.isNullOrEmpty()) {
                //重新选择后, 节点为空
                if (handleBean.noActionList != null) {
                    result = handleAction(handleBean, handleNodeList, handleBean.noActionList)
                } else {
                    //失败
                    result.success = false
                }
            } else {
                //重新选择后, 节点不为空
                result = handleAction(handleBean, handleNodeList, handleBean.actionList)
            }
        } else {
            //默认处理
            if (handleBean.actionList.isNullOrEmpty()) {
                result.success = handleBean.actionList != null
            } else {
                result = handleAction(handleBean, handleNodeList, handleBean.actionList)
            }
        }

        if (result.forceFail) {
            accParse.accControl.log("强制失败处理:${handleBean}")
            if (handleBean.failActionList != null) {
                result = handleAction(handleBean, handleNodeList, handleBean.failActionList)
                result.forceFail = true
            }
        } else if (result.success) {
            //如果处理成功
            if (handleBean.successActionList != null) {
                result = handleAction(handleBean, handleNodeList, handleBean.successActionList)
            }
        } else {
            if (handleBean.failActionList != null) {
                result = handleAction(handleBean, handleNodeList, handleBean.failActionList)
            }
        }

        //
        if (result.success) {
            result.handleBean = result.handleBean ?: handleBean
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
        handleBean: HandleBean,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        actionList: List<String>?
    ): HandleResult {
        val result = HandleResult()
        val handledNodeList = mutableListOf<AccessibilityNodeInfoCompat>()

        //枚举actionList
        actionList?.forEach { action ->
            //处理action
            handleAction(handleBean, nodeList, action).apply {
                result.forceFail = forceFail || result.forceFail
                result.forceSuccess = forceSuccess || result.forceSuccess
                result.success = success || result.success
                if (success) {
                    //把处理成功的元素收集起来
                    result.handleBean = this.handleBean
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
    fun handleAction(
        handleBean: HandleBean,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult {
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
                    result.forceFail = forceFail || result.forceFail
                    result.forceSuccess = forceSuccess || result.forceSuccess
                    result.success = success || result.success
                    if (success) {

                        //this
                        if (it is DisableHandleAction) {
                            handleBean.enable = false
                        }

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