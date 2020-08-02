package com.angcyo.core.component.accessibility.action

import android.app.Instrumentation
import android.graphics.PointF
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.*
import com.angcyo.http.rx.doBack
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
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

    /**日志输出*/
    var actionLog: ILogPrint? = ILogPrint()

    /**如果是获取文本的任务, 那么多获取到文本时, 触发的回调*/
    var onGetTextResult: ((List<CharSequence>) -> Unit)? = null

    /**根据给定的[wordInputIndexList] [wordTextIndexList]返回对应的文本信息*/
    var onGetWordTextListAction: ((List<String>) -> List<String>?)? = null

    /**请求表单时, 配置表单数据的回调*/
    var onConfigParams: ((params: HashMap<String, Any>) -> Unit)? = null

    /**需要执行的[Action]描述*/
    var actionBean: ActionBean? = null

    /**解析核心*/
    var autoParser: AutoParser = AutoParser()

    /**获取到的文本, 临时存储*/
    var getTextList: MutableList<CharSequence>? = null

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
        super.checkOtherEvent(service, event, nodeList)
        val constraintList: List<ConstraintBean> = actionBean?.check?.other ?: return false

        if (checkOtherEventCount.isMaxLimit()) {
            return false
        }

        //执行对应的action操作
        var result = false

        autoParser.parse(service, this, nodeList, constraintList) {
            for (pair in it) {

                //执行action
                val handleResult = handleAction(service, pair.first, pair.second)

                //执行结果
                result = result || handleResult.result

                //是否跳过后续action
                if (handleResult.jumpNextHandle) {
                    break
                }
            }
        }

        return result
    }

    /**当[checkEvent]通过时, 需要怎么处理*/
    override fun doAction(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super.doAction(service, event, nodeList)
        val constraintList: List<ConstraintBean>? = actionBean?.check?.handle
        if (constraintList == null) {
            doActionFinish(ActionException("handleConstraint is null."))
        } else {
            //解析拿到对应的node
            val handleType = actionBean?.handleType
            val handleConstraintList: List<ConstraintBean> = when (handleType) {
                ActionBean.HANDLE_TYPE_RANDOM -> constraintList.randomGet(1) //随机获取处理约束
                ActionBean.HANDLE_TYPE_ORDER -> {
                    val nextIndex: Int = ((doActionCount.count - 1) % constraintList.size).toInt()
                    val next = constraintList.getOrNull(nextIndex)

                    if (next == null) {
                        constraintList
                    } else {
                        listOf(next)
                    }
                } //顺序获取处理约束
                else -> constraintList //默认
            }

            //执行对应的action操作
            var result = false

            autoParser.parse(service, this, nodeList, handleConstraintList) {

                for (pair in it) {

                    //执行action
                    val handleResult = handleAction(service, pair.first, pair.second) {
                        handleGetTextResult(it)
                    }

                    //执行结果
                    result = result || handleResult.result

                    if (handleResult.finish) {
                        //直接完成
                        result = false
                        doActionFinish()
                        break
                    }

                    //是否跳过后续action
                    if (handleResult.jumpNextHandle) {
                        break
                    }
                }

                //判断是否执行成功
                if (result) {
                    //完成
                    doActionFinish()
                }
            }

            //是否需要强制执行完成
            var actionMaxCount: Long = actionBean?.actionMaxCount ?: -1L
            if (accessibilityInterceptor != null && actionMaxCount > 0) {

                if (handleType == ActionBean.HANDLE_TYPE_RANDOM) {
                    //随机[actionMaxCount]
                    actionMaxCount =
                        (actionMaxCount + actionMaxCount * nextInt(0, 100) / 100f).roundToLong()
                }

                if (doActionCount.count >= actionMaxCount) {
                    doActionFinish()
                }
            }
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

            //执行操作
            fun handle(): Boolean {
                var result = false
                autoParser.parse(service, this, nodeList, constraintList) {
                    it.forEach { pair ->
                        result = result || handleAction(service, pair.first, pair.second).result
                    }
                }
                return result
            }

            var result = false

            val eventConstraintList: List<ConstraintBean>? = actionBean?.check?.event
            if (eventConstraintList == null) {
                //无界面约束匹配, 则不检查. 直接处理
                result = handle()
            } else {
                //匹配当前界面, 匹配成功后, 再处理
                if (autoParser.parse(service, this, nodeList, eventConstraintList)) {
                    //匹配成功
                    result = handle()
                }
            }
            return result
        }
        return super.doActionWidth(action, service, event, nodeList)
    }

    /**[Action]处理完成*/
    override fun doActionFinish(error: ActionException?) {
        //表单处理
        handleFormRequest(error)

        actionLog = null
        onGetTextResult = null
        super.doActionFinish(error)
    }

    //</editor-fold desc="周期回调方法">

    /**[getText]动作获取到的文本列表*/
    fun handleGetTextResult(textList: List<CharSequence>) {
        if (getTextList == null) {
            getTextList = mutableListOf()
        }

        getTextList?.clear()
        getTextList?.addAll(textList)

        onGetTextResult?.invoke(textList)
    }

    /**
     * [Pair] 第一个值, 表示执行是否成功, 第二个值表示是否需要跳过后续的[handle]
     * */
    open fun handleAction(
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        nodeList: List<AccessibilityNodeInfoCompat>,
        onGetTextResult: (List<CharSequence>) -> Unit = {}
    ): HandleResult {

        //需要执行的动作
        val actionList: List<String>? = constraintBean.actionList

        //获取到的文件列表
        val getTextResultList: MutableList<CharSequence> = mutableListOf()

        //需要返回的处理结果
        val handleResult = HandleResult()
        //此次执行完成后, 是否要跳过后面的执行
        handleResult.jumpNextHandle = constraintBean.jump

        //过滤需要处理的节点列表
        val handleNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
        if (constraintBean.handleNodeList.isListEmpty()) {
            handleNodeList.addAll(nodeList)
        } else {
            constraintBean.handleNodeList?.forEach {
                if (it >= 0) {
                    nodeList.getOrNull(it)?.let { node ->
                        handleNodeList.add(node)
                    }
                } else {
                    nodeList.getOrNull(nodeList.size + it)?.let { node ->
                        handleNodeList.add(node)
                    }
                }
            }
        }

        if (actionList.isListEmpty()) {
            handleResult.result = handleNodeList.clickAll {
                handleActionLog(buildString {
                    append("点击[")
                    it.logText(this)
                    append("]")
                })
            }
        } else {
            actionList?.forEach { act ->
                if (act.isEmpty()) {
                    //随机操作
                    service.gesture.randomization().apply {
                        handleResult.result = handleResult.result || first
                        handleActionLog("随机操作[${this.second}]:${handleResult.result}")
                    }
                } else if (act == ConstraintBean.ACTION_FINISH) {
                    //直接完成操作
                    handleResult.result = true
                    handleResult.jumpNextHandle = true
                    handleResult.finish = true
                } else {
                    //需要执行的动作
                    var action: String? = null
                    //动作携带的参数
                    var arg: String? = null

                    //点位
                    val p1 = PointF()
                    val p2 = PointF()
                    try {
                        //解析2个点的坐标
                        val indexOf = act.indexOf(":", 0, true)

                        if (indexOf == -1) {
                            //未找到
                            action = act
                        } else {
                            //找到
                            action = act.substring(0, indexOf)
                            arg = act.substring(indexOf + 1, act.length)
                        }
                        parsePoint(arg).let {
                            p1.set(it[0])
                            p2.set(it[1])
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    //操作执行提示
                    handleActionLog("即将执行[${action}${if (arg == null) "" else ":${arg}"}]")

                    //执行对应操作
                    handleResult.result = handleResult.result || when (action) {
                        ConstraintBean.ACTION_CLICK -> {
                            //触发节点自带的click
                            var value = false
                            handleNodeList.forEach {
                                value = value || it.getClickParent()?.click() ?: false
                            }
                            handleActionLog("点击节点[${handleNodeList.firstOrNull()?.text()}]:$value")
                            value
                        }
                        ConstraintBean.ACTION_CLICK2, ConstraintBean.ACTION_CLICK3 -> {
                            //触发节点区域的手势双击
                            var value = false
                            val click = action == ConstraintBean.ACTION_CLICK3

                            var x = 0f
                            var y = 0f

                            handleNodeList.forEach {
                                val bound = it.bounds()

                                var specifyPoint: PointF? = null
                                if (!arg.isNullOrEmpty()) {
                                    parsePoint(arg, bound.width(), bound.height()).let {
                                        specifyPoint = it[0]
                                    }
                                }

                                if (specifyPoint == null) {
                                    x = bound.centerX().toFloat()
                                    y = bound.centerY().toFloat()
                                } else {
                                    x = specifyPoint!!.x + bound.left
                                    y = specifyPoint!!.y + bound.top
                                }

                                value = value || if (click) {
                                    service.gesture.click(x, y, null)
                                } else {
                                    service.gesture.double(x, y, null)
                                }
                            }
                            if (click) {
                                handleActionLog("点击节点区域[${x},${y}]:$value")
                            } else {
                                handleActionLog("双击节点区域[${x},${y}]:$value")
                            }
                            value
                        }
                        ConstraintBean.ACTION_LONG_CLICK -> {
                            var value = false
                            handleNodeList.forEach {
                                value = value || it.getLongClickParent()?.longClick() ?: false
                            }
                            handleActionLog("长按节点[${handleNodeList.firstOrNull()}]:$value")
                            value
                        }
                        ConstraintBean.ACTION_DOUBLE -> service.gesture.double(p1.x, p1.y, null)
                            .apply {
                                handleActionLog("双击[$p1]:$this")
                            }
                        ConstraintBean.ACTION_MOVE -> service.gesture.move(
                            p1.x,
                            p1.y,
                            p2.x,
                            p2.y,
                            null
                        ).apply {
                            handleActionLog("move[$p1 $p2]:$this")
                        }
                        ConstraintBean.ACTION_FLING -> service.gesture.fling(
                            p1.x,
                            p1.y,
                            p2.x,
                            p2.y,
                            null
                        ).apply {
                            handleActionLog("fling[$p1 $p2]:$this")
                        }
                        ConstraintBean.ACTION_BACK -> service.back().apply {
                            handleActionLog("返回:$this")
                        }
                        ConstraintBean.ACTION_HOME -> service.home().apply {
                            handleActionLog("回到桌面:$this")
                        }
                        ConstraintBean.ACTION_SCROLL_BACKWARD -> {
                            //如果滚动到头部了, 会滚动失败
                            var value = false
                            handleNodeList.forEach {
                                value = value || it.scrollBackward()
                            }
                            handleActionLog("向后滚动:$value")
                            value
                        }
                        ConstraintBean.ACTION_SCROLL_FORWARD -> {
                            //如果滚动到底了, 会滚动失败
                            var value = false
                            handleNodeList.forEach {
                                value = value || it.scrollForward()
                            }
                            handleActionLog("向前滚动:$value")
                            value
                        }
                        ConstraintBean.ACTION_GET_TEXT -> {
                            val textRegexList = constraintBean.getTextRegexList
                            handleNodeList.forEach {
                                it.text()?.also { text ->
                                    val resultTextList = mutableListOf<String>()

                                    if (textRegexList == null) {
                                        //未指定正则匹配规则
                                        resultTextList.add(text.toString())
                                    } else {
                                        textRegexList.forEach {
                                            text.patternList(it).let { list ->
                                                //正则匹配过滤后的文本列表
                                                if (list.isNotEmpty()) {
                                                    resultTextList.addAll(list)
                                                }
                                            }
                                        }

                                        //未匹配到正则时, 使用默认
                                        if (resultTextList.isEmpty()) {
                                            resultTextList.add(text.toString())
                                        }
                                    }

                                    //汇总所有文本
                                    getTextResultList.addAll(resultTextList)
                                }
                            }
                            handleActionLog("获取文本[$getTextResultList]:${getTextResultList.isNotEmpty()}")
                            getTextResultList.isNotEmpty()
                        }
                        ConstraintBean.ACTION_SET_TEXT -> {
                            var value = false

                            //执行set text时的文本
                            val text = getInputText(constraintBean)

                            handleNodeList.forEach {
                                value = value || it.setNodeText(text)
                            }

                            handleActionLog("设置文本[$text]:$value")
                            value
                        }
                        ConstraintBean.ACTION_TOUCH -> service.gesture.click(p1.x, p1.y).apply {
                            handleActionLog("touch[$p1]:$this")
                        }
                        ConstraintBean.ACTION_RANDOM -> service.gesture.randomization().run {
                            handleActionLog("随机操作[$this]:$this")
                            first
                        }
                        ConstraintBean.ACTION_START -> {
                            //启动应用程序

                            //包名
                            val targetPackageName = if (arg.isNullOrEmpty() || arg == "target") {
                                actionBean?.check?.packageName?.split(";")?.firstOrNull()
                                    ?: accessibilityInterceptor?.filterPackageNameList?.firstOrNull()
                            } else if (arg == "main") {
                                service.packageName
                            } else {
                                arg
                            }

                            var value = false
                            targetPackageName?.let {
                                value = service.openApp(
                                    it,
                                    flags = 0/*Intent.FLAG_ACTIVITY_SINGLE_TOP*/
                                ) != null

                                handleActionLog("启动程序:[$targetPackageName]:$value")
                            }

                            value
                        }
                        ConstraintBean.ACTION_COPY -> {
                            val text = arg ?: getInputText(constraintBean)
                            val value = text?.copy() == true
                            handleActionLog("复制文本[$text]:$value")
                            value
                        }
                        ConstraintBean.ACTION_KEY -> {
                            var value = false

                            val keyCode = arg?.toIntOrNull() ?: -1
                            val keyCodeStr = KeyEvent.keyCodeToString(keyCode)

                            if (keyCode > 0) {
                                doBack {
                                    try {
                                        val inst = Instrumentation()
                                        inst.sendKeyDownUpSync(keyCode)
                                    } catch (e: Exception) {
                                        AccessibilityHelper.log("Exception when sendKeyDownUpSync $keyCodeStr :$e")
                                        e.printStackTrace()
                                    }
                                }
                                value = true
                            }

                            handleActionLog("发送按键[$keyCodeStr]:$value")
                            value
                        }
                        ConstraintBean.ACTION_FOCUS -> {
                            var value = false
                            handleNodeList.forEach {
                                value = value || it.focus()
                            }
                            handleActionLog("设置焦点:$value")
                            value
                        }
                        ConstraintBean.ACTION_ERROR -> {
                            //直接失败操作
                            val value = false
                            handleResult.jumpNextHandle = true

                            //异常退出
                            val error = arg ?: "ACTION_ERROR"
                            doActionFinish(ActionException(error))

                            handleActionLog("强制异常退出[$error]:${handleResult.result}")
                            value
                        }
                        ConstraintBean.ACTION_JUMP -> {
                            //执行跳转指令
                            var value = false
                            val interceptor = accessibilityInterceptor

                            if (arg.isNullOrEmpty() || interceptor == null) {
                                //没有跳转参数,直接完成action
                                value = true
                                handleResult.finish = true
                            } else {
                                //[:1:3] [:-1] [:<2]前 [:>3]后 [:actionId]
                                val indexOf = arg.indexOf(":", 0, true)
                                val arg1: String?
                                val arg2: String?
                                if (indexOf == -1) {
                                    //未找到
                                    arg1 = arg
                                    arg2 = "$DEFAULT_JUMP_MAX_COUNT"
                                } else {
                                    //找到
                                    arg1 = arg.substring(0, indexOf)
                                    arg2 = arg.substring(indexOf + 1, arg.length)
                                }

                                val maxCount = arg2.toLongOrNull() ?: DEFAULT_JUMP_MAX_COUNT

                                jumpCount.maxCountLimit = maxCount

                                if (jumpCount.isMaxLimit()) {
                                    //超限后, 不跳转, 直接完成
                                    value = true
                                    handleResult.finish = true
                                } else {
                                    value = true
                                    arg1.toLongOrNull()?.let { targetIndex ->
                                        val size = interceptor.actionList.size
                                        if (targetIndex.absoluteValue in 0 until size) {
                                            //处理[:1] [:-1]的情况
                                            if (targetIndex > 0) {
                                                interceptor.actionIndex =
                                                    targetIndex.toInt()
                                            } else {
                                                interceptor.actionIndex =
                                                    (size + targetIndex).toInt()
                                            }
                                        } else {
                                            //寻找指定actionId
                                            interceptor.actionList.forEachIndexed { index, baseAccessibilityAction ->
                                                if (baseAccessibilityAction is AutoParseAction) {
                                                    if (baseAccessibilityAction.actionBean?.actionId == targetIndex) {
                                                        interceptor.actionIndex = index
                                                    }
                                                }
                                            }
                                        }
                                    }.elseNull {
                                        val num = arg1.substring(1, arg1.length).toIntOrNull() ?: 0
                                        if (arg1.startsWith("<")) {
                                            interceptor.actionIndex -= num
                                        } else if (arg1.startsWith(">")) {
                                            interceptor.actionIndex += num
                                        } else {
                                            value = false
                                            handleResult.finish = true
                                        }
                                    }
                                    jumpCount.doCount()
                                }
                            }

                            handleActionLog("跳转[$arg]:$value")
                            value
                        }
                        else -> service.gesture.click().apply {
                            handleActionLog("默认点击:$this")
                        }
                    }

                    //...end
                }
            }
        }

        if (getTextResultList.isNotEmpty()) {
            onGetTextResult(getTextResultList)
        }

        if (constraintBean.ignore) {
            //如果忽略了约束, 则不进行jumpNext操作
            handleResult.result = false
            handleResult.jumpNextHandle = false
        } else {
            if (!handleResult.result) {
                //执行失败, 不进行jumpNext操作
                handleResult.jumpNextHandle = false
            }
        }

        return handleResult
    }

    /**一些处理日志*/
    open fun handleActionLog(charSequence: CharSequence) {
        actionLog?.log(charSequence)
    }

    /** 从参数中, 解析设置的点位信息
     * [move:10,10-100,100]
     * [fling:10,10-100,100]
     * */
    fun parsePoint(
        arg: String?,
        refWidth: Int = autoParser._rootNodeRect.width(),
        refHeight: Int = autoParser._rootNodeRect.height()
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

                split("-").apply {

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
        val inputList: List<String>? = if (constraintBean.wordInputIndexList != null) {
            onGetWordTextListAction?.invoke(constraintBean.wordInputIndexList!!)
        } else {
            constraintBean.inputList
        }

        val text = if (inputList.isListEmpty()) {
            //随机产生文本
            randomString()
        } else {
            inputList!!.getOrNull(nextInt(0, inputList.size))
        }
        return text
    }

    /**获取[textList]*/
    fun getTextList(constraintBean: ConstraintBean): List<String>? {
        val inputList: List<String>? = if (constraintBean.wordTextIndexList != null) {
            onGetWordTextListAction?.invoke(constraintBean.wordTextIndexList!!)
        } else {
            constraintBean.textList
        }
        return inputList
    }

    /**表单请求*/
    fun handleFormRequest(error: ActionException?) {
        actionBean?.form?.let {
            //指定了表单处理
            it.request { map ->

                //如果配置了[getText]的[formKey]
                actionBean?.formKey?.let { map[it] = getTextList?.firstOrNull() ?: "" }

                //action执行结果, 执行成功发送 200
                if (error == null) {
                    map[FormBean.KEY_MSG] = "${actionBean?.title} 执行完成."
                    map[FormBean.KEY_DATA] = "${getTextList?.firstOrNull()}"
                } else {
                    map[FormBean.KEY_MSG] = "${actionBean?.title} 执行失败,${error.message}"
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

    if (x <= 1f) {
        p.x = width * x
    } else {
        p.x = x * dp
    }

    if (y <= 1f) {
        p.y = height * y
    } else {
        p.y = y * dp
    }
    return p
}