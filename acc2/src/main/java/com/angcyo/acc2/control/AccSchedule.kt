package com.angcyo.acc2.control

import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.core.BaseAccService
import com.angcyo.acc2.parse.AccParse
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.L
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.sleep
import com.angcyo.library.ex.toElapsedTime

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

    /**累加跳转次数*/
    fun jumpCountIncrement(actionId: Long) {
        val count = actionCount[actionId] ?: ActionCount()
        count.jumpCount.doCount()
        actionCount[actionId] = count
    }

    /**获取[ActionBean]的运行次数*/
    fun getRunCount(actionId: Long): Long = actionCount[actionId]?.runCount?.count ?: -1

    fun getJumpCount(actionId: Long): Long = actionCount[actionId]?.jumpCount?.count ?: -1

    fun next() {
        _targetIndex = _currentIndex + 1
    }

    //</editor-fold desc="操作">

    //<editor-fold desc="调度">

    /**开始调度*/
    fun startSchedule() {
        _startTime = nowTime()
        actionCount.clear()
        _endTime = 0
        _currentIndex = -1
        _currentActionBean = null
        _targetIndex = -1
    }

    /**结束调度*/
    fun endSchedule() {
        _endTime = nowTime()
    }

    /**额外需要执行的[ActionBean]*/
    val targetActionList = mutableListOf<ActionBean>()

    //当前调度的索引
    var _currentIndex = -1

    //当前调度的[ActionBean]
    var _currentActionBean: ActionBean? = null

    //正在调度的索引, 理论上[_currentIndex]=[_targetIndex]
    var _targetIndex = -1

    /**循环调度*/
    fun scheduleNext() {
        //调度下一个之前
        _currentActionBean?.let {
            targetActionList.remove(it)
        }
        _currentActionBean = null

        //下一个
        val nextActionBean = nextActionBean()

        if (nextActionBean == null) {
            //无[ActionBean]需要调度, 调度结束
            accControl.finish("执行完成")
        } else {
            _currentActionBean = nextActionBean
            _currentIndex = _targetIndex

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
            accControl.accPrint.log("${actionBean.actionLog()}未满足激活条件,跳过调度.")
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
                    accControl.accPrint.log("任务前置执行:${beforeAction}")
                    beforeHandleResult = _runAction(beforeAction, null, false)
                }
            }

            //处理
            if (beforeHandleResult?.success != true) {

                //action前置处理
                val beforeAction = actionBean.before
                if (beforeAction != null) {
                    accControl.accPrint.log("前置执行:${beforeAction}")
                    beforeHandleResult = _runAction(beforeAction, null, false)
                }

                //action处理
                if (beforeHandleResult?.success != true) {
                    accControl.accPrint.log("${if (isPrimaryAction) "[主线]" else ""}开始执行[${actionBean.actionId}]:${actionBean}")
                    if (isPrimaryAction) {
                        //执行统计
                        runCountIncrement(actionBean.actionId)
                    }

                    //run
                    result = _runAction(actionBean, otherActionList, isPrimaryAction)

                    if (result.success) {
                        //只有成功才打印日志, 否则日志太多
                        accControl.accPrint.log("${actionBean.actionLog()}执行结果:${result.success}")
                    }

                    //action后置处理
                    val afterAction = actionBean.after
                    if (afterAction != null) {
                        accControl.accPrint.log("后置执行:${afterAction}")
                        _runAction(afterAction, null, false)
                    }
                }

            }

            //Task后置处理
            if (isPrimaryAction) {
                val afterAction = taskBean?.after
                if (afterAction != null) {
                    accControl.accPrint.log("任务后置执行:${afterAction}")
                    _runAction(afterAction, null, false)
                }
            }

        } catch (e: Exception) {
            L.e("异常:$e")
            e.printStackTrace()
            accControl.accPrint.log("运行失败[${actionBean.title}]:$e")
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
            if (_targetIndex < 0) {
                _targetIndex = 0
            }

            //获取可以执行的目标
            accControl._taskBean?.actionList?.apply {
                for (i in _targetIndex..lastIndex) {
                    val bean = getOrNull(i)
                    if (bean != null) {
                        if (bean.enable) {
                            //找到激活的
                            if (!accParse.conditionParse.parse(bean.conditionList).success) {
                                accControl.accPrint.log("${bean.actionLog()}未满足激活条件,跳过调度.")
                                continue
                            }
                            _targetIndex = i
                            result = bean
                            break
                        } else {
                            if (bean.autoEnable) {
                                //开启了自动激活
                                if (accParse.conditionParse.parse(bean.conditionList).success) {
                                    bean.enable = true
                                    _targetIndex = i
                                    result = bean
                                    break
                                } else {
                                    accControl.accPrint.log("${bean.actionLog()}自动激活失败,跳过调度.")
                                }
                            } else {
                                accControl.accPrint.log("${bean.actionLog()}未激活,跳过调度.")
                            }
                        }
                    }
                }
            }
        }

        return result
    }

    /**解析器*/
    var accParse = AccParse(accControl)

    /**保存[ActionBean]执行的结果.
     * key = id
     * value = 成功or失败*/
    val actionResultMap = hashMapOf<Long, Boolean>()

    //正在运行的[ActionBean]
    var _runActionBean: ActionBean? = null

    /**开始执行[actionBean]
     * [actionBean] 需要执行的动作
     * [otherActionList] 当[actionBean]无法处理时, 需要执行的动作
     * [isPrimaryAction] 是否是主线动作
     * */
    fun _runAction(
        actionBean: ActionBean,
        otherActionList: List<ActionBean>?,
        isPrimaryAction: Boolean
    ): HandleResult {
        var handleActionResult = HandleResult()
        _runActionBean = actionBean

        if (BaseAccService.lastService == null) {
            accControl.error("无障碍服务连接中断")
            return handleActionResult
        }

        //等待执行
        val delayTime = accParse.parseTime(actionBean.start)
        accControl.accPrint.log("${actionBean.actionLog()}等待[$delayTime]ms后运行.")
        accControl.accPrint.next(
            actionBean.title,
            actionBean.des,
            delayTime
        )
        sleep(delayTime)

        //回调
        accControl.controlListenerList.forEach {
            it.onActionRunBefore(actionBean, isPrimaryAction)
        }

        val handleList = actionBean.check?.handle
        val eventList = actionBean.check?.event

        /* if (actionBean.check == null) {
             //未指定check, 直接操作根元素
             handleActionResult = accParse.handle(this, actionBean, handleList)
         } else {
             val eventElementList = if (eventList == null)
                 listOf(DriverWebElement(_autoParse.getBounds())) else _autoParse.parseSelector(
                 this,
                 eventList
             )

             if (eventElementList.isEmpty()) {
                 logAction?.invoke("[event]未匹配到元素:${eventList}")
                 //未找到元素
                 val handleResult = _autoParse.handle(this, actionBean, actionBean.check?.other)
                 if (!handleResult.success) {
                     //还是未成功
                     otherActionList?.forEach {
                         actionSchedule.scheduleAction(it)
                     }
                 }
             } else {
                 //找到了目标元素
                 logAction?.invoke(eventElementList.toLog("[event]匹配到元素(${eventElementList.size})${actionBean.check}↓"))
                 val result = _autoParse.handle(this, actionBean, handleList, eventElementList)
                 handleActionResult = result
                 if (result.success) {
                     //未处理成功
                     actionBean.check?.success?.let {
                         handleActionResult =
                             _autoParse.handle(this, actionBean, it, eventElementList)
                     }
                 } else {
                     //未处理成功
                     actionBean.check?.fail?.let {
                         handleActionResult =
                             _autoParse.handle(this, actionBean, it, eventElementList)
                     }
                 }
             }
         }

         //退出框架
         eventList?.forEach {
             if (it.frame?._frame != null) {
                 logAction?.invoke("退出[event]iframe:${it.frame}")
                 it.frame?._frame = null
                 driver?.switchTo()?.defaultContent()
             }
         }

         if (isPrimaryAction) {
             //保存执行结果.
             actionResultMap[actionBean.actionId] = handleActionResult?.success ?: false

             //处理结果
             handleActionResult?.apply {
                 if (success) {
                     //处理成功
                     //showElementTip(elementList)
                     actionSchedule.next(actionBean)
                 } else {
                     //未处理成功
                     if (actionSchedule._nextTime <= 0) {
                         //防止ui卡死
                         actionSchedule._nextTime = 160
                     }
                 }
             }
         }*/

        //回调
        accControl.controlListenerList.forEach {
            it.onActionRunAfter(actionBean, isPrimaryAction, handleActionResult)
        }

        _runActionBean = null

        return handleActionResult
    }

    //<editor-fold desc="调度">
}