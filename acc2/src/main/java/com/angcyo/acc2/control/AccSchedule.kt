package com.angcyo.acc2.control

import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.findFirstActionByGroup
import com.angcyo.acc2.bean.isLoopValid
import com.angcyo.acc2.core.AccNodeLog
import com.angcyo.acc2.core.ControlException
import com.angcyo.acc2.parse.AccParse
import com.angcyo.acc2.parse.ConditionParse
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.L
import com.angcyo.library.component.ThreadExecutor
import com.angcyo.library.ex.*
import java.util.*
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt

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
    val actionCount = hashMapOf<Long, ActionCount>()

    /**记录action运行的时长, 毫秒*/
    val actionTime = hashMapOf<Long, Long>()

    /**记录运行期间, 程序包名的变化*/
    val packageTrackList = mutableListOf<String>()

    /**记录输入过的文本内容
     *
     * 可以通过key[com.angcyo.acc2.action.Action.LAST_INPUT]引用到
     *
     * [com.angcyo.acc2.parse.TextParse.parse]
     * */
    val inputTextList = mutableListOf<String?>()

    /**计数统计
     * [com.angcyo.acc2.action.CountAction]*/
    val countMap = hashMapOf<String, Long>()

    //<editor-fold desc="操作">

    /**获取总共运行时长*/
    fun duration(): Long = if (_endTime <= 0 && _startTime <= 0) {
        0
    } else if (_endTime <= 0) {
        nowTime() - _startTime
    } else {
        _endTime - _startTime
    }

    fun durationStr() = when {
        _startTime <= 0 -> "未开始"
        _endTime <= 0 -> {
            "已运行 ${(nowTime() - _startTime).toElapsedTime(pattern = intArrayOf(-1, 1, 1))}"
        }
        else -> "共运行 ${duration().toElapsedTime(pattern = intArrayOf(-1, 1, 1))}"
    }

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

    fun setActionRunTime(actionId: Long, time: Long) {
        actionTime[actionId] = time
        if (time <= 0) {
            //清理时间
            if (actionId == _scheduleActionBean?.actionId) {
                _latsRunActionTime = nowTime()
            }
        }
    }

    fun getActionRunTime(actionId: Long): Long {
        return actionTime[actionId] ?: -1
    }

    fun clearActionRunTime(actionId: Long) {
        actionTime.remove(actionId)
    }

    /**预备下一个需要执行*/
    fun next() {
        _scheduleIndex = _currentIndex + 1
    }

    /**上一个*/
    fun prev() {
        var index = if (_targetIndex == null) {
            _scheduleIndex - 1
        } else {
            _targetIndex!! - 1
        }

        if (index < 0) {
            index = 0
        }

        _targetIndex = index

        val actionBean = accControl._taskBean?.actionList?.get(index)
        if (actionBean == null) {
            //no op
        } else {
            _currentIndex = index
            accControl.next(actionBean, -1)
        }
    }

    fun async(target: Runnable) {
        ThreadExecutor.execute(target)
        /*Thread(target, this.simpleHash()).apply {
            start()
        }*/
    }

    /**记录切换的应用程序*/
    fun addPackageTrack(packageName: String) {
        val last = packageTrackList.lastOrNull()
        if (last != packageName) {
            packageTrackList.add(packageName)
        }
    }

    fun indexTip() = "$_currentIndex/${actionSize()}"

    /**总共需要执行的[ActionBean]的数量*/
    fun actionSize() = accControl._taskBean?.actionList.size()

    /**打印[ActionBean]控制的日志*/
    fun printActionLog(
        log: String?,
        actionBean: ActionBean? = null,
        isPrimaryAction: Boolean = true
    ) {
        var needLog: Boolean? = null

        if (accControl._taskBean?.log == true) {
            needLog = true
        } else if (accControl._taskBean?.log == false) {
            needLog = false
        } else {
            if (actionBean == null ||
                actionBean.log == true ||
                (actionBean.log == null && isPrimaryAction)
            ) {
                needLog = true
            }
        }

        if (needLog == true) {
            accControl.log(log, isPrimaryAction)
        }
    }

    fun relyList(): List<Long>? {
        return _runActionBean?.relyList ?: _scheduleActionBean?.relyList
    }

    /**通过[group]查找分组中的第一个[ActionBean]*/
    fun findFirstActionByGroup(group: String? /*不支持分割*/): ActionBean? {
        return accControl._taskBean?.findFirstActionByGroup(group)
    }

    /**指定分组中的第一个[ActionBean]是否激活
     * 默认有多个分组时, 必须全部满足才可以. (请注意分组的顺序)
     * 可以通过包含[ConditionParse.OR],采用或者的关系
     * [actionBean] 当前的[ActionBean]
     *
     * 返回[first] 是否激活
     * [second] 激活时的分组信息
     * */
    fun isFirstActionEnableByGroup(
        actionBean: ActionBean,
        group: String? /*支持分割*/,
        isPrimaryAction: Boolean
    ): Pair<Boolean, String?> {
        if (group.isNullOrEmpty()) {
            return true to null
        }

        if (group.contains(ConditionParse.OR)) {
            //或者
            var result: Pair<Boolean, String?> = false to null
            val groupList = group.split(Action.PACKAGE_SPLIT)
            for (g in groupList) {
                if (g.isEmpty() || g == ConditionParse.OR) {
                    continue
                }

                val firstActionByGroup = findFirstActionByGroup(g)

                result = if (firstActionByGroup != null && firstActionByGroup == actionBean) {
                    //第一个就是自身
                    isActionBeanEnable(actionBean, isPrimaryAction)
                } else {
                    (firstActionByGroup?.enable == true)
                } to g

                if (result.first) {
                    break
                }
            }
            return result
        } else {
            //全部匹配
            var allMatch = true
            var matchGroup: String? = null

            val groupList = group.split(Action.PACKAGE_SPLIT)
            for (g in groupList) {
                if (g.isEmpty() || g == ConditionParse.OR) {
                    continue
                }

                //分组中的第一个ActionBean
                val firstActionByGroup = findFirstActionByGroup(g)

                allMatch = if (firstActionByGroup != null && firstActionByGroup == actionBean) {
                    //第一个就是自身
                    isActionBeanEnable(actionBean, isPrimaryAction)
                } else {
                    (firstActionByGroup?.enable == true)
                }
                matchGroup = g

                if (!allMatch) {
                    break
                }
            }

            return allMatch to matchGroup
        }
    }

    /**当前[actionBean]是否在分组中的第一个*/
    fun isFirstActionInGroup(actionBean: ActionBean): Boolean {
        val group = actionBean.group
        if (group.isNullOrEmpty()) {
            return true
        }
        var result = false
        val groupList = group.split(Action.PACKAGE_SPLIT)
        for (g in groupList) {
            val findFirstActionByGroup = findFirstActionByGroup(g)
            result = findFirstActionByGroup == actionBean
            if (!result) {
                break
            }
        }
        return result
    }

    fun isPrimaryAction(actionBean: ActionBean?): Boolean {
        if (actionBean == null) {
            return false
        }
        return targetActionList.contains(actionBean) || accControl._taskBean?.actionList?.contains(
            actionBean
        ) == true
    }

    //</editor-fold desc="操作">

    //<editor-fold desc="调度">

    /**开始调度*/
    fun startSchedule() {
        _startTime = nowTime()
        actionCount.clear()
        actionTime.clear()
        actionResultMap.clear()
        packageTrackList.clear()
        inputTextList.clear()
        countMap.clear()
        _endTime = 0
        _currentIndex = -1
        _scheduleActionBean = null
        _scheduleIndex = -1
        _runActionBean = null
        _latsRunActionTime = 0L
        _lastRunActionHash = 0
        runActionBeanStack.clear()
        _isLeaveWindow = false
        accParse.onScheduleStart(this)
    }

    /**结束调度*/
    fun endSchedule() {
        _endTime = nowTime()
        accParse.onScheduleEnd(this)
    }

    val isRunPrimaryAction: Boolean
        get() = _runActionBean != null && _runActionBean == _scheduleActionBean

    /**额外需要执行的[ActionBean]*/
    val targetActionList = mutableListOf<ActionBean>()

    //当前调度的索引, 提示使用
    var _currentIndex = -1

    //当前调度的[ActionBean], 主线[ActionBean], 也有可能是[targetActionList]中的
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
        } else if (nextActionBean.async == true) {
            async {
                scheduleAction(ControlContext().apply {
                    control = accControl
                    action = nextActionBean
                }, nextActionBean, accControl._taskBean?.backActionList, true)
            }
            printActionLog("异步执行:${nextActionBean.actionLog()}")
            next()
        } else {
            val taskLimitRunTime = accControl._taskBean?.taskLimitRunTime ?: -1
            if (taskLimitRunTime > 0) {
                //任务指定的运行时长限制
                val nowTime = nowTime()
                if (nowTime - _startTime >= taskLimitRunTime) {
                    //任务超时

                    val limitTip = "任务超时,限制运行时长[${taskLimitRunTime.toElapsedTime()}]"

                    val taskLimitTime = accControl._taskBean?.taskLimitTime
                    if (taskLimitTime == null) {
                        //默认处理
                        accControl.error(limitTip)
                    } else {
                        runAction(ControlContext().apply {
                            control = accControl
                            action = taskLimitTime
                        }, taskLimitTime, null, false)
                        if (accControl.isControlRunning) {
                            //强制终止任务
                            accControl.error(limitTip)
                        }
                    }
                }
            }

            //调度
            if (accControl.isControlRunning) {
                //运行状态
                _scheduleActionBean = nextActionBean
                _currentIndex = _scheduleIndex

                val controlContext = ControlContext().apply {
                    control = accControl
                    action = nextActionBean
                }

                //核心调度
                val result = scheduleAction(
                    controlContext,
                    nextActionBean,
                    accControl._taskBean?.backActionList,
                    true
                )

                //Loop解析
                val loop = nextActionBean.loop

                if (loop == null) {
                    if (result.forceSuccess || (!result.forceFail && result.success)) {
                        //无loop, 成功后
                        next()
                    }
                } else {
                    //loop处理
                    if (!loop.valid || (loop.valid && loop.isLoopValid(accParse))) {
                        //如果不需要验证数据结构, 或者 需要验证但是验证通过了
                        if (!accParse.loopParse.parse(
                                controlContext,
                                null,
                                nextActionBean,
                                result,
                                loop
                            )
                        ) {
                            if (loop.exit == null) {
                                //未被loop处理
                                next()
                            } else {
                                accParse.handleParse.parse(controlContext, null, loop.exit)
                            }
                        }

                    }
                }
            }
        }
    }

    /**开始调度[actionBean]*/
    fun scheduleAction(
        controlContext: ControlContext,
        actionBean: ActionBean,
        otherActionList: List<ActionBean>?,
        isPrimaryAction: Boolean
    ): HandleResult {
        var result = HandleResult()

        if (!accControl.isControlRunning) {
            return result
        }

        //激活条件判断
        if (!accParse.conditionParse.parse(actionBean.conditionList).success) {
            result.success = false
            printActionLog("${actionBean.actionLog()}未满足激活条件,跳过调度.", actionBean, isPrimaryAction)
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
                val taskBeforeAction = taskBean?.before
                if (taskBeforeAction != null) {
                    printActionLog("任务前置执行:${taskBeforeAction}", taskBeforeAction, isPrimaryAction)
                    beforeHandleResult = runAction(controlContext.copy {
                        action = taskBeforeAction
                    }, taskBeforeAction, null, false)
                }
            }

            //跳过原本的action处理
            var skipActionRun = false

            if (beforeHandleResult == null) {
                skipActionRun = false
            } else {
                if (beforeHandleResult.forceFail) {
                    skipActionRun = false
                } else if (beforeHandleResult.success || beforeHandleResult.forceSuccess) {
                    skipActionRun = true
                }
            }

            //处理
            if (!skipActionRun) {

                //action前置处理
                val beforeAction = actionBean.before
                if (beforeAction != null) {
                    printActionLog("前置执行:${beforeAction}", beforeAction, isPrimaryAction)
                    beforeHandleResult = runAction(controlContext.copy {
                        action = beforeAction
                    }, beforeAction, null, false)
                }

                skipActionRun = false
                if (beforeHandleResult == null) {
                    skipActionRun = false
                } else {
                    if (beforeHandleResult.forceFail) {
                        skipActionRun = false
                    } else if (beforeHandleResult.success || beforeHandleResult.forceSuccess) {
                        skipActionRun = true
                    }
                }

                //action处理
                if (!skipActionRun) {
                    val pLog = if (isPrimaryAction) "[主线]" else ""
                    printActionLog(
                        "${pLog}开始执行(${indexTip()})[${actionBean.actionLog()}]:${actionBean}",
                        actionBean,
                        isPrimaryAction
                    )
                    //执行统计
                    runCountIncrement(actionBean.actionId)

                    //run
                    result = runAction(controlContext.copy {
                        action = actionBean
                    }, actionBean, otherActionList, isPrimaryAction)

                    if (result.success) {
                        //只有成功才打印日志, 否则日志太多
                        controlContext.log {
                            append("执行结果:${result.success}")
                        }
                    }

                    //action后置处理
                    val afterAction = actionBean.after
                    if (afterAction != null) {
                        printActionLog("后置执行:${afterAction}", afterAction, isPrimaryAction)

                        val runActionResult = runAction(controlContext.copy {
                            action = afterAction
                        }, afterAction, null, false)

                        if (isPrimaryAction) {
                            if (runActionResult.forceSuccess) {
                                //强制成功, 跳过当前的actionBean
                                next()
                            }
                        }
                    }
                }
            }

            //Task后置处理
            if (isPrimaryAction) {
                val taskAfterAction = taskBean?.after
                if (taskAfterAction != null) {
                    printActionLog("任务后置执行:${taskAfterAction}", taskAfterAction, isPrimaryAction)
                    runAction(controlContext.copy {
                        action = taskAfterAction
                    }, taskAfterAction, null, false)
                }
            }

        } catch (e: Exception) {
            L.e("异常:$e")
            e.printStackTrace()
            printActionLog(
                "scheduleAction运行失败[${actionBean.actionLog()}]:$e",
                null,
                isPrimaryAction
            )
            printActionLog(e.stackTraceToString(), null, isPrimaryAction)
        }

        return result
    }

    /**下一个需要执行的[ActionBean]*/
    fun nextActionBean(isPrimaryAction: Boolean = true): ActionBean? {
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
                    if (isActionBeanGroupEnable(bean, isPrimaryAction)) {
                        _scheduleIndex = i
                        result = bean
                        break
                    }
                }
            }
        }

        return result
    }

    /**判断[action]以及[group]中是否激活, 激活后才允许运行*/
    fun isActionBeanGroupEnable(action: ActionBean?, isPrimaryAction: Boolean): Boolean {
        if (action != null) {
            //分组判断
            val group = action.group
            if (!group.isNullOrEmpty() && !isFirstActionInGroup(action)) {
                //具有分组标识, 并且不是第一个
                return if (isFirstActionEnableByGroup(action, group, isPrimaryAction).first) {
                    //printActionLog("${action.actionLog()}的分组[${group}]中第一个ActionBean激活.")
                    true
                } else {
                    printActionLog(
                        "${action.actionLog()}的分组[${group}]中第一个ActionBean未激活,跳过.",
                        action,
                        isPrimaryAction
                    )
                    false
                }
            }
            return isActionBeanEnable(action, isPrimaryAction)
        }
        return false
    }

    /**判断[action]是否激活, 激活后才允许运行
     * 不进行分组判断
     * 分组判断请使用[isActionBeanGroupEnable]
     * [isPrimaryAction] 日志打印使用*/
    fun isActionBeanEnable(action: ActionBean?, isPrimaryAction: Boolean): Boolean {
        if (action != null) {
            val pLog = if (isPrimaryAction) "[主线]" else ""
            //激活判断
            if (action.enable || action._enable != null) {
                var factor = 0
                if (action.randomEnable) {
                    //需要处理随机激活
                    action._enable = action.enable
                    if (isDebugType()) {
                        //debug模式下, 直接激活. 方便测试
                        return action.enable
                    }
                    action.enable = if (action.randomAmount.isNullOrEmpty()) {
                        //未指定随机概率
                        nextBoolean()
                    } else {
                        //指定了随机的概率
                        factor = nextInt(1, 101) //[1-100]
                        accParse.expParse.parseAndCompute(
                            action.randomAmount,
                            inputValue = factor.toFloat()
                        )
                    }
                    printActionLog(
                        "${pLog}${action.actionLog()}随机[${factor}][${action.randomAmount}]激活:[${action.enable}]",
                        action,
                        isPrimaryAction
                    )
                    return action.enable
                } else {
                    if (action.conditionList != null) {
                        if (!accParse.conditionParse.parse(action.conditionList).success) {
                            printActionLog(
                                "${pLog}${action.actionLog()}未满足激活条件,跳过.",
                                action,
                                isPrimaryAction
                            )
                            return false
                        }
                    }
                    return true
                }
            } else if (action.autoEnable) {
                //开启了自动激活
                val conditionResult = accParse.conditionParse.parse(action.conditionList)
                return if (conditionResult.success) {
                    action.enable = true
                    printActionLog(
                        "${pLog}${action.actionLog()}自动激活成功:[${conditionResult.conditionBean}]",
                        action,
                        isPrimaryAction
                    )
                    true
                } else {
                    printActionLog(
                        "${pLog}${action.actionLog()}自动激活失败,跳过.",
                        action,
                        isPrimaryAction
                    )
                    false
                }
            } else {
                //未激活的
                printActionLog("${pLog}${action.actionLog()}未激活,跳过.", action, isPrimaryAction)
                return false
            }
        }
        return false
    }

    /**跳过当前分组的所有[ActionBean],指定下一个需要调度的[ActionBean]
     * [group]支持分割
     * [actionBean] 当前的[ActionBean]
     * */
    fun nextScheduleActionByGroup(actionBean: ActionBean, group: String?): Boolean {
        val pair = isFirstActionEnableByGroup(actionBean, group, isPrimaryAction(actionBean))
        val targetGroup = pair.second
        if (!pair.first || targetGroup == null) {
            return false
        }

        var result = false
        var isFindGroup = false

        //获取可以执行的目标
        accControl._taskBean?.actionList?.apply {
            for (i in 0..lastIndex) {
                val bean = getOrNull(i)

                val isInGroup = bean?.group?.contains(targetGroup) == true

                if (isFindGroup && !isInGroup) {
                    printActionLog("下一个执行目标[$i/${actionSize()}]:$bean")
                    _targetIndex = i
                    result = true
                    break
                }

                if (isInGroup) {
                    isFindGroup = true
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
                printActionLog("强制执行目标[$it/${actionSize()}]:$bean")
                _targetIndex = it
            }
        }
    }

    fun clearTargetAction() {
        targetActionList.clear()
    }

    /**添加需要额外执行的[ActionBean]*/
    fun addTargetAction(list: List<ActionBean>) {
        list.forEach {
            addTargetAction(it)
        }
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

    /**同一个action,一直在运行的时长,毫秒*/
    var _latsRunActionTime = 0L
    var _lastRunActionHash = 0

    val runActionBeanStack = Stack<ActionBean>()

    //正在运行的[ActionBean], 有可能是主线[ActionBean]
    var _runActionBean: ActionBean? = null

    /**开始执行[actionBean]
     * [actionBean] 需要执行的动作
     * [otherActionList] 当[actionBean]无法处理时, 需要执行的动作
     * [isPrimaryAction] 是否是主线动作
     * */
    fun runAction(
        controlContext: ControlContext,
        actionBean: ActionBean,
        otherActionList: List<ActionBean>?,
        isPrimaryAction: Boolean
    ): HandleResult {
        val handleActionResult = HandleResult()
        if (!accControl.isControlRunning) {
            return handleActionResult
        }

        if (isPrimaryAction) {
            //窗口根节点集合, 记录窗口切换轨迹
            accParse.accControl.accService()?.rootInActiveWindow?.packageName?.let {
                addPackageTrack(it.str())
            }
        }

        val context = controlContext.copy {
            action = actionBean
        }

        if (actionBean.async == true && !accControl.isControlMainThread()) {
            //异步执行
            async {
                runActionInner(
                    context,
                    actionBean,
                    otherActionList,
                    isPrimaryAction,
                    handleActionResult
                )
            }
        } else {
            //同步执行
            if (runActionBefore(context, actionBean, isPrimaryAction, handleActionResult)) {
                if (!accControl.isControlRunning) {
                    return handleActionResult
                }
                runActionInner(
                    context,
                    actionBean,
                    otherActionList,
                    isPrimaryAction,
                    handleActionResult
                )
                if (!accControl.isControlRunning) {
                    return handleActionResult
                }
                runActionAfter(context, actionBean, isPrimaryAction, handleActionResult)
            }
        }
        return handleActionResult
    }

    /**返回是否运行成功, 运行失败, 会中断后续执行*/
    fun runActionBefore(
        controlContext: ControlContext,
        actionBean: ActionBean,
        isPrimaryAction: Boolean,
        handleActionResult: HandleResult
    ): Boolean {
        runActionBeanStack.push(actionBean)
        _runActionBean = actionBean

        if (accControl.accService() == null) {
            runActionBeanStack.popSafe()
            _runActionBean = null
            accControl.error("无障碍服务连接中断")
            return false
        }

        //激活条件判断
        if ((isPrimaryAction && !accParse.conditionParse.parse(actionBean.conditionList).success) ||
            (!isPrimaryAction && !isActionBeanGroupEnable(actionBean, isPrimaryAction))
        ) {
            handleActionResult.success = false
            runActionBeanStack.popSafe()
            _runActionBean = null
            printActionLog("${actionBean.actionLog()}未满足激活条件,跳过执行.", actionBean, isPrimaryAction)
            return false
        }

        //等待执行
        val _start =
            if (isDebugType()) actionBean.debugStart ?: actionBean.start else actionBean.start
        val delayTime = accParse.parseTime(_start)
        if (isPrimaryAction) {
            accControl.next(actionBean, delayTime)
        }
        printActionLog("${actionBean.actionLog()}等待[$delayTime]ms后运行.", actionBean, isPrimaryAction)
        sleep(delayTime)

        //停止了运行
        if (!accControl.isControlRunning) {
            return false
        }

        val lastActionHash = _lastRunActionHash
        val newActionHash = if (isPrimaryAction) {
            actionBean.hashCode()
        } else {
            lastActionHash
        }
        if (isPrimaryAction) {
            _lastRunActionHash = newActionHash
        }

        //运行时长限制判断
        if (lastActionHash == newActionHash) {
            if (isPrimaryAction) {
                val limitRunTime = if (actionBean.limitRunTime >= 0) {
                    actionBean.limitRunTime
                } else {
                    accControl._taskBean?.limitRunTime ?: -1
                }

                if (limitRunTime > 0 && !isDebugType() /*debug*/) {
                    val runTime = nowTime() - _latsRunActionTime
                    if (runTime >= limitRunTime) {
                        //运行时长超出限制
                        if (actionBean.check?.limitTime == null) {
                            accControl.error("[${actionBean.actionLog()}]执行时长超出限制[${limitRunTime}]ms")
                        } else {

                            val handleParse = accParse.handleParse
                            val findParse = accParse.findParse

                            //窗口根节点集合
                            val rootNodeList = findParse.findRootNode(actionBean.window)

                            _latsRunActionTime = nowTime()
                            handleParse.parse(
                                controlContext,
                                rootNodeList,
                                actionBean.check?.limitTime
                            ).copyTo(handleActionResult)
                        }
                        if (!handleActionResult.isSuccessResult()) {
                            runActionBeanStack.popSafe()
                            _runActionBean = null
                            return false
                        }
                    }
                }
            }
        } else {
            //切换了action
            if (isPrimaryAction || actionBean.actionId != -1L) {
                _latsRunActionTime = nowTime()
            }
        }

        if (isPrimaryAction || actionBean.actionId != -1L) {
            //运行时长
            val actionRunTime = nowTime() - _latsRunActionTime
            setActionRunTime(actionBean.actionId, actionRunTime)
        }

        //运行开始的回调
        accControl.controlListenerList.forEach {
            it.onActionRunBefore(accControl, actionBean, isPrimaryAction)
        }

        return true
    }

    /**标识是否离开了主程序*/
    var _isLeaveWindow = false

    fun runActionInner(
        controlContext: ControlContext,
        actionBean: ActionBean,
        otherActionList: List<ActionBean>?,
        isPrimaryAction: Boolean,
        handleActionResult: HandleResult
    ) {
        if (!accControl.isControlRunning) {
            return
        }

        val handleParse = accParse.handleParse
        val findParse = accParse.findParse
        val accSchedule = accParse.accControl.accSchedule

        val actionCheckBean = actionBean.check
        val handleList = actionCheckBean?.handle
        val eventList = actionCheckBean?.event

        controlContext.check = actionCheckBean

        //窗口根节点集合
        val windowBean = actionCheckBean?.window ?: actionBean.window
        val rootNodeList = findParse.findRootNode(windowBean)

        if (isPrimaryAction) {
            val haveWindow = !rootNodeList.isNullOrEmpty()
            _isLeaveWindow = !haveWindow

            accControl.controlListenerList.forEach {
                it.onActionLeave(accControl, actionBean, isPrimaryAction, _isLeaveWindow)
            }

            if (_isLeaveWindow) {
                AccNodeLog().apply {
                    logMinWindowInfo = true
                    logWindowNode = false
                    getAccessibilityWindowLog().apply {
                        printActionLog("未匹配到窗口[$windowBean]↓\n$this", actionBean, isPrimaryAction)
                    }
                }

                val leaveActionBean = actionBean.leave ?: accControl._taskBean?.leave
                if (leaveActionBean != null) {
                    val leaveResult = runAction(controlContext.copy {
                        action = leaveActionBean
                    }, leaveActionBean, null, false)
                    if (!leaveResult.forceFail && (leaveResult.success || leaveResult.forceSuccess)) {
                        //处理成功
                        return
                    }
                }
            }
        }

        if (actionCheckBean == null) {
            //未指定check, 直接操作根元素
            handleParse.parse(controlContext, rootNodeList, handleList).copyTo(handleActionResult)

            if (isPrimaryAction) {
                if (!handleActionResult.isSuccessResult()) {
                    if (handleList == null) {
                        printActionLog(
                            "无法处理${actionBean.actionLog()}, 请检查[check]是否未初始化.",
                            actionBean,
                            isPrimaryAction
                        )
                        accControl.error(ControlException("请检查[check]是否未初始化."))
                    } else {
                        printActionLog(
                            "无法处理↓\nhandle:${handleList}",
                            actionBean,
                            isPrimaryAction
                        )
                    }
                }
            }
        } else {
            //是否要跳过处理
            var skipHandle = false
            val eventNodeList = if (eventList == null) {
                rootNodeList
            } else {
                val parse = findParse.parse(controlContext, rootNodeList, eventList)
                if (parse.forceSuccess) {
                    skipHandle = true
                    handleActionResult.success = true
                    printActionLog(
                        "${actionCheckBean.checkLog()}[event]强制成功,跳过处理:${eventList}",
                        actionBean,
                        isPrimaryAction
                    )
                } else if (parse.forceFail) {
                    skipHandle = true
                    handleActionResult.success = false
                    printActionLog(
                        "${actionCheckBean.checkLog()}[event]强制失败,跳过处理:${eventList}",
                        actionBean,
                        isPrimaryAction
                    )
                }
                parse.nodeList
            }

            //流程处理
            if (skipHandle) {
                //no op
            } else if (eventNodeList.isNullOrEmpty()) {
                controlContext.log {
                    append("[event]未匹配到元素:${eventList}")
                }

                //未找到元素, 交给other处理
                val handleResult =
                    handleParse.parse(controlContext, rootNodeList, actionCheckBean.other)

                if (handleResult.forceSuccess) {
                    handleActionResult.success = true
                }

                if (!handleResult.success) {
                    //还是未成功
                    for (otherAction in otherActionList ?: emptyList()) {
                        var otherHandleResult: HandleResult? = null
                        if (otherAction.async != false) {
                            async {
                                otherHandleResult =
                                    accSchedule.scheduleAction(controlContext.copy {
                                        action = otherAction
                                    }, otherAction, null, false)
                                if (otherHandleResult?.forceSuccess == true) {
                                    handleActionResult.success = true
                                }
                            }
                        } else {
                            otherHandleResult = accSchedule.scheduleAction(controlContext.copy {
                                action = otherAction
                            }, otherAction, null, false)
                            if (otherHandleResult?.forceSuccess == true) {
                                handleActionResult.success = true
                            }
                        }
                        if (otherHandleResult?.success == true) {
                            printActionLog(
                                "[other]已处理[${actionBean.actionLog()}]:${otherAction}",
                                actionBean,
                                isPrimaryAction
                            )
                            break
                        } else {
                            //other action 也没有处理成功
                            accControl.controlListenerList.forEach {
                                it.onActionNoHandle(accControl, actionBean, isPrimaryAction)
                            }

                            //如果这个时候, 丢失了窗口节点
                            val nodeList = findParse.findRootNode(null)
                            if (nodeList.isNullOrEmpty() ||
                                (nodeList.size() == 1 && nodeList.firstOrNull()?.childCount ?: 0 <= 0)
                            ) {
                                //leave
                                _isLeaveWindow = true
                                val loseActionBean = actionBean.lose ?: accControl._taskBean?.lose
                                if (loseActionBean != null) {
                                    val runResult = runAction(controlContext.copy {
                                        action = loseActionBean
                                    }, loseActionBean, null, false)

                                    //result
                                    if (runResult.forceFail) {
                                        handleActionResult.success = false
                                    } else if (runResult.forceSuccess) {
                                        handleActionResult.success = true
                                        handleActionResult.nodeList = runResult.nodeList
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                //找到了目标元素
                controlContext.log {
                    append("[event]匹配到元素:${eventNodeList.size}")
                }

                val result = handleParse.parse(controlContext, eventNodeList, handleList)
                result.copyTo(handleActionResult)
                if (result.isSuccessResult()) {
                    //处理成功
                    actionCheckBean.success?.let {
                        handleParse.parse(controlContext, eventNodeList, it)
                            .copyTo(handleActionResult)
                    }
                } else {
                    //未处理成功
                    actionCheckBean.fail?.let {
                        handleParse.parse(controlContext, eventNodeList, it)
                            .copyTo(handleActionResult)
                    }
                }
            }
        }
    }

    fun runActionAfter(
        controlContext: ControlContext,
        actionBean: ActionBean,
        isPrimaryAction: Boolean,
        handleActionResult: HandleResult
    ) {
        if (isPrimaryAction) {
            //保存执行结果.
            actionResultMap[actionBean.actionId] = handleActionResult.success

            //处理结果
            handleActionResult.apply {
                if (success) {
                    //处理成功
                    //showElementTip(elementList)
                    //next()
                } else {
                    //未处理成功
                    //getRunCount(actionBean.actionId)
                }

                //运行次数限制
                val limitRunCount = if (actionBean.limitRunCount >= 0) {
                    actionBean.limitRunCount
                } else {
                    accControl._taskBean?.limitRunCount ?: -1
                }

                if (limitRunCount > 0) {
                    val runCount = getRunCount(actionBean.actionId)
                    if (runCount >= limitRunCount) {
                        //运行次数超出限制
                        if (actionBean.check?.limitRun == null) {
                            //默认处理
                            if (!success) {
                                accControl.error("[${actionBean.actionLog()}]执行次数超出限制[${limitRunCount}]")
                            }
                        } else {
                            val handleParse = accParse.handleParse
                            val findParse = accParse.findParse
                            //窗口根节点集合
                            val rootNodeList = findParse.findRootNode(actionBean.window)
                            handleParse.parse(
                                controlContext,
                                rootNodeList,
                                actionBean.check?.limitRun
                            ).copyTo(handleActionResult)
                        }
                    }
                }
            }
        }

        //现场
        runActionBeanStack.popSafe()
        _runActionBean = null

        //form
        accParse.formParse.parseActionForm(
            controlContext,
            accControl,
            actionBean,
            handleActionResult
        )

        //回调
        accControl.controlListenerList.forEach {
            it.onActionRunAfter(accControl, actionBean, isPrimaryAction, handleActionResult)
        }
    }

    //</editor-fold desc="执行">
}