package com.angcyo.core.component.accessibility.action

import android.graphics.PointF
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ActionBean
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.isListEmpty
import com.angcyo.library.ex.randomGet
import com.angcyo.library.ex.randomString
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

    /**需要执行的[Action]描述*/
    var actionBean: ActionBean? = null

    /**解析核心*/
    var autoParse: AutoParse = AutoParse()

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
        val constraintList: List<ConstraintBean>? = actionBean?.event
        if (constraintList == null) {
            doActionFinish(ActionException("eventConstraint is null."))
            return false
        }
        return autoParse.parse(service, nodeList, constraintList)
    }

    override fun doAction(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super.doAction(service, event, nodeList)
        val constraintList: List<ConstraintBean>? = actionBean?.handle
        if (constraintList == null) {
            doActionFinish(ActionException("handleConstraint is null."))
        } else {
            //解析拿到对应的node

            val randomHandle = actionBean?.randomHandle == true
            val handleConstraintList: List<ConstraintBean> = if (randomHandle) {
                //随机获取处理约束
                constraintList.randomGet(1)
            } else {
                constraintList
            }

            //执行对应的action操作
            var result = false

            autoParse.parse(service, nodeList, handleConstraintList) {

                for (pair in it) {

                    //执行action
                    val handleAction: Pair<Boolean, Boolean> =
                        handleAction(service, pair.first, pair.second) {
                            onGetTextResult?.invoke(it)
                        }

                    //执行结果
                    result = result || handleAction.first

                    //是否跳过后续action
                    if (handleAction.second) {
                        break
                    }
                }

                //判断是否执行成功
                if (result) {
                    //完成
                    doActionFinish()
                }
            }

            if (!result) {
                //未完成
                var actionMaxCount: Int = actionBean?.actionMaxCount ?: -1
                if (actionMaxCount > 0) {

                    if (randomHandle) {
                        actionMaxCount =
                            (actionMaxCount + actionMaxCount * nextInt(0, 100) / 100f).roundToInt()
                    }

                    if (actionDoCount >= actionMaxCount) {
                        doActionFinish()
                    }
                }
            }
        }
    }

    override fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        val constraintList: List<ConstraintBean>? = actionBean?.back

        if (constraintList != null) {
            val filterPackageNameList = action.accessibilityInterceptor?.filterPackageNameList

            //执行操作
            fun handle(): Boolean {
                var result = false
                autoParse.parse(service, nodeList, constraintList) {
                    it.forEach { pair ->
                        result = result || handleAction(service, pair.first, pair.second).first
                    }
                }
                return result
            }

            var result = false

            val eventConstraintList: List<ConstraintBean>? = actionBean?.event
            if (eventConstraintList == null) {
                //无界面约束匹配, 则不检查. 直接处理
                result = handle()
            } else {
                //匹配当前界面, 匹配成功后, 再处理
                if (autoParse.parse(service, nodeList, eventConstraintList)) {
                    //匹配成功
                    result = handle()
                }
            }
            return result
        }
        return super.doActionWidth(action, service, event, nodeList)
    }

    open fun handleAction(
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        nodeList: List<AccessibilityNodeInfoCompat>,
        onGetTextResult: (List<CharSequence>) -> Unit = {}
    ): Pair<Boolean, Boolean> {

        //需要执行的动作
        val actionList: List<String>? = constraintBean.actionList

        //获取到的文件列表
        val getTextList: MutableList<CharSequence> = mutableListOf()

        //此次执行, 返回结果的结果
        var result = false

        //此次执行成功后, 是否要跳过后面的执行
        var jump: Boolean = constraintBean.jump

        if (actionList.isListEmpty()) {
            result = nodeList.clickAll {
                log(buildString {
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
                        log("随机操作[${this.second}]:$result")
                    }
                } else if (act == ConstraintBean.ACTION_FINISH) {
                    //直接完成操作
                    result = true
                    jump = true
                } else {
                    val screenWidth: Int = _screenWidth
                    val screenHeight: Int = _screenHeight

                    val fX: Float = screenWidth * 1 / 3f + nextInt(5, 10)
                    val tX: Float = screenWidth * 2 / 3f + nextInt(5, 10)
                    val fY: Float = screenHeight * 3 / 5f - nextInt(5, 10)
                    val tY: Float = screenHeight * 2 / 5f + nextInt(5, 10)

                    val p1 = PointF(fX, fY)
                    val p2 = PointF(tX, tY)

                    var action: String? = null
                    var point: String? = null
                    try {
                        //解析2个点的坐标
                        act.split(":").apply {
                            action = getOrNull(0)
                            point = getOrNull(1)
                            point?.apply {
                                this.split("-").apply {
                                    getOrNull(0)?.toPointF(
                                        autoParse._rootNodeRect.width(),
                                        autoParse._rootNodeRect.height()
                                    )?.apply {
                                        p1.set(this)
                                    }
                                    getOrNull(1)?.toPointF(
                                        autoParse._rootNodeRect.width(),
                                        autoParse._rootNodeRect.height()
                                    )?.apply {
                                        p2.set(this)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    //执行对应操作
                    result = result || when (action) {
                        ConstraintBean.ACTION_CLICK -> {
                            var value = false
                            nodeList.forEach {
                                value = value || it.getClickParent()?.click() ?: false
                            }
                            log("点击节点[${nodeList.firstOrNull()?.text()}]:$value")
                            value
                        }
                        ConstraintBean.ACTION_CLICK2 -> {
                            var value = false
                            nodeList.forEach {
                                val bound = it.bounds()
                                value = value || service.gesture.double(
                                    bound.centerX().toFloat(),
                                    bound.centerY().toFloat(),
                                    null
                                )
                            }
                            log("双击节点区域[${nodeList.firstOrNull()?.bounds()}]:$value")
                            value
                        }
                        ConstraintBean.ACTION_LONG_CLICK -> {
                            var value = false
                            nodeList.forEach {
                                value = value || it.getLongClickParent()?.longClick() ?: false
                            }
                            log("长按节点[${nodeList.firstOrNull()}]:$value")
                            value
                        }
                        ConstraintBean.ACTION_DOUBLE -> service.gesture.double(p1.x, p1.y, null)
                            .apply {
                                log("双击[$p1]:$this")
                            }
                        ConstraintBean.ACTION_MOVE -> service.gesture.move(
                            p1.x,
                            p1.y,
                            p2.x,
                            p2.y,
                            null
                        ).apply {
                            log("move[$p1 $p2]:$this")
                        }
                        ConstraintBean.ACTION_FLING -> service.gesture.fling(
                            p1.x,
                            p1.y,
                            p2.x,
                            p2.y,
                            null
                        ).apply {
                            log("fling[$p1 $p2]:$this")
                        }
                        ConstraintBean.ACTION_BACK -> service.back().apply {
                            log("返回:$this")
                        }
                        ConstraintBean.ACTION_GET_TEXT -> {
                            nodeList.forEach {
                                it.text()?.apply {
                                    getTextList.add(this)
                                }
                            }
                            log("获取文本[$getTextList]:${getTextList.isNotEmpty()}")
                            getTextList.isNotEmpty()
                        }
                        ConstraintBean.ACTION_SET_TEXT -> {
                            var value = false

                            //执行set text时的文本
                            val comments = constraintBean.commentList
                            val text = if (comments.isListEmpty()) {
                                //随机产生文本
                                randomString()
                            } else {
                                comments!!.getOrNull(nextInt(0, comments.lastIndex))
                            }

                            nodeList.forEach {
                                value = value || it.setNodeText(text)
                            }
                            log("设置文本[$text]:$value")
                            value
                        }
                        ConstraintBean.ACTION_TOUCH -> service.gesture.click(p1.x, p1.y).apply {
                            log("touch[$p1]:$this")
                        }
                        ConstraintBean.ACTION_RANDOM -> service.gesture.randomization().run {
                            log("随机操作[$this]:$this")
                            first
                        }
                        else -> service.gesture.click().apply {
                            log("默认点击:$this")
                        }
                    }
                }
            }
        }

        if (getTextList.isNotEmpty()) {
            onGetTextResult(getTextList)
        }

        if (constraintBean.ignore) {
            //如果忽略了约束, 则不进行jump操作
            result = false
            jump = false
        } else {
            if (!result) {
                //执行失败, 不进行jump操作
                jump = false
            }
        }

        return result to jump
    }

    open fun log(charSequence: CharSequence) {
        onLogPrint?.invoke(charSequence)
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

