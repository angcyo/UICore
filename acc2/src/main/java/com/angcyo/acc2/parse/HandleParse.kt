package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.*
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.bean.TextParamBean
import com.angcyo.acc2.control.*
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.size

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class HandleParse(val accParse: AccParse) : BaseParse() {

    /**可执行的动作列表*/
    val registerActionList = mutableListOf<BaseAction>()

    init {

        registerActionList.add(StartAction())
        registerActionList.add(UrlAction())
        registerActionList.add(SleepAction())
        registerActionList.add(CopyAction())
        registerActionList.add(TrueAction())
        registerActionList.add(FalseAction())
        registerActionList.add(RandomFalseAction())
        registerActionList.add(EnableAction())
        registerActionList.add(DisableAction())
        registerActionList.add(ToastAction())
        registerActionList.add(RequestFormAction())
        registerActionList.add(IntentAction())

        registerActionList.add(BackAction())
        registerActionList.add(HomeAction())
        registerActionList.add(ScreenshotAction())

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
        registerActionList.add(SetTextAction())
        registerActionList.add(GetTextAction())
        registerActionList.add(FullscreenAction())
        registerActionList.add(NotTouchableAction())
        registerActionList.add(HideWindowAction())

        registerActionList.add(AppendTextAction())
        registerActionList.add(ClearTextAction())
        registerActionList.add(PutTextAction())

        registerActionList.add(JumpAction())
        registerActionList.add(PassAction())
        registerActionList.add(ClearRunCountAction())
        registerActionList.add(ClearJumpCountAction())
        registerActionList.add(ClearRunTimeAction())
        registerActionList.add(FinishAction())
        registerActionList.add(ErrorAction())
        registerActionList.add(DisableHandleAction())
        registerActionList.add(StopAction())
        registerActionList.add(PauseAction())
        registerActionList.add(ResumeAction())
        registerActionList.add(InterruptAction())
        registerActionList.add(CountAction())
    }

    /**解析, 并处理[handleList]*/
    fun parse(
        controlContext: ControlContext,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleList: List<HandleBean>?
    ): HandleResult {
        var result = HandleResult()
        if (handleList.isNullOrEmpty()) {
            //no op
        } else {
            val handleNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
            for (handBean in handleList) {
                val handleResult = parse(controlContext, originList, handBean)
                result.forceSuccess = handleResult.forceSuccess || result.forceSuccess
                result.forceFail = handleResult.forceFail || result.forceFail

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
        controlContext: ControlContext,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleBean: HandleBean
    ): HandleResult {
        var result = HandleResult()

        controlContext.handle = handleBean

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

        //rootNode
        val rootNodeList = if (handleBean.rootNode == Action.RESULT) {
            originList
        } else {
            accParse.findParse.rootWindowNode()
        }

        //待处理的元素节点集合
        var handleNodeList = if (handleBean.findList != null) {
            //需要明确重新指定
            val findResult =
                accParse.findParse.parse(controlContext, rootNodeList, handleBean.findList)
            findResult.nodeList
        } else {
            originList
        }

        //过滤
        if (handleBean.filter != null) {
            handleNodeList = handleNodeList?.toMutableList()?.apply {
                removeAll(
                    accParse.filterParse.parse(
                        controlContext,
                        handleNodeList,
                        handleBean.filter
                    )
                )
            }
        }

        //index筛选
        if (handleBean.index != null && !handleNodeList.isNullOrEmpty()) {
            val filterFindNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
            val index = accParse.textParse.parseOrDef(handleBean.index).firstOrNull()
            filterFindNodeList.addAll(handleNodeList.eachRangeItem(index))
            handleNodeList = filterFindNodeList
        }

        //---------------开始处理----------------

        if (handleBean.handleBefore != null) {
            //处理前, 需要的处理
            parse(controlContext, handleNodeList, handleBean.handleBefore)
        }

        //case
        val caseBean = if (handleBean.caseList != null) {
            accParse.caseParse.parse(handleBean.caseList!!, handleNodeList)
        } else {
            null
        }

        //text param
        val textParamBean =
            caseBean?.textParam ?: handleBean.textParam ?: accParse.accControl._taskBean?.textParam

        if (isDebug() && !handleBean.debugActionList.isNullOrEmpty()) {
            //调试专用
            result = handleAction(
                controlContext,
                handleBean,
                textParamBean,
                handleNodeList,
                handleBean.debugActionList
            )
        } else if (conditionActionList != null) {
            //不满足约束条件时,又指定了对应的actionList, 优先执行
            result = handleAction(
                controlContext,
                handleBean,
                textParamBean,
                handleNodeList,
                conditionActionList
            )
        } else {
            val targetActionList: List<String>? = caseBean?.actionList ?: handleBean.actionList
            if (handleBean.findList != null) {
                //需要重新选择
                if (handleNodeList.isNullOrEmpty()) {
                    //重新选择后, 节点为空
                    if (handleBean.noActionList != null) {
                        result = handleAction(
                            controlContext,
                            handleBean,
                            textParamBean,
                            handleNodeList,
                            handleBean.noActionList
                        )
                    } else {
                        //重新选择后, 没有找到元素, 也没有指定[noActionList], 这直接失败
                        result.success = false
                    }
                } else {
                    //重新选择后, 节点不为空
                    result = handleAction(
                        controlContext,
                        handleBean,
                        textParamBean,
                        handleNodeList,
                        targetActionList
                    )
                }
            } else {
                //默认处理
                if (targetActionList.isNullOrEmpty()) {
                    result.success = targetActionList != null
                } else {
                    result = handleAction(
                        controlContext,
                        handleBean,
                        textParamBean,
                        handleNodeList,
                        targetActionList
                    )
                }
            }
        }

        if (result.forceFail) {
            accParse.accControl.log("强制失败处理:${handleBean}")
            if (handleBean.failActionList != null) {
                result = handleAction(
                    controlContext,
                    handleBean,
                    textParamBean,
                    handleNodeList,
                    handleBean.failActionList
                )
                result.forceFail = true
            }
        } else if (result.success) {
            //如果处理成功
            if (handleBean.successActionList != null) {
                result = handleAction(
                    controlContext,
                    handleBean,
                    textParamBean,
                    handleNodeList,
                    handleBean.successActionList
                )
            }
        } else {
            if (handleBean.failActionList != null) {
                result = handleAction(
                    controlContext,
                    handleBean,
                    textParamBean,
                    handleNodeList,
                    handleBean.failActionList
                )
            }
        }

        if (handleBean.handleAfter != null) {
            //处理后, 需要的处理
            parse(controlContext, handleNodeList, handleBean.handleAfter)
        }

        //---------------处理结束----------------

        //
        if (result.success) {
            result.handleBean = result.handleBean ?: handleBean
        }

        //后置处理
        if (handleBean.ignore) {
            controlContext.log {
                append("忽略[handle]结果:${handleBean}")
            }
            result.success = false
        }

        val operate = handleBean.operate
        if (operate != null) {
            accParse.operateParse.parse(handleBean, operate, result)
        }

        //operate form
        accParse.formParse.parseOperateForm(
            controlContext,
            accParse.accControl,
            handleBean,
            originList,
            result
        )

        accParse.accControl.controlListenerList.forEach {
            it.onHandleAction(controlContext, accParse.accControl, handleBean, result)
        }

        //handle form
        accParse.formParse.parseHandleForm(
            controlContext,
            accParse.accControl,
            handleBean,
            originList,
            result
        )

        return result
    }

    /**处理动作集合*/
    fun handleAction(
        controlContext: ControlContext,
        handleBean: HandleBean?,
        textParamBean: TextParamBean?,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        actionList: List<String>?
    ): HandleResult {
        val result = HandleResult()
        val handledNodeList = mutableListOf<AccessibilityNodeInfoCompat>()

        //枚举actionList
        if (actionList != null) {
            for (action in actionList) {
                //处理action
                val handleResult = handleAction(
                    controlContext,
                    handleBean,
                    textParamBean,
                    nodeList,
                    action
                )

                //result
                result.forceFail = handleResult.forceFail || result.forceFail
                result.forceSuccess = handleResult.forceSuccess || result.forceSuccess
                result.success = handleResult.success || result.success

                if (handleResult.success) {
                    //把处理成功的元素收集起来
                    result.handleBean = handleResult.handleBean
                    handleResult.nodeList?.forEach {
                        if (!handledNodeList.contains(it)) {
                            handledNodeList.add(it)
                        }
                    }
                }

                if (handleResult.forceFail || accParse.accControl.isControlEnd) {
                    //强制失败后, 中断后续action执行
                    break
                }
            }
        }

        return result
    }

    /**处理分发*/
    fun handleAction(
        controlContext: ControlContext,
        handleBean: HandleBean?,
        textParamBean: TextParamBean?,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult {
        val result = HandleResult()

        val handledNodeList = mutableListOf<AccessibilityNodeInfoCompat>()

        var isActionIntercept = false

        val accControl = accParse.accControl
        if (!nodeList.isNullOrEmpty()) {
            accControl.log(nodeList.toLog("处理节点[${nodeList.size()}][$action] ${accControl.accSchedule._runActionBean?.actionLog()}↓"))
            accControl.accPrint.handleNode(nodeList)
        }

        registerActionList.forEach {
            //是否要处理指定的action
            if (it.interceptAction(accControl, action)) {
                isActionIntercept = true
                it.textParamBean = textParamBean
                //运行处理
                it.runAction(accControl, controlContext, nodeList, action).apply {
                    result.forceFail = forceFail || result.forceFail
                    result.forceSuccess = forceSuccess || result.forceSuccess
                    result.success = success || result.success
                    if (success) {

                        //this
                        if (it is DisableHandleAction) {
                            handleBean?.enable = false
                        }

                        //把处理成功的元素收集起来
                        this.nodeList?.forEach {
                            if (!handledNodeList.contains(it)) {
                                handledNodeList.add(it)
                            }
                        }
                    }
                }
                it.textParamBean = null
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

    /**[InputAction]整个集合列表输入结束之后触发*/
    fun onTextInputEnd(
        handleBean: HandleBean,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ) {
        if (handleBean.handleActionEndActionList != null) {
            handleAction(
                controlContext,
                handleBean,
                null,
                nodeList,
                handleBean.handleActionEndActionList
            )
        }
    }

    override fun onScheduleStart(scheduled: AccSchedule) {
        super.onScheduleStart(scheduled)
        registerActionList.forEach {
            it.onScheduleStart(scheduled)
        }
    }

    override fun onScheduleEnd(scheduled: AccSchedule) {
        super.onScheduleEnd(scheduled)
        registerActionList.forEach {
            it.onScheduleEnd(scheduled)
        }
    }

}