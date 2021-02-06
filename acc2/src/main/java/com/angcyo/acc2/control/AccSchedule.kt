package com.angcyo.acc2.control

import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.parse.AccParse
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.L
import com.angcyo.library.ex.*
import java.util.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AccSchedule(val accControl: AccControl) {

    var _startTime: Long = 0
    var _endTime: Long = 0

    /**运行次数统计, 从1开始计数*/
    var actionCount = hashMapOf<Long, ActionCount>()

    //<editor-fold desc="操作">

    /**获取总共运行时长*/
    fun duration(): Long = _endTime - _startTime

    fun durationStr() = duration().toElapsedTime(pattern = intArrayOf(-1, 1, 1))

    /**累加运行次数*/
    fun runCountIncrement(actionId: Long) {
        val count = actionCount[actionId] ?: ActionCount()
        count.runCount.doCount()
        actionCount[actionId] = count
    }

    fun clearRunCount(actionId: Long) {
        actionCount[actionId]?.runCount?.clear()
    }

    fun clearJumpCount(actionId: Long) {
        actionCount[actionId]?.jumpCount?.clear()
    }

    /**累加跳转次数*/
    fun jumpCountIncrement(actionId: Long?) {
        if (actionId != null) {
            val count = actionCount[actionId] ?: ActionCount()
            count.jumpCount.doCount()
            actionCount[actionId] = count
        }
    }

    /**获取[ActionBean]的运行次数*/
    fun getRunCount(actionId: Long): Long = actionCount[actionId]?.runCount?.count ?: -1

    fun getJumpCount(actionId: Long): Long = actionCount[actionId]?.jumpCount?.count ?: -1

    /**预备下一个需要执行*/
    fun next() {
        _scheduleIndex = _currentIndex + 1
    }

    fun thread(target: Runnable) {
        Thread(target, this.simpleHash()).apply {
            start()
        }
    }

    fun indexTip() = "$_currentIndex/${actionSize()}"

    /**总共需要执行的[ActionBean]的数量*/
    fun actionSize() = accControl._taskBean?.actionList.size()

    fun relyList(): List<Long>? {
        return _runActionBean?.relyList ?: _scheduleActionBean?.relyList
    }

    //</editor-fold desc="操作">

    //<editor-fold desc="调度">

    /**开始调度*/
    fun startSchedule() {
        _startTime = nowTime()
        actionCount.clear()
        _endTime = 0
        _currentIndex = -1
        _scheduleActionBean = null
        _scheduleIndex = -1
    }

    /**结束调度*/
    fun endSchedule() {
        _endTime = nowTime()
    }

    /**额外需要执行的[ActionBean]*/
    val targetActionList = mutableListOf<ActionBean>()

    //当前调度的索引
    var _currentIndex = -1

    //当前调度的[ActionBean], 主线[ActionBean]
    var _scheduleActionBean: ActionBean? = null

    //正在调度的索引, 理论上[_currentIndex]=[_targetIndex]
    var _scheduleIndex = -1

    //强制指定下一个需要执行的索引
    var _targetIndex: Int? = null

    /**循环调度*/
    fun scheduleNext() {
        //调度下一个之前
        _scheduleActionBean?.let {
            targetActionList.remove(it)
        }
        _scheduleActionBean = null

        //获取下一个需要调度的[ActionBean]
        val targetIndex = _targetIndex
        if (targetIndex != null) {
            _currentIndex = targetIndex
            _scheduleIndex = targetIndex
            _targetIndex = null
        }

        //next
        val nextActionBean = nextActionBean()

        if (nextActionBean == null) {
            //无[ActionBean]需要调度, 调度结束
            accControl.finish("执行完成")
        } else {
            _scheduleActionBean = nextActionBean
            _currentIndex = _scheduleIndex

            val result =
                scheduleAction(nextActionBean, accControl._taskBean?.backActionList, true)
            if (result.success) {
                next()
            }
        }
    }

    /**开始调度[actionBean]*/
    fun scheduleAction(
        actionBean: ActionBean,
        otherActionList: List<ActionBean>?,
        isPrimaryAction: Boolean
    ): HandleResult {
        var result = HandleResult()

        //激活条件判断
        if (!accParse.conditionParse.parse(actionBean.conditionList).success) {
            result.success = false
            accControl.log("${actionBean.actionLog()}未满足激活条件,跳过调度.")
            if (isPrimaryAction) {
                next()
            }
            return result
        }

        try {

            val taskBean = accControl._taskBean

            //Task前置处理
            var beforeHandleResult: HandleResult? = null
            if (isPrimaryAction) {
                val beforeAction = taskBean?.before
                if (beforeAction != null) {
                    accControl.log("任务前置执行:${beforeAction}")
                    beforeHandleResult = runAction(beforeAction, null, false)
                }
            }

            //处理
            if (beforeHandleResult?.success != true) {

                //action前置处理
                val beforeAction = actionBean.before
                if (beforeAction != null) {
                    accControl.log("前置执行:${beforeAction}")
                    beforeHandleResult = runAction(beforeAction, null, false)
                }

                //action处理
                if (beforeHandleResult?.success != true) {
                    accControl.log("${if (isPrimaryAction) "[主线]" else ""}开始执行[${actionBean.actionLog()}](${indexTip()}):${actionBean}")
                    if (isPrimaryAction) {
                        //执行统计
                        runCountIncrement(actionBean.actionId)
                    }

                    //run
                    result = runAction(actionBean, otherActionList, isPrimaryAction)

                    if (result.success) {
                        //只有成功才打印日志, 否则日志太多
                        accControl.log("${actionBean.actionLog()}执行结果:${result.success}")
                    }

                    //action后置处理
                    val afterAction = actionBean.after
                    if (afterAction != null) {
                        accControl.log("后置执行:${afterAction}")
                        runAction(afterAction, null, false)
                    }
                }

            }

            //Task后置处理
            if (isPrimaryAction) {
                val afterAction = taskBean?.after
                if (afterAction != null) {
                    accControl.log("任务后置执行:${afterAction}")
                    runAction(afterAction, null, false)
                }
            }

        } catch (e: Exception) {
            L.e("异常:$e")
            e.printStackTrace()
            accControl.log("运行失败[${actionBean.title}]:$e")
        }

        return result
    }

    /**下一个需要执行的[ActionBean]*/
    fun nextActionBean(): ActionBean? {
        var result: ActionBean? = null

        if (targetActionList.isNotEmpty()) {
            //优先执行插队[ActionBean]
            result = targetActionList.first()
        } else {
            if (_scheduleIndex < 0) {
                _scheduleIndex = 0
            }

            //获取可以执行的目标
            accControl._taskBean?.actionList?.apply {
                for (i in _scheduleIndex..lastIndex) {
                    val bean = getOrNull(i)
                    if (bean != null) {
                        if (bean.enable) {
                            //找到激活的
                            if (!accParse.conditionParse.parse(bean.conditionList).success) {
                                accControl.log("${bean.actionLog()}未满足激活条件,跳过调度.")
                                continue
                            }
                            _scheduleIndex = i
                            result = bean
                            break
                        } else {
                            if (bean.autoEnable) {
                                //开启了自动激活
                                if (accParse.conditionParse.parse(bean.conditionList).success) {
                                    bean.enable = true
                                    _scheduleIndex = i
                                    result = bean
                                    break
                                } else {
                                    accControl.log("${bean.actionLog()}自动激活失败,跳过调度.")
                                }
                            } else {
                                accControl.log("${bean.actionLog()}未激活,跳过调度.")
                            }
                        }
                    }
                }
            }
        }

        return result
    }

    /**指定下一个需要调度的[ActionBean]*/
    fun nextScheduleAction(bean: ActionBean) {
        val targetIndex = accControl._taskBean?.actionList?.indexOf(bean)
        targetIndex?.let {
            if (it != -1) {
                accControl.log("强制执行目标[$it/${actionSize()}]:$bean")
                _targetIndex = it
            }
        }
    }

    fun clearTargetAction() {
        targetActionList.clear()
    }

    fun addTargetAction(bean: ActionBean) {
        if (!targetActionList.contains(bean)) {
            targetActionList.add(bean)
        }
    }

    /**设置下一个需要执行的[ActionBean]*/
    fun startTargetAction(bean: ActionBean) {
        clearTargetAction()
        addTargetAction(bean)
        accControl.resume(false)
    }

    //</editor-fold desc="调度">

    //<editor-fold desc="执行">

    /**解析器*/
    var accParse = AccParse(accControl)

    /**保存[ActionBean]执行的结果.
     * key = id
     * value = 成功or失败*/
    val actionResultMap = hashMapOf<Long, Boolean>()

    val runActionBeanStack = Stack<ActionBean>()

    //正在运行的[ActionBean], 有可能是主线[ActionBean]
    var _runActionBean: ActionBean? = null

    /**开始执行[actionBean]
     * [actionBean] 需要执行的动作
     * [otherActionList] 当[actionBean]无法处理时, 需要执行的动作
     * [isPrimaryAction] 是否是主线动作
     * */
    fun runAction(
        actionBean: ActionBean,
        otherActionList: List<ActionBean>?,
        isPrimaryAction: Boolean
    ): HandleResult {
        runActionBeanStack.push(actionBean)
        _runActionBean = actionBean

        var handleActionResult = HandleResult()

        if (accControl.accService() == null) {
            runActionBeanStack.popSafe()
            _runActionBean = null
            accControl.error("无障碍服务连接中断")
            return handleActionResult
        }

        //激活条件判断
        if (!accParse.conditionParse.parse(actionBean.conditionList).success) {
            handleActionResult.success = false
            runActionBeanStack.popSafe()
            _runActionBean = null
            accControl.log("${actionBean.actionLog()}未满足激活条件,跳过执行.")
            return handleActionResult
        }

        //等待执行
        val _start =
            if (isDebugType()) actionBean.debugStart ?: actionBean.start else actionBean.start
        val delayTime = accParse.parseTime(_start)
        if (isPrimaryAction) {
            accControl.next(actionBean.summary ?: actionBean.title, actionBean.des, delayTime)
        }
        accControl.log("${actionBean.actionLog()}等待[$delayTime]ms后运行.")
        sleep(delayTime)

        //回调
        accControl.controlListenerList.forEach {
            it.onActionRunBefore(actionBean, isPrimaryAction)
        }

        val handleParse = accParse.handleParse
        val findParse = accParse.findParse
        val accSchedule = accParse.accControl.accSchedule

        val handleList = actionBean.check?.handle
        val eventList = actionBean.check?.event

        //窗口根节点集合
        val rootNodeList = findParse.findRootNode(actionBean.window)
        if (actionBean.check == null) {
            //未指定check, 直接操作根元素
            handleActionResult = handleParse.parse(rootNodeList, handleList)

            if (isPrimaryAction) {
                if (!handleActionResult.success) {
                    accControl.log("无法处理↓\n${actionBean.check}\n${handleList}")
                }
            }
        } else {
            val eventNodeList = if (eventList == null) rootNodeList else
                findParse.parse(rootNodeList, eventList).nodeList

            if (eventNodeList.isNullOrEmpty()) {
                accControl.log("[event]未匹配到元素:${eventList}")
                //未找到元素
                val handleResult = handleParse.parse(rootNodeList, actionBean.check?.other)
                if (!handleResult.success) {
                    //还是未成功
                    for (otherAction in otherActionList ?: emptyList()) {
                        var otherHandleResult: HandleResult? = null
                        if (otherAction.async) {
                            thread {
                                otherHandleResult =
                                    accSchedule.scheduleAction(otherAction, null, false)
                            }
                        } else {
                            otherHandleResult = accSchedule.scheduleAction(otherAction, null, false)
                        }
                        if (otherHandleResult?.success == true) {
                            accControl.log("[other]已处理[${actionBean.actionLog()}]:${otherAction}")
                            break
                        }
                    }
                }
            } else {
                //找到了目标元素
                accControl.log(eventNodeList.toLog("[event]匹配到元素(${eventNodeList.size})${actionBean.check}↓"))
                val result = handleParse.parse(eventNodeList, handleList)
                handleActionResult = result
                if (result.success) {
                    //未处理成功
                    actionBean.check?.success?.let {
                        handleActionResult = handleParse.parse(eventNodeList, it)
                    }
                } else {
                    //未处理成功
                    actionBean.check?.fail?.let {
                        handleActionResult = handleParse.parse(eventNodeList, it)
                    }
                }
            }
        }

        if (isPrimaryAction) {
            //保存执行结果.
            actionResultMap[actionBean.actionId] = handleActionResult.success

            //处理结果
            handleActionResult.apply {
                if (success) {
                    //处理成功
                    //showElementTip(elementList)
                    next()
                } else {
                    //未处理成功
                }
            }
        }

        //现场
        runActionBeanStack.popSafe()
        _runActionBean = null

        //回调
        accControl.controlListenerList.forEach {
            it.onActionRunAfter(actionBean, isPrimaryAction, handleActionResult)
        }

        return handleActionResult
    }

    //</editor-fold desc="执行">
}