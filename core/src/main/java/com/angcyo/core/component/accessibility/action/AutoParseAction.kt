package com.angcyo.core.component.accessibility.action

import android.graphics.PointF
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.parse.ParseBean
import com.angcyo.core.component.accessibility.parse.isEmpty
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.dp
import kotlin.random.Random

/**
 * 智能识别的[Action], 通过配置一些关键字, 快速创建对应的[Action]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParseAction : BaseAccessibilityAction() {

    /**是否需要当前事件[checkEvent]解析时的关键数据*/
    var eventBean: ParseBean? = null

    /**目中目标界面后[doAction]解析时的关键数据*/
    var handleBean: ParseBean? = null

    /**当[Action]被作为回退处理时[doActionWidth]解析时的关键数据*/
    var backBean: ParseBean? = null

    /**日志输出*/
    var onLog: ((CharSequence) -> Unit)? = null

    /**如果是获取文本的任务, 那么多获取到文本时, 触发的回调*/
    var onGetTextResult: ((List<CharSequence>) -> Unit)? = null

    /**解析核心*/
    var autoParse: AutoParse = AutoParse()

    override fun doActionFinish(error: ActionException?) {
        onLog = null
        onGetTextResult = null
        super.doActionFinish(error)
    }

    override fun checkEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        val params = eventBean
        if (params.isEmpty()) {
            doActionFinish(ActionException("eventParams is null."))
            return false
        }
        return params?.run {
            autoParse.parse(service, this)
        } ?: super.checkEvent(service, event)
    }

    override fun doAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        super.doAction(service, event)
        val params = handleBean
        if (params.isEmpty()) {
            doActionFinish(ActionException("clickParams is null."))
        } else {

            //解析拿到对应的node
            autoParse.parse(service, params!!) {

                //执行对应的action操作
                var result = false
                it.forEach {
                    result = result || handleAction(service, it.first, it.second) {
                        onGetTextResult?.invoke(it)
                    }
                }

                //判断是否执行成功
                if (result) {
                    //完成
                    doActionFinish()
                } else {
                    //未完成
                }
            }
        }
    }

    override fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        val params = backBean
        if (!params.isEmpty()) {
            var result = false
            if (eventBean == null) {
                //无界面约束匹配, 则不检查. 直接处理
            } else {
                //匹配当前界面, 匹配成功后, 再处理
                if (autoParse.parse(service, eventBean!!)) {
                    //匹配成功
                    autoParse.parse(service, params!!) {
                        it.forEach {
                            result = result || handleAction(service, it.first, it.second)
                        }
                    }
                }
            }
            return result
        }
        return super.doActionWidth(action, service, event)
    }

    open fun handleAction(
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        nodeList: List<AccessibilityNodeInfoCompat>,
        onGetTextResult: (List<CharSequence>) -> Unit = {}
    ): Boolean {

        val textList = mutableListOf<CharSequence>()

        return constraintBean.action?.run {
            var result = false
            this.forEach {
                if (it.isEmpty()) {
                    //随机操作
                    service.gesture.randomization().apply {
                        result = result || first
                        log("随机操作[$this]:$result")
                    }
                } else {
                    val screenWidth = _screenWidth
                    val screenHeight = _screenHeight

                    val fX = screenWidth * 1 / 3f + Random.nextInt(5, 10)
                    val tX = screenWidth * 2 / 3f + Random.nextInt(5, 10)
                    val fY = screenHeight * 3 / 5f - Random.nextInt(5, 10)
                    val tY = screenHeight * 2 / 5f + Random.nextInt(5, 10)

                    val p1 = PointF(fX, fY)
                    val p2 = PointF(tX, tY)

                    var action: String? = null
                    //执行set text时的文本
                    var text: String? = null
                    try {
                        //解析2个点的坐标
                        it.split(":").apply {
                            action = getOrNull(0)
                            text = getOrNull(1)
                            text?.apply {
                                this.split("-").apply {
                                    getOrNull(0)?.toPointF()?.apply {
                                        p1.set(this)
                                    }
                                    getOrNull(1)?.toPointF()?.apply {
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
                                    textList.add(this)
                                }
                            }
                            log("获取文本[$textList]:${textList.isNotEmpty()}")
                            textList.isNotEmpty()
                        }
                        ConstraintBean.ACTION_SET_TEXT -> {
                            var value = false
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

            if (textList.isNotEmpty()) {
                onGetTextResult(textList)
            }

            if (constraintBean.ignore) {
                false
            } else {
                result
            }
        } ?: nodeList.clickAll {
            log(buildString {
                append("点击[")
                it.logText(this)
                append("]")
            })
        }
    }

    open fun log(charSequence: CharSequence) {
        onLog?.invoke(charSequence)
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

fun String.toPointF(): PointF {
    val p = PointF()
    var x = 0f
    var y = 0f

    split(",").apply {
        x = getOrNull(0)?.toFloatOrNull() ?: x
        y = getOrNull(1)?.toFloatOrNull() ?: y
    }

    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    if (x <= 1f) {
        p.x = screenWidth * x
    } else {
        p.x = x * dp
    }

    if (y <= 1f) {
        p.y = screenHeight * y
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

