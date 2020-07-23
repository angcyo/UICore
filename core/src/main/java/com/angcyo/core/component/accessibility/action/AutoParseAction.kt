package com.angcyo.core.component.accessibility.action

import android.app.Instrumentation
import android.graphics.PointF
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ActionBean
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.*
import kotlin.math.roundToInt
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
    var onLogPrint: ((CharSequence) -> Unit)? = null

    /**如果是获取文本的任务, 那么多获取到文本时, 触发的回调*/
    var onGetTextResult: ((List<CharSequence>) -> Unit)? = null

    /**根据给定的[wordInputIndexList] [wordTextIndexList]返回对应的文本信息*/
    var onGetWordTextListAction: ((List<String>) -> List<String>?)? = null

    /**需要执行的[Action]描述*/
    var actionBean: ActionBean? = null

    /**解析核心*/
    var autoParser: AutoParser = AutoParser()

    /**获取到的文本, 临时存储*/
    var getTextList: MutableList<CharSequence>? = null

    override fun doActionFinish(error: ActionException?) {
        onLogPrint = null
        onGetTextResult = null
        super.doActionFinish(error)
    }

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
        return autoParser.parse(service, this, nodeList, constraintList)
    }

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
                val handleResult: Pair<Boolean, Boolean> =
                    handleAction(service, pair.first, pair.second)

                //执行结果
                result = result || handleResult.first

                //是否跳过后续action
                if (handleResult.second) {
                    break
                }
            }
        }

        return result
    }

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
                    val handleResult: Pair<Boolean, Boolean> =
                        handleAction(service, pair.first, pair.second) {
                            handleGetTextResult(it)
                        }

                    //执行结果
                    result = result || handleResult.first

                    //是否跳过后续action
                    if (handleResult.second) {
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
            var actionMaxCount: Int = actionBean?.actionMaxCount ?: -1
            if (actionMaxCount > 0) {

                if (handleType == ActionBean.HANDLE_TYPE_RANDOM) {
                    //随机[actionMaxCount]
                    actionMaxCount =
                        (actionMaxCount + actionMaxCount * nextInt(0, 100) / 100f).roundToInt()
                }

                if (doActionCount.count >= actionMaxCount) {
                    doActionFinish()
                }
            }
        }
    }

    /**[getText]动作获取到的文本列表*/
    fun handleGetTextResult(textList: List<CharSequence>) {
        if (getTextList == null) {
            getTextList = mutableListOf()
        }

        getTextList?.clear()
        getTextList?.addAll(textList)

        onGetTextResult?.invoke(textList)
    }

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
                        result = result || handleAction(service, pair.first, pair.second).first
                    }
                }
                return result
            }

            var result = false

            val eventConstraintList: List<ConstraintBean>? = actionBean?.check?.back
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

    /**
     * [Pair] 第一个值, 表示执行是否成功, 第二个值表示是否需要跳过后续的[handle]
     * */
    open fun handleAction(
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        nodeList: List<AccessibilityNodeInfoCompat>,
        onGetTextResult: (List<CharSequence>) -> Unit = {}
    ): Pair<Boolean, Boolean> {

        //需要执行的动作
        val actionList: List<String>? = constraintBean.actionList

        //获取到的文件列表
        val getTextResultList: MutableList<CharSequence> = mutableListOf()

        //此次执行, 返回结果的结果
        var result = false

        //此次执行成功后, 是否要跳过后面的执行
        var jumpNextHandleAction: Boolean = constraintBean.jump

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
            result = handleNodeList.clickAll {
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
                        result = result || first
                        handleActionLog("随机操作[${this.second}]:$result")
                    }
                } else if (act == ConstraintBean.ACTION_FINISH) {
                    //直接完成操作
                    result = true
                    jumpNextHandleAction = true
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

                            parsePoint(arg).let {
                                p1.set(it[0])
                                p2.set(it[1])
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    //操作执行提示
                    handleActionLog("即将执行[${action}${if (arg == null) "" else ":${arg}"}]")

                    //执行对应操作
                    result = result || when (action) {
                        ConstraintBean.ACTION_CLICK -> {
                            var value = false
                            handleNodeList.forEach {
                                value = value || it.getClickParent()?.click() ?: false
                            }
                            handleActionLog("点击节点[${handleNodeList.firstOrNull()?.text()}]:$value")
                            value
                        }
                        ConstraintBean.ACTION_CLICK2 -> {
                            var value = false
                            handleNodeList.forEach {
                                val bound = it.bounds()
                                value = value || service.gesture.double(
                                    bound.centerX().toFloat(),
                                    bound.centerY().toFloat(),
                                    null
                                )
                            }
                            handleActionLog(
                                "双击节点区域[${handleNodeList.firstOrNull()?.bounds()}]:$value"
                            )
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
                            handleNodeList.forEach {
                                it.text()?.apply {
                                    getTextResultList.add(this)
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
                                actionBean?.check?.packageName
                                    ?: accessibilityInterceptor?.filterPackageNameList?.firstOrNull()
                            } else if (arg == "main") {
                                service.packageName
                            } else {
                                arg
                            }
                            targetPackageName?.openApp() != null
                        }
                        ConstraintBean.ACTION_COPY -> {
                            val text = getInputText(constraintBean)
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
            //如果忽略了约束, 则不进行jump操作
            result = false
            jumpNextHandleAction = false
        } else {
            if (!result) {
                //执行失败, 不进行jump操作
                jumpNextHandleAction = false
            }
        }

        return result to jumpNextHandleAction
    }

    /**一些处理日志*/
    open fun handleActionLog(charSequence: CharSequence) {
        L.d(charSequence)
        onLogPrint?.invoke(charSequence)
    }

    /** 从参数中, 解析设置的点位信息
     * [move:10,10-100,100]
     * [fling:10,10-100,100]
     * */
    fun parsePoint(arg: String?): List<PointF> {
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
                val refWidth = autoParser._rootNodeRect.width()
                val refHeight = autoParser._rootNodeRect.height()

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

fun dslAutoParseAction(action: AutoParseAction.() -> Unit): AutoParseAction {
    return AutoParseAction().apply(action)
}

//fun au() {
//
//    dslAutoParseAction {
//        backBean = dslParseParams {
//
//        }
//    }
//}

