package com.angcyo.core.component.accessibility.action

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import androidx.collection.ArrayMap
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc.getBrotherNodeNext
import com.angcyo.acc.getBrotherNodePrev
import com.angcyo.acc.getParentOrChildNodeDown
import com.angcyo.acc.getParentOrChildNodeUp
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.*
import com.angcyo.http.base.default
import com.angcyo.http.base.toJson
import com.angcyo.library._contentHeight
import com.angcyo.library._contentWidth
import com.angcyo.library.app
import com.angcyo.library.ex.*
import com.angcyo.library.utils.getLongNum
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt

/**
 * 解析处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParser {

    companion object {

        /**需要被激活的[ConstraintId]临时存放*/
        val enableConstraintIdList = mutableListOf<Long>()

        fun enableConstraint(constraintId: Long, enable: Boolean = true) {
            if (enable) {
                if (!isHaveEnableConstraint(constraintId)) {
                    enableConstraintIdList.add(constraintId)
                }
            } else {
                if (isHaveEnableConstraint(constraintId)) {
                    enableConstraintIdList.remove(constraintId)
                }
            }
        }

        fun isHaveEnableConstraint(constraintId: Long) =
            enableConstraintIdList.contains(constraintId)

        /**
         * [textList]优先从[wordList]集合中取值.
         * 支持表达式:
         * $N $0将会替换为[wordList]索引为0的值.
         * $-2 表示倒数第二个
         * 1-4 取索引为[1-4]的值
         * 0--1 取索引为[0-倒数第1个]的值
         * -1 取倒数第1个的值
         * [originTextList] 数据源
         * [indexTextList] 包含匹配表达式的文本
         * */
        fun parseWordTextList(
            originTextList: List<String>?,
            indexTextList: List<String>
        ): List<String>? {
            if (originTextList.isListEmpty()) {
                return null
            }

            val originList: List<String> = originTextList!!
            val result = mutableListOf<String>()

            for (indexText in indexTextList) {
                if (!indexText.contains("$") &&
                    !indexText.contains("~") &&
                    !indexText.contains("-")
                ) {
                    //不符合规则的表达式
                    result.add(indexText)
                    continue
                }

                if (indexText.contains("$")) {
                    //找出所有的$N
                    val tList = indexText.patternList("\\$[-]?\\d+")
                    if (tList.isEmpty()) {
                        result.add(indexText)
                    } else {
                        //替换表达式后的文本
                        var target: String? = indexText
                        tList.forEach { exp ->
                            //exp like this: $-999 $999
                            val num = exp.getLongNum() ?: -1
                            val originText = originList.getOrNull2(num.toInt())
                            if (originText != null) {
                                target = target?.replace(exp, originText)
                            }
                        }
                        target?.also { result.add(it) }
                    }
                } else {
                    val num = indexText.toIntOrNull()

                    if (num == null) {
                        //不是一个单纯的数字, 匹配 [1-4] 范围
                        var indexOf = indexText.indexOf("~")
                        if (indexOf == -1) {
                            //未找到~号, 则使用-号
                            indexOf = indexText.indexOf("-")
                        }
                        if (indexOf != -1) {
                            val startIndex = indexText.substring(0, indexOf).toIntOrNull() ?: 0
                            val endIndex =
                                indexText.substring(indexOf + 1, indexText.length).toIntOrNull()
                                    ?: 0

                            val fist =
                                if (startIndex < 0) startIndex + originList.size else startIndex
                            val second =
                                if (endIndex < 0) endIndex + originList.size else endIndex

                            for (i in min(fist, second)..max(fist, second)) {
                                originList.getOrNull2(i)?.also {
                                    result.add(it)
                                }
                            }
                        }
                    } else {
                        // 匹配 1, -1 索引
                        originList.getOrNull2(num)?.also {
                            result.add(indexText)
                        }
                    }
                }
            }
            //返回
            return if (result.isEmpty()) {
                null
            } else {
                result
            }
        }

        /**[com.angcyo.core.component.accessibility.parse.ConstraintBean.ACTION_CLICK]
         * 判断自身和[parentCount]个parent内所有节点是否都满足状态*/
        fun matchNodeStateAndParent(
            node: AccessibilityNodeInfoCompat,
            state: String,
            parentCount: Int = 0
        ): Pair<Boolean, AccessibilityNodeInfoCompat> {
            var result: Pair<Boolean, AccessibilityNodeInfoCompat> = true to node
            if (matchNodeState(node, state)) {
                //自身已经满足条件
                if (parentCount > 0) {
                    var parent: AccessibilityNodeInfoCompat? = node
                    for (count in 1..parentCount) {
                        parent = parent?.parent
                        if (parent == null) {
                            break
                        }
                        val match = matchNodeState(parent, state)
                        if (!match) {
                            //有一个不匹配, 返回false, 并且返回根节点
                            result = false to node
                            break
                        }
                        //全部匹配, 还是返回根节点
                        result = match to node
                    }
                }
            } else {
                result = false to node
            }
            return result
        }

        /**
         * 判断自身和[parentCount]个parent内是否有满足指定状态的节点, 并返回
         * [first] 表示是否匹配成功
         * [second] 当前匹配的节点*/
        fun matchNodeStateOfParent(
            node: AccessibilityNodeInfoCompat,
            state: String,
            parentCount: Int = 0
        ): Pair<Boolean, AccessibilityNodeInfoCompat> {
            var result: Pair<Boolean, AccessibilityNodeInfoCompat> = false to node
            if (matchNodeState(node, state)) {
                //自身已经满足条件
                result = true to node
            } else if (parentCount > 0) {
                //自身不满足, 则看看指定的parent中, 是否有满足条件的
                var parent: AccessibilityNodeInfoCompat? = node
                for (count in 1..parentCount) {
                    parent = parent?.parent
                    if (parent == null) {
                        break
                    }

                    val match = matchNodeState(parent, state)
                    result = match to parent

                    if (match) {
                        //有一个匹配
                        break
                    }
                }
            }
            return result
        }

        /**只要有一个状态匹配, 即可*/
        fun matchNodeStateOr(node: AccessibilityNodeInfoCompat, stateList: List<String>): Boolean {
            stateList.forEach {
                if (matchNodeState(node, it)) {
                    return true
                }
            }
            return false
        }

        /**所有状态都匹配*/
        fun matchNodeStateAnd(node: AccessibilityNodeInfoCompat, stateList: List<String>): Boolean {
            stateList.forEach {
                if (!matchNodeState(node, it)) {
                    return false
                }
            }
            return true
        }

        /**匹配节点的状态*/
        fun matchNodeState(node: AccessibilityNodeInfoCompat, state: String): Boolean {
            var match = true
            when (state) {
                ConstraintBean.STATE_CLICKABLE -> {
                    //需要具备可以点击的状态
                    if (!node.isClickable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_NOT_CLICKABLE -> {
                    //需要具备不可以点击的状态
                    if (node.isClickable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_FOCUSABLE -> {
                    //需要具备可以获取焦点状态
                    if (!node.isFocusable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_NOT_FOCUSABLE -> {
                    //需要具备不可以获取焦点状态
                    if (node.isFocusable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_FOCUSED -> {
                    //需要具备焦点状态
                    if (!node.isFocused) {
                        match = false
                    }
                }
                ConstraintBean.STATE_UNFOCUSED -> {
                    //需要具备无焦点状态
                    if (node.isFocused) {
                        match = false
                    }
                }
                ConstraintBean.STATE_SELECTED -> {
                    //需要具备选中状态
                    if (!node.isSelected) {
                        match = false
                    }
                }
                ConstraintBean.STATE_UNSELECTED -> {
                    //需要具备不选中状态
                    if (node.isSelected) {
                        match = false
                    }
                }
                ConstraintBean.STATE_SCROLLABLE -> {
                    //需要具备可滚动状态
                    if (!node.isScrollable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_NOT_SCROLLABLE -> {
                    //需要具备不可滚动状态
                    if (node.isScrollable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_LONG_CLICKABLE -> {
                    //需要具备可以长按的状态
                    if (!node.isLongClickable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_NOT_LONG_CLICKABLE -> {
                    //需要具备不可以长按的状态
                    if (node.isLongClickable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_ENABLE -> {
                    //需要具备激活状态
                    if (!node.isEnabled) {
                        match = false
                    }
                }
                ConstraintBean.STATE_DISABLE -> {
                    //需要具备禁用状态
                    if (node.isEnabled) {
                        match = false
                    }
                }
                ConstraintBean.STATE_PASSWORD -> {
                    //需要具备密码状态
                    if (!node.isPassword) {
                        match = false
                    }
                }
                ConstraintBean.STATE_NOT_PASSWORD -> {
                    //需要具备非密码状态
                    if (node.isPassword) {
                        match = false
                    }
                }
                ConstraintBean.STATE_CHECKABLE -> {
                    if (!node.isCheckable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_NOT_CHECKABLE -> {
                    if (node.isCheckable) {
                        match = false
                    }
                }
                ConstraintBean.STATE_CHECKED -> {
                    if (!node.isChecked) {
                        match = false
                    }
                }
                ConstraintBean.STATE_UNCHECKED -> {
                    if (node.isChecked) {
                        match = false
                    }
                }
            }
            return match
        }


        /**验证[value]是否满足数学表达式*/
        fun compareStringNum(expression: String?, value: Float, ref: Float = 1f): Boolean {
            return MathParser.verifyValue(value, expression, ref)
        }

        /**将参数转换成对应的包名*/
        fun parseTargetPackageName(arg: String?, target: String?): String? {
            //包名
            return if (arg.isNullOrEmpty() || arg == "target") {
                target
            } else if (arg == "main") {
                app().packageName
            } else {
                arg
            }
        }

        /**
         * 获取状态中, 满足条件的节点
         * [com.angcyo.core.component.accessibility.parse.ConstraintBean.stateList]
         * [index] 未使用的参数, 支持负数
         * */
        fun getStateParentNode(
            stateList: List<String>?,
            node: AccessibilityNodeInfoCompat,
            index: Int = -1
        ): Pair<Boolean, AccessibilityNodeInfoCompat> {
            var result = true to node
            if (stateList != null && !stateList.isListEmpty()) {
                for (state in stateList) {

                    //所有节点的状态都要匹配
                    val allMatch = state.contains("*")

                    val stateString: String?
                    val parentCount: Int

                    state.replace("*", "").split(":").apply {
                        stateString = getOrNull(0)
                        parentCount = getOrNull(1)?.toIntOrNull() ?: 0
                    }

                    if (!stateString.isNullOrEmpty()) {
                        //需要状态约束
                        result = if (allMatch) {
                            matchNodeStateAndParent(node, stateString, parentCount)
                        } else {
                            matchNodeStateOfParent(node, stateString, parentCount)
                        }
                    }

                    if (!result.first) {
                        //有一个状态不匹配
                        result = false to node
                        break
                    }
                }

            }
            return result
        }
    }

    /**解析id时, 需要补全的id全路径包名*/
    var idPackageName: String? = null

    /**根节点在屏幕中的坐标*/
    var _rootNodeRect = Rect()

    val maxWidth: Int
        get() = _contentWidth

    val maxHeight: Int
        get() = _contentHeight

    /**返回当前界面, 是否包含[constraintList]约束的Node信息
     * [onTargetResult] 当找到目标时, 通过此方法回调目标给调用者. first:对应的约束, second:约束对应的node集合
     * */
    open fun parse(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        nodeList: List<AccessibilityNodeInfo>,
        constraintList: List<ConstraintBean>,
        includeDisableConstraint: Boolean = false,//是否需要包含被禁用的约束
        onTargetResult: ((List<ParseResult>) -> Unit)? = null //为空, 则只需要有一个满足条件,就break
    ): Boolean {

        val result: MutableList<ParseResult> = mutableListOf()

        //是否需要深度解析,否则只需要有一个满足条件,就break
        val deep = onTargetResult != null

        for (constraint in constraintList) {
            var enable: Boolean

            if (isHaveEnableConstraint(constraint.constraintId)) {
                constraint.enable = true
                enableConstraint(constraint.constraintId, false)
            }

            if (constraint.randomEnable) {
                //随机激活
                enable = nextBoolean()
            } else {
                if (includeDisableConstraint) {
                    enable = true
                } else {
                    enable = constraint.enable
                    if (!enable) {
                        enable =
                            constraint.actionList?.find { it.startsWith(ConstraintBean.ACTION_ENABLE) } != null
                    }
                }
            }

            if (enable) {
                //如果当前的[constraint]处于激活状态, 或者拥有激活自身的能力
                val parseResult = ParseResult(constraint, constraintList)

                //查找node
                findConstraintNode(service, autoParseAction, nodeList, parseResult)

                parseResult.resultHandleNodeList().apply {
                    if (this?.isNotEmpty() == true) {
                        //找到了节点
                        result.add(parseResult)
                    } else {
                        //没有找到节点
                    }
                }
            }

            if (!deep) {
                //不需要深度循环
                if (result.isNotEmpty()) {
                    onTargetResult?.invoke(result)
                    return true
                }
            }
        }

        if (result.isNotEmpty()) {
            //通过约束, 找到了目标
            onTargetResult?.invoke(result)
            return true
        }

        return false
    }

    /**查找满足约束的Node*/
    open fun findConstraintNode(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        nodeList: List<AccessibilityNodeInfo>,
        parseResult: ParseResult
    ) {
        val rootNodeInfo: AccessibilityNodeInfo = nodeList.mainNode() ?: return

        //存储一下跟node的矩形, 方便用于坐标比例计算
        rootNodeInfo.getBoundsInScreen(_rootNodeRect)

        //查找所有
        findConstraintNodeByRootNode(
            service,
            autoParseAction,
            rootNodeInfo,
            parseResult
        )
    }

    /**在指定的根节点[rootNodeInfo]下, 匹配节点*/
    open fun findConstraintNodeByRootNode(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        rootNodeInfo: AccessibilityNodeInfo,
        parseResult: ParseResult
    ) {
        //约束条件
        val constraintBean: ConstraintBean = parseResult.constraint

        //返回值
        val result: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

        //根节点
        val rootNodeWrap: AccessibilityNodeInfoCompat = rootNodeInfo.wrap()

        if (!ConfigParser.verifySystem(constraintBean.system)) {
            return
        }

        if (!ConfigParser.verifyAction(
                autoParseAction.accessibilityInterceptor,
                constraintBean.action
            )
        ) {
            return
        }

        //节点总数约束
        if (constraintBean.sizeCount != null) {
            val allNode = constraintBean.sizeCount?.contains("*") == true
            val sizeCountExp = constraintBean.sizeCount?.replaceFirst("*", "")

            var size = 0f
            var next = -1
            var allow = false
            rootNodeInfo.findNode(deep = constraintBean.deep) {
                next = -1
                if (allNode || it.childCount == 0) {
                    //空节点
                    size++

                    if (sizeCountExp?.startsWith("<") == true) {
                        allow = true
                        //如果是小于运算符, 则没有匹配到时,才退出
                        if (!compareStringNum(sizeCountExp, size)) {
                            allow = false
                            next = -2
                        }
                    } else if (compareStringNum(sizeCountExp, size)) {
                        //其他运算符, 匹配到时, 则退出
                        allow = true
                        next = -2
                    }
                }
                next
            }

            if (!allow) {
                //不满足节点总数约束条件
                return
            }
        }

        //节点bound约束
        if (constraintBean.boundRect != null) {
            if (!matchClassAndRect(rootNodeWrap, rect = constraintBean.boundRect)) {
                //不满足bound约束条件
                return
            }
        }

        //不包含文本约束
        val notTextList = autoParseAction.getWordTextList(
            constraintBean.wordNotTextIndexList,
            constraintBean.notTextList
        )
        if (notTextList != null) {
            //只要满足不包含的文本即可
            val packageName: String = idPackageName ?: service.packageName

            var haveTextNode = false
            for (index: Int in notTextList.indices) {
                val text = notTextList[index] ?: ""
                //完整id 是需要包含包名的
                val isIdText: Boolean = constraintBean.notIdList?.getOrNull(index) == 1
                val subText: String? = if (isIdText) packageName.id(text) else text

                val findList = rootNodeInfo.findNode(deep = constraintBean.deep) { nodeInfoCompat ->
                    var findNode = -1
                    if (isIdText) {
                        //id 全等匹配
                        val idName = nodeInfoCompat.viewIdName()
                        findNode = if (subText == idName) {
                            1
                        } else {
                            -1
                        }
                    } else {
                        //文本包含匹配
                        findNode = if (nodeInfoCompat.haveText(subText ?: "")) {
                            1
                        } else {
                            -1
                        }
                    }
                    findNode
                }

                if (findList.isNotEmpty()) {
                    //不需要包含的文本, 却找到了对应文本的节点
                    haveTextNode = true
                    break
                }
            }

            if (haveTextNode) {
                //匹配失败, 则走正常流程
            } else {
                //特殊流程
                result.add(rootNodeWrap)
                parseResult.notTextMatch = true
                parseResult.nodeList.addAll(result)
                return
            }
        }

        /*------------------------------开始匹配条件-------------------------------------*/

        if (constraintBean.isConstraintEmpty()) {
            //空约束返回[rootNodeInfo]
            result.add(rootNodeWrap)
        } else {

            //需要匹配的文本
            val textList: List<String?>? = autoParseAction.getTextList(constraintBean)

            if (textList == null) {
                //不约束文本, 单纯约束其他规则
                if (constraintBean.isOnlyPathConstraint()) {
                    //单纯的path约束
                    parseConstraintPath(
                        constraintBean,
                        constraintBean.pathList?.getOrNull(0),
                        rootNodeWrap,
                        result
                    )
                } else {
                    //其他约束组合
                    rootNodeInfo.findNode(result, deep = constraintBean.deep) { nodeInfoCompat ->
                        if (matchClassAndRectAndState(constraintBean, nodeInfoCompat, 0)) {
                            val node = getStateParentNode(
                                constraintBean.stateList,
                                nodeInfoCompat,
                                0
                            ).second
                            if (!constraintBean.pathList.isNullOrEmpty()) {
                                parseConstraintPath(
                                    constraintBean,
                                    constraintBean.pathList?.getOrNull(0),
                                    node,
                                    result
                                )
                                -1
                            } else {
                                if (node != nodeInfoCompat) {
                                    //查找的 nodeInfoCompat 被替换成 node
                                    if (!result.contains(node)) {
                                        result.add(node)
                                    }
                                    -1
                                } else {
                                    1
                                }
                            }
                        } else {
                            -1
                        }
                    }
                }
            } else {
                //文本或者id匹配查找目标
                val packageName: String = idPackageName ?: service.packageName

                //临时存储node
                var tempList: MutableList<AccessibilityNodeInfoCompat>

                //列表中的所有文本是否都匹配通过
                val matchMap = ArrayMap<Int, List<AccessibilityNodeInfoCompat>>()
                for (index: Int in textList.indices) {
                    tempList = mutableListOf()
                    try {
                        val text = textList[index]
                        //完整id 是需要包含包名的
                        val isIdText: Boolean = constraintBean.idList?.getOrNull(index) == 1
                        val subText: String? = if (isIdText) packageName.id(text!!) else text

                        if (!isIdText && subText == null) {
                            //text匹配模式下, null 匹配所有节点, 注意性能.
                            //tempList.add(rootNodeWrap)
                            rootNodeInfo.findNode(tempList, deep = constraintBean.deep) {
                                //匹配所有节点
                                1
                            }
                            matchMap[index] = tempList
                        } else {
                            rootNodeInfo.findNode(
                                tempList,
                                deep = constraintBean.deep
                            ) { nodeInfoCompat ->
                                var findNode = -1
                                if (isIdText) {
                                    //id 全等匹配
                                    val idName = nodeInfoCompat.viewIdName()
                                    findNode = if (subText == idName) {
                                        1
                                    } else {
                                        -1
                                    }
                                } else {
                                    //文本包含匹配
                                    findNode = if (nodeInfoCompat.haveText(subText ?: "")) {
                                        1
                                    } else {
                                        -1
                                    }
                                }

                                if (findNode == 1) {
                                    if (constraintBean.selectNodeList.isNullOrEmpty()) {
                                        //不需要挑选节点
                                        findNode = if (matchClassAndRectAndState(
                                                constraintBean,
                                                nodeInfoCompat,
                                                index
                                            )
                                        ) {
                                            //其他约束匹配成功
                                            val node = getStateParentNode(
                                                constraintBean.stateList,
                                                nodeInfoCompat,
                                                index
                                            ).second
                                            if (!constraintBean.pathList.isNullOrEmpty()) {
                                                parseConstraintPath(
                                                    constraintBean,
                                                    constraintBean.pathList?.getOrNull(index),
                                                    node,
                                                    tempList
                                                )
                                                -1
                                            } else {
                                                if (node != nodeInfoCompat) {
                                                    //查找的 nodeInfoCompat 被替换成 node
                                                    tempList.add(node)
                                                    -1
                                                } else {
                                                    1
                                                }
                                            }
                                        } else {
                                            -1
                                        }
                                    } else {
                                        //需要挑选节点, 那么[matchClassAndRectAndState]将在后面执行
                                        val node = getStateParentNode(
                                            constraintBean.stateList,
                                            nodeInfoCompat,
                                            index
                                        ).second
                                        findNode = if (!constraintBean.pathList.isNullOrEmpty()) {
                                            parseConstraintPath(
                                                constraintBean,
                                                constraintBean.pathList?.getOrNull(index),
                                                node,
                                                tempList
                                            )
                                            -1
                                        } else {
                                            if (node != nodeInfoCompat) {
                                                //查找的 nodeInfoCompat 被替换成 node
                                                tempList.add(node)
                                                -1
                                            } else {
                                                1
                                            }
                                        }
                                    }
                                }

                                findNode
                            }

                            if (tempList.isNotEmpty()) {
                                matchMap[index] = tempList
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                //是否所有文本都匹配到了
                var allTextMatch = true
                for (index: Int in textList.indices) {
                    if (matchMap[index].isNullOrEmpty()) {
                        allTextMatch = false
                        break
                    }
                }

                //全部匹配到, 将匹配到的node返回
                if (allTextMatch) {
                    for (index: Int in textList.indices) {
                        matchMap[index]?.forEach {
                            if (!result.contains(it)) {
                                result.add(it)
                            }
                        }
                    }
                }
            }
        }

        //节点数量约束
        if (constraintBean.nodeCount != null) {
            if (!compareStringNum(constraintBean.nodeCount, result.size.toFloat())) {
                //数量不匹配, 清空节点
                result.clear()
            }
        }

        //筛选需要特殊处理的节点
        if (!constraintBean.selectNodeList.isNullOrEmpty()) {
            var selectResult = true

            constraintBean.selectNodeList?.forEach {
                val index = when (it) {
                    666666 -> nextInt(0, result.size)
                    else -> it
                }
                val node = result.getOrNull2(index)
                if (node != null) {
                    if (!matchClassAndRectAndState(constraintBean, node, index)) {
                        selectResult = false
                        return@forEach
                    }
                }
            }

            if (!selectResult) {
                //不匹配
                result.clear()
            }
        }

        /*------------------------------后置处理---------------------------------------*/

        //条件过滤筛选
        if (!constraintBean.conditionList.isNullOrEmpty()) {
            //需要条件筛选
            val conditionNodeList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()
            result.forEach { node ->
                for (condition in constraintBean.conditionList!!) {
                    val targetNode = if (condition.root) rootNodeWrap else node
                    val isGet = parseCondition(service, autoParseAction, targetNode, condition)
                    if (isGet) {
                        //筛选通过
                        conditionNodeList.add(node) // rootNodeWrap or node ?
                        break
                    }
                }
            }
            parseResult.conditionNodeList = conditionNodeList
        }

        //依次需要匹配child
        if (!constraintBean.childMatchList.isNullOrEmpty()) {
            if (parseResult.isHaveCondition()) {
                parseResult.conditionNodeList?.apply {
                    val list = parseChildMatch(
                        service,
                        autoParseAction,
                        this,
                        constraintBean.childMatchList!!
                    )
                    clear()
                    addAll(list)
                }
            } else {
                result.apply {
                    val list = parseChildMatch(
                        service,
                        autoParseAction,
                        this,
                        constraintBean.childMatchList!!
                    )
                    clear()
                    addAll(list)
                }
            }
        }

        //最终需要返回的节点列表
        val _result: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

        val after = constraintBean.after
        if (after == null || after.isConstraintEmpty() || result.isEmpty()) {
            if (result.isNotEmpty()) {
                _result.addAll(result)
            }
        } else {
            //还有[after]约束
            val afterNodeList =
                if (!parseResult.isHaveCondition()) result else parseResult.conditionNodeList!!

            val resultAfter: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()
            afterNodeList.forEach { node ->
                //after 继续查找
                val nextParseResult = ParseResult(after, parseResult.constraintList)
                findConstraintNodeByRootNode(
                    service,
                    autoParseAction,
                    node.unwrap(),
                    nextParseResult
                )
                resultAfter.addAll(nextParseResult.nodeList)

                //条件约束筛选后的节点集合
                if (!parseResult.isHaveCondition()) {
                    parseResult.conditionNodeList = nextParseResult.conditionNodeList
                } else {
                    parseResult.conditionNodeList?.addAll(
                        nextParseResult.conditionNodeList ?: emptyList()
                    )
                }
            }

            if (constraintBean.useAfterNode) {
                _result.clear()
            }

            if (resultAfter.isNotEmpty()) {
                _result.addAll(resultAfter)
            }
        }

        if (_result.isNotEmpty()) {
            //parent 递归查询
            val parent = constraintBean.parent
            val finishResult = if (parent != null) {
                val resultParent: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()
                _result.forEach { node ->
                    parseParentNode(service, autoParseAction, node, parent, 1, resultParent)
                }
                resultParent
            } else {
                _result
            }
            parseResult.nodeList.addAll(finishResult)
            if (isShowDebug()) {
                autoParseAction.handleActionLog(buildString {
                    append("命中(count=${finishResult.size}) first↓")
                    appendLine()
                    append(constraintBean.toJson {
                        default()
                        disableHtmlEscaping()
                    })
                    appendLine()
                    finishResult.first().unwrap()
                        .debugNodeInfo(outBuilder = this, logAction = false, logChild = false)
                })
            }
        }
    }

    /**匹配类名和矩形约束*/
    open fun matchClassAndRect(
        node: AccessibilityNodeInfoCompat,
        cls: String? = null,
        rect: String? = null,
    ): Boolean {
        //是否匹配成功
        var result = false

        if (!cls.isNullOrEmpty() /*指定了匹配类名*/ &&
            !node.className.have(cls) /*类名匹配未命中*/
        ) {
            //但是类名不同
            result = false
        } else {
            //类名命中

            if (rect != null) {
                //坐标约束
                val bound = node.bounds()
                //如果设置了矩形匹配规则, 那么这个node的rect一定要是有效的

                //[-0.1,0.9~0.1,0.9999]格式
                var rectString: String? = null
                //[>=780]
                var widthString: String? = null
                var heightString: String? = null

                if (rect.isNullOrEmpty()) {
                    rectString = rect
                } else if (rect.contains(",")) {
                    //包含矩形约束信息, l,t~r,b
                    rect.split(":").apply {
                        rectString = getOrNull(0)
                        widthString = getOrNull(1)
                        heightString = getOrNull(2)
                    }
                } else {
                    //单独宽高约束 w:h
                    rect.split(":").apply {
                        widthString = getOrNull(0)
                        heightString = getOrNull(1)
                    }
                }

                result = true
                //矩形坐标约束
                rectString?.also {
                    result = MathParser.verifyRectValue(bound, it, maxWidth, maxHeight)
                }

                if (result) {
                    //宽度约束
                    widthString?.also {
                        if (!compareStringNum(it, bound.width().toFloat(), maxWidth.toFloat())) {
                            result = false
                        }
                    }
                }

                if (result) {
                    //高度约束
                    heightString?.also {
                        if (!compareStringNum(it, bound.height().toFloat(), maxHeight.toFloat())) {
                            result = false
                        }
                    }
                }
            } else {
                //other
                result = true
            }
        }

        return result
    }

    /**匹配类名和矩形约束和状态约束*/
    open fun matchClassAndRectAndState(
        constraintBean: ConstraintBean,
        node: AccessibilityNodeInfoCompat,
        index: Int /*对应text中的索引*/
    ): Boolean {
        val cls = constraintBean.clsList?.getOrNull2(index)
        val rect = constraintBean.rectList?.getOrNull2(index)

        //矩形和类名
        var result = matchClassAndRect(node, cls, rect)

        //状态约束
        if (result) {
            val stateList: List<String>? = constraintBean.stateList
            if (stateList != null && !stateList.isListEmpty()) {
                //状态约束后的匹配结果
                val match = getStateParentNode(constraintBean.stateList, node, index).first

                if (!match) {
                    //匹配状态失败
                    result = false
                }
            }
        }

        return result
    }

    /**根据约束的路径, 找出对应的node
     * @param path 格式: +1 -2 >3 <4
     * */
    open fun parseConstraintPath(
        constraintBean: ConstraintBean,
        path: String?,
        node: AccessibilityNodeInfoCompat,
        result: MutableList<AccessibilityNodeInfoCompat>
    ) {
        if (path.isNullOrEmpty()) {
            result.add(node)
        } else {
            var target: AccessibilityNodeInfoCompat? = node
            //格式: +1 -2 >3 <4
            val paths = path.split(" ").toList()
            for (p in paths) {
                target = parsePath(p, target)

                if (target == null) {
                    break
                }
            }
            if (target != null) {
                result.add(target)
            }
        }
    }

    /**@param path +1 / -1  / >1 / <1 */
    open fun parsePath(
        path: String,
        node: AccessibilityNodeInfoCompat?
    ): AccessibilityNodeInfoCompat? {
        return if (node == null || path.isEmpty()) {
            node
        } else {
            //[+1] 兄弟下1个的节点
            //[-2] 兄弟上2个的节点
            //[>3] child第3个节点
            //[<4] 第4个parent

            val num = path.getLongNum()?.toInt() ?: 0 //path.substring(1, path.length).toIntOrNull()
            val stateList = path.patternList("[a-z]+") //clickable等状态匹配

            if (stateList.isEmpty()) {
                val numAbs = num.abs()
                //不需要匹配状态
                when {
                    path.startsWith("+") -> node.getBrotherNode(numAbs)
                    path.startsWith("-") -> node.getBrotherNode(-numAbs)
                    path.startsWith(">") -> node.getParentOrChildNode(numAbs)
                    path.startsWith("<") -> node.getParentOrChildNode(-numAbs)
                    else -> null
                }
            } else {
                //需要匹配状态
                if (num > 1) {
                    var result: AccessibilityNodeInfoCompat? = node
                    for (i in 0 until num) {
                        result = when {
                            path.startsWith("+") -> result?.getBrotherNodeNext(stateList)
                            path.startsWith("-") -> result?.getBrotherNodePrev(stateList)
                            path.startsWith(">") -> result?.getParentOrChildNodeDown(stateList)
                            path.startsWith("<") -> result?.getParentOrChildNodeUp(stateList)
                            else -> null
                        }
                        if (result == null) {
                            break
                        }
                    }
                    result
                } else {
                    when {
                        path.startsWith("+") -> node.getBrotherNodeNext(stateList)
                        path.startsWith("-") -> node.getBrotherNodePrev(stateList)
                        path.startsWith(">") -> node.getParentOrChildNodeDown(stateList)
                        path.startsWith("<") -> node.getParentOrChildNodeUp(stateList)
                        else -> null
                    }
                }
            }
        }
    }

    /**条件过滤, 返回值表示过滤成功*/
    open fun parseCondition(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        node: AccessibilityNodeInfoCompat,
        condition: ConditionBean
    ): Boolean {
        //true 表示节点满足条件
        var isGet = false

        //child 数量条件
        val childCount = condition.childCount
        if (childCount != null) {
            isGet = compareStringNum(childCount, node.childCount.toFloat())
            if (!isGet) {
                //没有匹配上, 结束后续的匹配
                return false
            }
        }

        //包含文本
        if (condition.containsText != null) {
            var isHave = true
            condition.containsText?.forEach {
                if (!node.haveText(it)) {
                    isHave = false
                }
            }
            if (isHave) {
                isGet = true
            }

            if (!isGet) {
                return false
            }
        }

        //不包含文本
        if (condition.notContainsText != null) {
            var isHave = false
            condition.notContainsText?.forEach {
                if (node.haveText(it)) {
                    isHave = true
                }
            }
            if (isHave) {
                isGet = false
            }

            if (!isGet) {
                return false
            }
        }

        //指定了op
        if (condition.op != null) {
            if (condition.checkList == null) {
                if (parseConditionCheck(
                        service,
                        autoParseAction,
                        node,
                        condition,
                        condition.check
                    )
                ) {
                    //匹配通过
                    isGet = true
                }
            } else {
                for (check in condition.checkList!!) {
                    if (!parseConditionCheck(
                            service,
                            autoParseAction,
                            node,
                            condition,
                            check
                        )
                    ) {
                        //匹配未通过
                        isGet = false
                        break
                    } else {
                        isGet = true
                    }
                }
            }
        }
        return isGet
    }

    /**返回某一条[check]是否符合条件*/
    fun parseConditionCheck(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        node: AccessibilityNodeInfoCompat,
        condition: ConditionBean,
        check: ConstraintBean?
    ): Boolean {
        var isGet = false
        when (condition.op) {
            ConditionBean.OP_IS -> {
                //必须满足 check
                if (check != null) {
                    if (parse(service, autoParseAction, listOf(node.unwrap()), listOf(check))) {
                        isGet = true
                    }
                }
            }
            ConditionBean.OP_NOT -> {
                //必须不满足 check
                if (check != null) {
                    if (!parse(
                            service,
                            autoParseAction,
                            listOf(node.unwrap()),
                            listOf(check)
                        )
                    ) {
                        isGet = true
                    }
                }
            }
        }
        return isGet
    }

    /**递归查找parent, 直到找到符合条件的节点*/
    fun parseParentNode(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        node: AccessibilityNodeInfoCompat,//从此节点开始查询
        parentBean: ParentBean,
        depth: Int = 1, //当前查询深度
        result: MutableList<AccessibilityNodeInfoCompat> //查询到的节点
    ) {
        val constraintList = parentBean.checkList
        val maxDepth = parentBean.depth

        if (constraintList.isNullOrEmpty() || depth > maxDepth) {
            //深度超限
            return
        }

        val path = parentBean.path ?: "-1"

        //先拿到parent node
        val parent = parsePath("<1", node)
        if (parent != null) {
            //再拿到需要搜索的根节点

            fun findNodeByTarget(
                findPath: String,
                findByNode: AccessibilityNodeInfoCompat
            ): Boolean /*返回值, 表示是否需要继续递归parent*/ {
                val target = parsePath(findPath, findByNode)

                if (target == null) {
                    return true
                } else {
                    //开始查询

                    for (constraint in constraintList) {
                        val parseResult = ParseResult(constraint, constraintList)
                        findConstraintNodeByRootNode(
                            service,
                            autoParseAction,
                            target.unwrap(),
                            parseResult
                        )
                        parseResult.resultHandleNodeList().apply {
                            if (this?.isNotEmpty() == true) {
                                result.addAll(this)
                            }
                        }
                    }

                    if (result.isNotEmpty()) {
                        //查找到了
                        return false
                    } else {
                        //没有查找到, 继续枚举其他兄弟node
                        val newPath: String
                        val newIndex = (findPath.getLongNum(true) ?: 0) + 1
                        if (findPath.startsWith("-")) {
                            newPath = "-${newIndex}"
                        } else if (findPath.startsWith("+")) {
                            newPath = "+${newIndex}"
                        } else {
                            return true
                        }
                        return findNodeByTarget(newPath, findByNode)
                    }
                }
            }

            if (findNodeByTarget(path, parent)) {
                parseParentNode(
                    service,
                    autoParseAction,
                    parent,
                    parentBean,
                    depth + 1,
                    result
                )
            }
        }
    }

    /**依次匹配node的所有child,是否都符合条件*/
    fun parseChildMatch(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        nodeList: List<AccessibilityNodeInfoCompat>,
        childMatchList: List<ConstraintBean>
    ): List<AccessibilityNodeInfoCompat> {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()

        nodeList.forEach {
            var add = true
            it.eachChild { index, child ->
                val matchConstraint = childMatchList.getOrNull(index)
                if (matchConstraint == null) {
                    //没有约束条件, 则直接通过
                } else {
                    //只用了约束条件, 则符合条件才通过
                    val parseResult = ParseResult(matchConstraint)
                    findConstraintNodeByRootNode(
                        service,
                        autoParseAction,
                        child.unwrap(),
                        parseResult
                    )
                    if (parseResult.haveNodeList()) {
                        //找到了符合条件的node
                    } else {
                        //不符合条件
                        add = false
                    }
                }
            }
            if (add) {
                //...
                result.add(it)
            }
        }

        return result
    }
}