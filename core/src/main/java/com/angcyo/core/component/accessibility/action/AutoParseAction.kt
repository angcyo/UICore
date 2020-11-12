package com.angcyo.core.component.accessibility.action

import android.graphics.PointF
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.action.a.*
import com.angcyo.core.component.accessibility.parse.*
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.component._delay
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ
import kotlin.math.roundToLong
import kotlin.random.Random.Default.nextInt

/**
 * 智能识别的[Action], 通过配置一些关键字, 快速创建对应的[Action]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParseAction : BaseAccessibilityAction() {

    /**如果是获取文本的任务, 那么多获取到文本时, 触发的回调*/
    var onGetTextResult: ((String?, List<CharSequence>?) -> Unit)? = null

    /**根据给定的[wordInputIndexList] [wordTextIndexList]返回对应的文本信息*/
    var onGetWordTextListAction: ((List<String>) -> List<String>?)? = null

    var onStartTaskAction: ((autoParseAction: AutoParseAction, formBean: FormBean, loop: String?, count: Long) -> Boolean)? =
        null

    /**请求表单时, 配置表单数据的回调*/
    var onConfigParams: ((params: HashMap<String, Any>) -> Unit)? = null

    /**需要执行的[Action]描述*/
    var actionBean: ActionBean? = null

    /**解析核心*/
    var autoParser: AutoParser = AutoParser()

    /**获取到的文本, 临时存储*/
    var getTextList: MutableList<CharSequence>? = null

    //指令列表
    val cmdActionList = mutableListOf<BaseAction>()

    /**[ActionBean.HANDLE_TYPE_ORDER2]顺序执行次数统计*/
    val doOrderCount: ActionCount = ActionCount()

    init {
        cmdActionList.apply {
            add(BackAction())
            add(ClickAction())
            add(ClickTouchAction())
            add(CopyAction())
            add(DefaultAction())
            add(DisableAction())
            add(DoOtherAction())
            add(DoubleAction())
            add(EnableAction())
            add(ErrorAction())
            add(FalseAction())
            add(FinishAction())
            add(FlingAction())
            add(FocusAction())
            add(FullscreenAction())
            add(GetTextAction())
            add(HideWindowAction())
            add(HomeAction())
            add(JumpAction())
            add(KeyAction())
            add(LongClickAction())
            add(MoveAction())
            add(RandomAction())
            add(ScrollBackwardAction())
            add(ScrollForwardAction())
            add(SetTextAction())
            add(SleepAction())
            add(StartAction())
            add(TouchAction())
            add(TrueAction())
            add(UrlAction())
            add(NotTouchableAction())
            add(TaskAction().apply {
                onStartTask =
                    { autoParseAction: AutoParseAction, taskFormBean: FormBean, loop: String?, count: Long ->
                        onStartTaskAction?.invoke(
                            autoParseAction,
                            taskFormBean,
                            loop,
                            count
                        ) ?: false
                    }
            })
            add(DestroyAction())
        }
    }

    //<editor-fold desc="周期回调方法">

    /**检查是否是匹配的界面*/
    override fun checkEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        val constraintList: List<ConstraintBean>? = actionBean?.check?.event
        if (constraintList == null) {
            doActionFinish(ActionException("eventConstraint is null."))
            return false
        }
        if (constraintList.isEmpty()) {
            //如果是空约束, 则直接返回true
            return true
        }
        return autoParser.parse(service, this, nodeList, constraintList)
    }

    /**当[checkEvent]无法通过时, 需要怎么处理*/
    override fun checkOtherEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {

        var handleResult: Boolean =
            parseHandleAction(
                service,
                currentAccessibilityAction(),
                nodeList,
                actionBean?.check?.other
            )

        if (!handleResult) {
            //处理没有成功, 再进行计数统计
            handleResult = super.checkOtherEvent(service, event, nodeList)
        }

        if (checkOtherEventCount.isMaxLimit()) {
            //超限制
            handleResult = parseHandleAction(
                service,
                currentAccessibilityAction(),
                nodeList,
                actionBean?.check?.otherOut
            )
            if (handleResult) {
                //处理了otherEvent, 清空计数
                checkOtherEventCount.clear()
            }
        }

        return handleResult
    }

    /**解析[List<ConstraintBean>]约束对应的Node, 并且执行对应的指令.
     * 返回值表示 指令是否执行成功
     * */
    fun parseHandleAction(
        service: BaseAccessibilityService,
        fromAction: BaseAccessibilityAction?,
        nodeList: List<AccessibilityNodeInfo>,
        constraintList: List<ConstraintBean>?,
        onGetTextResult: (String?, List<CharSequence>?) -> Unit = { _, _ -> }
    ): Boolean {
        //执行对应的action操作
        var result = false

        if (constraintList != null) {
            autoParser.parse(
                service,
                this,
                nodeList,
                constraintList.filterSystem(firstPackageName()),
                true
            ) {
                for (parseResult in it) {

                    val constraint = parseResult.constraint
                    if (!constraint.enable) {
                        handleActionLog("跳过Constraint[${constraint.constraintId}]的处理.")
                        continue
                    }

                    val actionId = fromAction?.actionBean()?.actionId ?: actionBean?.actionId ?: -1
                    val actionIds = constraint.actionIds
                    if (!actionIds.isNullOrEmpty()) {
                        if (!_haveActionId(actionIds, actionId)) {
                            //不满足条件
                            continue
                        }
                    }
                    val notActionIds = constraint.notActionIds
                    if (!notActionIds.isNullOrEmpty()) {
                        if (_haveActionId(notActionIds, actionId)) {
                            //不满足条件
                            continue
                        }
                    }

                    val delay = constraint.delay?.toLongOrNull() ?: -1

                    if (delay >= 0) {
                        handleActionLog("将延迟[$delay ms]执行[${constraint.constraintId}]的处理.")
                        _delay(delay) {
                            handleAction(service, parseResult, onGetTextResult)
                        }
                    } else {
                        //执行action
                        val handleResult: HandleResult =
                            handleAction(service, parseResult, onGetTextResult)

                        if (!handleResult.result) {
                            //执行失败后,
                            if (constraint.not != null) {
                                handleResult.result = parseHandleAction(
                                    service,
                                    fromAction,
                                    nodeList,
                                    constraint.not,
                                    onGetTextResult
                                )
                            }
                        }

                        //执行结果
                        result = handleResult.result || result

                        if (handleResult.finish) {
                            //直接完成
                            result = true
                            doActionFinish()
                            break
                        }

                        //是否跳过后续action
                        if (handleResult.jumpNextHandle) {
                            break
                        }
                    }
                }
            }
        }
        return result
    }

    /**是否包含当前的[actionId]*/
    fun _haveActionId(actionIds: String?, actionId: Long): Boolean {
        if (actionIds.isNullOrEmpty()) {
            return false
        }

        return if (actionIds.contains("~") || actionIds.contains(":")) {

            val segmentList = if (actionIds.contains(";")) {
                //具有多段
                actionIds.split(";")
            } else {
                listOf(actionIds)
            }

            fun isInSegment(segment: String): Boolean {
                //指定的是一个范围
                val split = segment.split("~")
                val startId = split.getOrNull2(0)?.toLongOrNull()
                val endId = split.getOrNull2(1)?.toLongOrNull()

                return if (startId != null && endId != null) {
                    actionId >= startId && actionId <= endId
                } else {
                    segment.have("$actionId")
                }
            }

            for (segment in segmentList) {
                if (isInSegment(segment)) {
                    return true
                }
            }

            return false
        } else {
            actionIds.have("$actionId")
        }
    }

    /**当[checkEvent]通过时, 需要怎么处理*/
    override fun doAction(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        doActionCount.start()
        doOrderCount.start()

        checkOtherEventCount.clear()

        if (doActionCount.isMaxLimit()) {
            return
        }

        //是否执行成功了
        var isFinish = false

        val constraintList: List<ConstraintBean>? = actionBean?.check?.handle
        if (constraintList == null) {
            doActionFinish(ActionException("handleConstraint is null."))
        } else {
            //解析拿到对应的node
            val handleType = actionBean?.handleType

            val filterConstraintList = if (handleType != null) {
                constraintList.filterSystem(firstPackageName())
            } else {
                constraintList
            }

            val handleConstraintList: List<ConstraintBean> = when (handleType) {
                ActionBean.HANDLE_TYPE_RANDOM -> filterConstraintList.randomGet(1) //随机获取处理约束
                ActionBean.HANDLE_TYPE_ORDER, ActionBean.HANDLE_TYPE_ORDER2 -> {
                    val count =
                        if (handleType == ActionBean.HANDLE_TYPE_ORDER2) doOrderCount.count else doActionCount.count

                    val nextIndex: Int = (count % filterConstraintList.size).toInt()
                    val next: ConstraintBean? = filterConstraintList.getOrNull(nextIndex)

                    if (next == null) {
                        filterConstraintList
                    } else {
                        listOf(next)
                    }
                } //顺序获取处理约束
                else -> filterConstraintList //默认
            }

            //执行对应的action操作
            val result: Boolean =
                parseHandleAction(
                    service,
                    this,
                    nodeList,
                    handleConstraintList
                ) { formKey, textList ->
                    handleGetTextResult(formKey, textList)
                }

            //计数
            doOrderCount.doCount()

            //判断是否执行成功
            if (result) {
                isFinish = true
                //完成
                if (isActionStart()) {
                    //[parseHandleAction]也会执行[doActionFinish],这样就会导致逻辑执行2次了
                    doActionFinish()
                }
            }

            //是否需要强制执行完成
            var actionMaxCount: Long = actionBean?.actionMaxCount ?: -1L
            if (accessibilityInterceptor != null && actionMaxCount > 0) {

                if (handleType == ActionBean.HANDLE_TYPE_RANDOM) {
                    //随机设置[actionMaxCount]
                    actionMaxCount =
                        (actionMaxCount + actionMaxCount * nextInt(0, 100) / 100f).roundToLong()
                }

                if (doActionCount.count >= actionMaxCount) {
                    isFinish = true
                    if (isActionStart()) {
                        doActionFinish()
                    }
                }
            }
        }

        if (isFinish) {
            //已经执行了 [doActionFinish]
            checkOtherEventCount.clear()
        } else {
            //没有直接完成
            super.doAction(service, event, nodeList)
        }
    }

    /**当前执行的[action]无法处理时, 进入[Back]流程的处理*/
    override fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        val constraintList: List<ConstraintBean>? = actionBean?.check?.back

        if (constraintList != null) {

            var result = false

            val eventConstraintList: List<ConstraintBean>? = actionBean?.check?.event
            if (eventConstraintList == null) {
                //无界面约束匹配, 则不检查. 直接处理
                result = parseHandleAction(service, action, nodeList, constraintList)
            } else {
                //匹配当前界面, 匹配成功后, 再处理
                if (autoParser.parse(service, this, nodeList, eventConstraintList)) {
                    //匹配成功
                    result = parseHandleAction(service, action, nodeList, constraintList)
                }
            }
            return result
        }
        return super.doActionWidth(action, service, event, nodeList)
    }

    /**[Action]处理完成*/
    override fun doActionFinish(error: ActionException?) {
        var handleError: ActionException? = error
        if (error != null && actionBean?.errorHandleType == ActionBean.ERROR_HANDLE_TYPE_NEXT) {
            //异常后, 需要继续
            if (error is ActionInterruptedNextException) {

            } else {
                handleError = ActionInterruptedNextException(error.message)
            }
        }

        //表单处理
        handleFormRequest(handleError)

        super.doActionFinish(handleError)
    }

    override fun release() {
        super.release()
        onGetTextResult = null
    }

    //</editor-fold desc="周期回调方法">

    /**[getText]动作获取到的文本列表*/
    fun handleGetTextResult(formKey: String?, textList: List<CharSequence>?) {

        if (textList != null) {
            if (getTextList == null) {
                getTextList = mutableListOf()
            }

            getTextList?.clear()
            getTextList?.addAll(textList)
        }

        onGetTextResult?.invoke(formKey, textList)
    }

    //记录指令
    fun _addActionName(actionName: String) {
        accessibilityInterceptor?._actionControl?.addActionName(actionName)
    }

    //获取参数对应的包名
    fun _targetPackageName(): String? {
        val actionPackageName = actionBean?.check?.packageName?.split(";")?.firstOrNull()
        if (actionPackageName.isNullOrEmpty()) {
            return accessibilityInterceptor?.filterPackageNameList?.firstOrNull()
        }
        return actionPackageName
    }

    /**
     * [Pair] 第一个值, 表示执行是否成功, 第二个值表示是否需要跳过后续的[handle]
     * */
    open fun handleAction(
        service: BaseAccessibilityService,
        parseResult: ParseResult,
        onGetTextResult: (String?, List<CharSequence>?) -> Unit
    ): HandleResult {

        val constraintBean: ConstraintBean = parseResult.constraint
        val nodeList: List<AccessibilityNodeInfoCompat> =
            parseResult.resultHandleNodeList() ?: emptyList() //parseResult.nodeList

        //需要执行的动作
        val actionList: List<String>? = when {
            parseResult.notTextMatch -> constraintBean.noActionList
            parseResult.isHaveCondition() -> {
                if (parseResult.conditionNodeList?.isEmpty() == true) {
                    //筛选后, 节点为空
                    constraintBean.noActionList
                } else {
                    constraintBean.actionList
                }
            }
            /*未开启筛选条件*/
            else -> constraintBean.actionList
        }

        //获取到的文本列表
        var getTextResultList: MutableList<CharSequence>? = mutableListOf()
        var getTextFormKey: String? = null

        //需要返回的处理结果
        val handleResult = HandleResult()
        //此次执行完成后, 是否要跳过后面的执行
        handleResult.jumpNextHandle = constraintBean.jump

        //过滤需要处理的节点列表
        val handleNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
        if (parseResult.notTextMatch || constraintBean.handleNodeList.isListEmpty()) {
            handleNodeList.addAll(nodeList)
        } else {
            constraintBean.handleNodeList?.forEach {
                val index = when {
                    it == 666666 -> nextInt(0, nodeList.size)
                    it >= 0 -> it
                    else -> nodeList.size + it
                }

                nodeList.getOrNull(index)?.let { node ->
                    if (!handleNodeList.contains(node)) {
                        handleNodeList.add(node)
                    }
                }
            }
        }

        if (actionList.isListEmpty()) {
            val checkId = actionBean?.checkId ?: -1
            handleActionLog("(${if (checkId > 0) checkId else actionBean?.title})未指定指令!")
            handleResult.result = false //2020-09-06
        } else {

            //fun...
            fun runActionList(actionList: List<String>?) {
                actionList?.forEach { act ->
                    //操作执行提示
                    handleActionLog("即将执行(${actionBean?.checkId})[${act}]")

                    var isIntercept = false
                    for (cmd in cmdActionList) {
                        if (cmd.interceptAction(this, act)) {
                            if (cmd is GetTextAction) {
                                cmd.onGetTextAction = { key, list ->
                                    getTextFormKey = key
                                    if (list == null) {
                                        getTextResultList = null
                                    } else {
                                        getTextResultList?.addAll(list)
                                    }
                                }
                            } else if (cmd is BaseConstraintAction) {
                                cmd.onGetConstraintList = {
                                    parseResult.constraintList
                                }
                            }

                            val runResult = cmd.runAction(
                                this,
                                service,
                                constraintBean,
                                handleNodeList,
                                handleResult
                            )

                            if (runResult) {
                                _addActionName(if (act.isEmpty()) ActionControl.ACTION_randomization else act)
                            }

                            handleResult.result = runResult || handleResult.result
                            isIntercept = true
                            break
                        }
                    }

                    if (!isIntercept) {
                        //未识别的act
                        handleResult.result = true
                        handleActionLog("未识别的指令[$act]:true")
                    }
                }
            }

            runActionList(actionList)

            if (!handleResult.result) {
                //actionList执行失败了
                if (constraintBean.notActionList != null) {
                    runActionList(constraintBean.notActionList)
                }
            }
        }

        if (getTextResultList?.isNotEmpty() == true) {
            onGetTextResult(getTextFormKey, getTextResultList)
        }

        if (constraintBean.jumpOnSuccess) {
            if (handleResult.result) {
                //执行成功, 跳过后续handle处理
                handleResult.jumpNextHandle = true
            }
        }

        if (constraintBean.ignore) {
            //如果忽略了约束
            handleResult.result = false

            //2020-8-26 4.1.2 移除此判断
//            if (constraintBean.enable) {
//                //不进行jumpNext操作
//                handleResult.jumpNextHandle = false
//            }
        } else {
            if (!handleResult.result) {
                //执行失败, 不进行jumpNext操作
                handleResult.jumpNextHandle = false
            }
        }

        //handleActionLog("ignore:${constraintBean.ignore} result:${handleResult.result} jump:${handleResult.jumpNextHandle} finish:${handleResult.finish}")

        return handleResult
    }

    /**一些处理日志*/
    open fun handleActionLog(charSequence: CharSequence) {
        actionLog?.log(charSequence)
    }

    /** 从参数中, 解析设置的点位信息. 通常用于手势坐标. 手势坐标, 尽量使用 屏幕宽高用来参考计算
     * [move:10,10-100,100]
     * [fling:10,10-100,100]
     * */
    fun parsePoint(
        arg: String?,
        refWidth: Int = _screenWidth,
        refHeight: Int = _screenHeight
    ): List<PointF> {
        val screenWidth: Int = _screenWidth
        val screenHeight: Int = _screenHeight

        val fX: Float = screenWidth * 1 / 3f + nextInt(5, 10)
        val tX: Float = screenWidth * 2 / 3f + nextInt(5, 10)
        val fY: Float = screenHeight * 3 / 5f - nextInt(5, 10)
        val tY: Float = screenHeight * 2 / 5f + nextInt(5, 10)

        val p1 = PointF(fX, fY)
        val p2 = PointF(tX, tY)

        try {
            arg?.apply {
                (if (this.contains("~")) split("~") else split("-")).apply {
                    //p1
                    getOrNull(0)?.toPointF(
                        refWidth,
                        refHeight
                    )?.apply {
                        p1.set(this)
                    }

                    //p2
                    getOrNull(1)?.toPointF(
                        refWidth,
                        refHeight
                    )?.apply {
                        p2.set(this)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf(p1, p2)
    }

    /**获取需要输入的文本,需要复制的文本*/
    fun getInputText(constraintBean: ConstraintBean): String? {
        val inputList: List<String?>? =
            getWordTextList(constraintBean.wordInputIndexList, constraintBean.inputList)
        val text = if (inputList.isListEmpty()) {
            //随机产生文本
            randomString()
        } else {
            inputList!!.getOrNull(nextInt(0, inputList.size))
        }
        return text
    }

    /**获取[textList]*/
    fun getTextList(constraintBean: ConstraintBean): List<String?>? {
        return getWordTextList(constraintBean.wordTextIndexList, constraintBean.textList)
    }

    /**根据表达式, 从[com.angcyo.core.component.accessibility.parse.TaskBean.wordList]中获取文本*/
    fun getWordTextList(
        wordIndexList: List<String>?,
        defaultTextList: List<String?>?
    ): List<String?>? {
        val resultList: List<String?>? = if (wordIndexList != null) {
            onGetWordTextListAction?.invoke(wordIndexList)
        } else {
            defaultTextList
        }
        return resultList ?: defaultTextList
    }

    /**
     * [wordIndex]
     * 支持表达式:
     * $N $0将会替换为[wordList]索引为0的值.
     * $-2 表示倒数第二个
     * 1-4 取索引为[1-4]的值
     * 0--1 取索引为[0-倒数第1个]的值
     * -1 取倒数第1个的值
     * */
    fun getTextFromWord(wordIndex: String?, defaultText: String? = wordIndex): String? {
        if (wordIndex.isNullOrEmpty()) {
            return defaultText
        }
        return getWordTextList(listOf(wordIndex), listOf(defaultText))?.firstOrNull()
    }

    /**表单请求*/
    fun handleFormRequest(error: ActionException?) {
        actionBean?.form?.let {
            var interceptor = accessibilityInterceptor
            //指定了表单处理
            it.request(result = { data, error ->
                if (error != null) {
                    //接口请求失败,中断流程
                    toastQQ(error.message)
                    interceptor?.actionError(this, ActionException(error.message))
                    interceptor = null
                }
            }) { map ->

                //打包所有之前也获取到的数据
                if (interceptor is AutoParseInterceptor) {
                    (interceptor as AutoParseInterceptor).handleFormParams(map)
                } else {
                    //如果配置了[getText]的[formKey]
                    actionBean?.formKey?.let { map[it] = getTextList?.firstOrNull() ?: "" }
                }

                //action执行结果, 执行成功发送 200
                if (error == null) {
                    map[FormBean.KEY_MSG] = "${actionBean?.title} 执行完成."
                    map[FormBean.KEY_DATA] = "${getTextList?.firstOrNull()}"
                } else {
                    map[FormBean.KEY_MSG] = "${actionBean?.title},${error.message ?: ""},执行失败!"
                }

                //错误码绑定
                map.bindErrorCode(error)

                //额外配置
                onConfigParams?.apply {
                    invoke(map)
                }
            }
        }
    }

    fun getGestureStartTime(time: String?) = time?.toLongOrNull() ?: getGestureStartTime(
        DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME
    )

    /**获取手势执行的开始时间数据*/
    fun getGestureStartTime(def: Long = DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME): Long {
        val lastMethodAction = accessibilityInterceptor?._actionControl?.lastMethodAction()
        return if (lastMethodAction?.contains(ConstraintBean.ACTION_HIDE_WINDOW) == true) {
            //如果之前触发了[ACTION_HIDE_WINDOW]那么下一次手势执行的时间需要延迟一点
            240L
        } else {
            def
        }
    }
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

fun String.toPointF(width: Int = _screenWidth, height: Int = _screenHeight): PointF {
    val p = PointF()
    var x = 0f
    var y = 0f

    split(",").apply {
        x = getOrNull(0)?.toFloatOrNull() ?: x
        y = getOrNull(1)?.toFloatOrNull() ?: y
    }

    p.x = x.toPointF(width)
    p.y = y.toPointF(height)

    return p
}

fun Float.toPointF(ref: Int): Float {
    return if (this <= 1f) {
        ref * this
    } else {
        this * dp
    }
}

/**使用指定的分隔符[split]分割, 获取第[index]个的数据*/
fun String.arg(index: Int = 0, split: String = ":") = split(split).getOrNull(index)

fun AutoParseAction.firstPackageName(): String? {
    val checkPackageName = actionBean?.check?.packageName?.arg(split = ";")
    return if (checkPackageName.isNullOrEmpty()) {
        accessibilityInterceptor?.filterPackageNameList?.firstOrNull()
    } else {
        checkPackageName
    }
}