package com.angcyo.core.component.accessibility.action

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import androidx.collection.ArrayMap
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ConditionBean
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.parse.isConstraintEmpty
import com.angcyo.core.component.accessibility.parse.isOnlyPathConstraint
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.app
import com.angcyo.library.ex.isListEmpty
import com.angcyo.library.utils.getFloatNum
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random.Default.nextBoolean

/**
 * 解析处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParser {

    companion object {

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
         * $N $0将会替换为[wordList]索引为0的值.最大支持10000
         * 1-4 取索引为[1-4]的值
         * 0--1 取索引为[0-倒数第1个]的值
         * -1 取倒数第1个的值
         * */
        fun parseWordTextList(originList: List<String>?, indexList: List<String>): List<String>? {
            if (originList.isListEmpty()) {
                return null
            }

            val originList: List<String> = originList!!

            val result = mutableListOf<String>()
            indexList.forEach { indexStr ->
                if (indexStr.contains("$")) {
                    //匹配 $表达式

                    var target: String? = null
                    for (i in 0..1_00_00) {
                        val pattern = "$$i"
                        if (indexStr.contains(pattern)) {
                            target =
                                (target ?: indexStr)
                                    .replace(pattern, originList.getOrNull(i) ?: "")
                        }
                    }
                    target?.also { result.add(it) }
                } else {
                    val num = indexStr.toIntOrNull()

                    if (num == null) {
                        //匹配 [1-4] 范围

                        val indexOf = indexStr.indexOf("-")
                        if (indexOf != -1) {
                            val startIndex = indexStr.substring(0, indexOf).toIntOrNull() ?: 0
                            val endIndex =
                                indexStr.substring(indexOf + 1, indexStr.length).toIntOrNull() ?: 0

                            val fist =
                                if (startIndex < 0) startIndex + originList.size else startIndex
                            val second =
                                if (endIndex < 0) endIndex + originList.size else endIndex

                            for (i in min(fist, second)..max(fist, second)) {
                                originList.getOrNull(i)?.also {
                                    result.add(it)
                                }
                            }
                        }
                    } else {
                        // 匹配 1, -1 索引
                        originList.getOrNull(if (num < 0) num + originList.size else num)?.also {
                            result.add(indexStr)
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

        /**比较
         * [>=2] 数量大于等于2
         * [>3] 数量大于3
         * [2] 数量等于2
         * [=2] 数量等于2
         * [<3] 数量小于3
         * 空字符 表示直接过
         * null 忽略此条件
         * [expression] 字符串数值表达式, 比如: >=2
         * [value] 需要比较的数值,比如: 3
         * */
        fun compareStringNum(expression: String?, value: Float, ref: Float = 1f): Boolean {
            var result = false
            if (expression != null) {
                if (expression.isEmpty()) {
                    result = true
                } else {
                    val num = expression.getFloatNum()
                    num?.also {
                        //如果是小数, 则按照比例计算
                        val tNum = if (it < 1) {
                            it * ref
                        } else {
                            it
                        }
                        if (expression.startsWith(">=")) {
                            if (value >= tNum) {
                                result = true
                            }
                        } else if (expression.startsWith(">")) {
                            if (value > tNum) {
                                result = true
                            }
                        } else if (expression.startsWith("<=")) {
                            if (value <= tNum) {
                                result = true
                            }
                        } else if (expression.startsWith("<")) {
                            if (value < tNum) {
                                result = true
                            }
                        } else {
                            if (value == tNum) {
                                result = true
                            }
                        }
                    }
                }
            }
            return result
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
         * [index] 未使用的参数
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

    //临时存放
    var _tempNodeRect = Rect()

    val maxWidth: Int
        get() = max(_screenWidth, _rootNodeRect.width())

    val maxHeight: Int
        get() = max(_screenHeight, _rootNodeRect.height())

    /**返回当前界面, 是否包含[constraintList]约束的Node信息
     * [onTargetResult] 当找到目标时, 通过此方法回调目标给调用者. first:对应的约束, second:约束对应的node集合
     * */
    open fun parse(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        nodeList: List<AccessibilityNodeInfo>,
        constraintList: List<ConstraintBean>,
        onTargetResult: (List<ParseResult>) -> Unit = {}
    ): Boolean {

        val result: MutableList<ParseResult> = mutableListOf()

        constraintList.forEach { constraint ->
            var enable: Boolean

            if (isHaveEnableConstraint(constraint.constraintId)) {
                constraint.enable = true
                enableConstraint(constraint.constraintId, false)
            }

            if (constraint.randomEnable) {
                //随机激活
                enable = nextBoolean()
            } else {
                enable = constraint.enable

                if (!enable) {
                    enable =
                        constraint.actionList?.find { it.startsWith(ConstraintBean.ACTION_ENABLE) } != null
                }
            }

            if (enable) {
                //如果当前的[constraint]处于激活状态, 或者拥有激活自身的能力
                val parseResult = ParseResult(constraint, constraintList)
                findConstraintNode(service, autoParseAction, nodeList, parseResult)

                parseResult.resultHandleNodeList().apply {
                    if (this?.isNotEmpty() == true) {
                        result.add(parseResult)
                    }
                }
            }
        }

        if (result.isNotEmpty()) {
            //通过约束, 找到了目标

            onTargetResult(result)
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

        //节点总数约束
        if (constraintBean.sizeCount != null) {
            val allNode = constraintBean.sizeCount?.contains("*") == true
            val sizeCountExp = constraintBean.sizeCount?.replaceFirst("*", "")

            var size = 0f
            var next = -1
            var allow = false
            rootNodeInfo.findNode {
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
                //不满足了约束条件
                return
            }
        }

        if (constraintBean.isConstraintEmpty()) {
            //空约束返回[rootNodeInfo]
            result.add(rootNodeWrap)
        } else {

            //需要匹配的文本
            val text: List<String?>? = autoParseAction.getTextList(constraintBean)

            if (text == null) {
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
                    rootNodeInfo.findNode(result) { nodeInfoCompat ->
                        if (match(constraintBean, nodeInfoCompat, 0)) {
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
                val packageName: String = idPackageName ?: service.packageName

                //临时存储node
                var tempList: MutableList<AccessibilityNodeInfoCompat>

                //列表中的所有文本是否都匹配通过
                val matchMap = ArrayMap<Int, List<AccessibilityNodeInfoCompat>>()
                for (index: Int in text.indices) {
                    tempList = mutableListOf()
                    try {

                        //完整id 是需要包含包名的
                        val isIdText: Boolean = constraintBean.idList?.getOrNull(index) == 1
                        val subText: String? =
                            if (isIdText) packageName.id(text[index]!!) else text[index]

                        if (!isIdText && subText == null) {
                            //text匹配模式下, null 匹配所有节点, 注意性能.
                            //tempList.add(rootNodeWrap)
                            rootNodeInfo.findNode(tempList) {
                                //匹配所有节点
                                1
                            }
                            matchMap[index] = tempList
                        } else {
                            rootNodeInfo.findNode(tempList) { nodeInfoCompat ->
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
                                    findNode = if (match(constraintBean, nodeInfoCompat, index)) {
                                        //其他约束匹配成功
                                        val node =
                                            getStateParentNode(
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
                for (index: Int in text.indices) {
                    if (matchMap[index].isNullOrEmpty()) {
                        allTextMatch = false
                        break
                    }
                }

                //全部匹配到, 将匹配到的node返回
                if (allTextMatch) {
                    for (index: Int in text.indices) {
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

        if (constraintBean.after == null ||
            constraintBean.after?.isConstraintEmpty() == true ||
            result.isEmpty()
        ) {
            if (result.isNotEmpty()) {
                parseResult.nodeList.addAll(result)
            }
        } else {
            //还有[after]约束
            val afterNodeList =
                if (!parseResult.isHaveCondition()) result else parseResult.conditionNodeList!!

            val resultAfter: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()
            afterNodeList.forEach { node ->
                //继续查找
                val nextParseResult =
                    ParseResult(constraintBean.after!!, parseResult.constraintList)
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

            if (resultAfter.isNotEmpty()) {
                parseResult.nodeList.addAll(resultAfter)
            }
        }
    }

    /**其他规则匹配*/
    open fun match(
        constraintBean: ConstraintBean,
        node: AccessibilityNodeInfoCompat,
        index: Int /*对应text中的索引*/
    ): Boolean {
        val cls = constraintBean.clsList?.getOrNull(index)
        val rect = constraintBean.rectList?.getOrNull(index)

        //是否匹配成功
        var result = false

        if (!cls.isNullOrEmpty() /*指定了匹配类名*/ &&
            node.className?.contains(cls.toString().toRegex()) != true /*类名匹配未命中*/
        ) {
            //但是类名不同
            result = false
        } else {
            //类名命中

            if (rect != null) {
                //坐标约束
                val bound = node.bounds()
                //如果设置了矩形匹配规则, 那么这个node的rect一定要是有效的

                //:符出现的次数
                val sum = rect.sumBy { if (it == ':') 1 else 0 }

                //[-0.1,0.9~0.1,0.9999]格式
                var rectString: String? = null
                //[>=780]
                var widthString: String? = null
                var heightString: String? = null

                if (rect.isNullOrEmpty()) {
                    rectString = rect
                } else if (rect.contains(",")) {
                    //包含矩形约束信息
                    if (sum == 0) {
                        rectString = rect
                    } else if (sum == 1) {
                        //出现一次
                        widthString = rect.split(":").getOrNull(1)
                    } else if (sum > 1) {
                        rect.split(":").apply {
                            widthString = getOrNull(1)
                            heightString = getOrNull(2)
                        }
                    }
                } else {
                    //单独宽高约束
                    if (sum == 0) {
                        widthString = rect
                    } else if (sum >= 1) {
                        rect.split(":").apply {
                            widthString = getOrNull(0)
                            heightString = getOrNull(1)
                        }
                    }
                }

                result = true
                //矩形坐标约束
                rectString?.also {
                    result = false
                    if (it.isEmpty()) {
                        //空字符只要宽高大于0, 就命中
                        result = node.isValid()
                    } else {
                        //兼容 ~ 和 -
                        if (it.contains("~")) {
                            it.split("~")
                        } else {
                            it.split("-")
                        }.apply {
                            val p1 = getOrNull(0)?.toPointF(maxWidth, maxHeight)
                            val p2 = getOrNull(1)?.toPointF(maxWidth, maxHeight)

                            if (p1 != null) {
                                if (p2 == null) {
                                    //只设置了单个点
                                    if (bound.contains(p1.x.toInt(), p1.y.toInt())) {
                                        result = true
                                    }
                                } else {
                                    _tempNodeRect.set(
                                        p1.x.toInt(),
                                        p1.y.toInt(),
                                        p2.x.toInt(),
                                        p2.y.toInt()
                                    )
                                    //设置了多个点, 那么只要2个矩形相交, 就算命中
                                    if (bound.intersect(_tempNodeRect)) {
                                        result = true
                                    }
                                }
                            }
                        }
                    }
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

    /**根据约束的路径, 找出对应的node*/
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

            val num = path.substring(1, path.length).toIntOrNull() ?: 0

            when {
                path.startsWith("+") -> node.getBrotherNode(num)
                path.startsWith("-") -> node.getBrotherNode(-num)
                path.startsWith(">") -> node.getParentOrChildNode(num)
                path.startsWith("<") -> node.getParentOrChildNode(-num)
                else -> null
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
}