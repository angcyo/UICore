package com.angcyo.acc2.parse

import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.ChildBean
import com.angcyo.acc2.bean.FindBean
import com.angcyo.acc2.bean.WindowBean
import com.angcyo.acc2.control.log
import com.angcyo.acc2.eachChildDepth
import com.angcyo.library.ex.*


/**
 * 查找元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FindParse(val accParse: AccParse) {

    /**路径解析*/
    var pathParse = PathParse(accParse)

    //<editor-fold desc="parse">

    /**通过一组规则, 查找满足规则的节点集合, 就终止*/
    fun parse(
        rootList: List<AccessibilityNodeInfoCompat>?,
        findList: List<FindBean>?
    ): FindResult {
        var result = FindResult()

        val accControl = accParse.accControl
        if (!findList.isNullOrEmpty()) {
            for (findBean in findList) {
                val parseResult = parse(rootList, findBean).apply {
                    result.forceSuccess == forceSuccess || result.forceSuccess
                    result.forceFail == forceFail || result.forceFail
                }

                if (parseResult.success) {
                    //匹配成功, 中断查询, 提升效率
                    result = parseResult
                    val toLog =
                        parseResult.nodeList?.toLog("Find找到节点[${result.nodeList.size()}]$findBean↓")
                    accControl.log(toLog)
                    break
                }
            }
        }

        if (result.success) {
            accControl.log("Find找到节点[${result.nodeList.size()}]↑")
            accControl.accPrint.findNode(result.nodeList)
        }

        return result
    }

    /**返回满足规则的节点集合*/
    fun parse(rootList: List<AccessibilityNodeInfoCompat>?, findBean: FindBean): FindResult {
        val result = FindResult()

        //准备content

        //选择window

        //根节点选择
        val rootNodeList = if (findBean.window == null) {
            rootList
        } else {
            rootWindowNode(findBean.window)
        }

        if (rootNodeList.isNullOrEmpty()) {
            return result
        }

        //---------------------约束条件判断-----------------------

        if (findBean.conditionList != null) {
            //有条件需要判断
            if (!accParse.conditionParse.parse(findBean.conditionList).success) {
                //不满足条件
                accParse.accControl.log("Find未满足条件[$findBean]↓\n${findBean.conditionList}")
                return result
            }
        }

        //-----------------------选择元素------------------------

        //找到的元素
        val findNodeList = mutableListOf<AccessibilityNodeInfoCompat>()

        val text = findBean.textList != null
        val cls = findBean.clsList != null
        val id = findBean.idList != null
        val rect = findBean.rectList != null
        val state = findBean.stateList != null
        val child = findBean.childList != null
        val path = findBean.pathList != null

        when {
            text -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.textList))
            cls -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.clsList))
            id -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.idList))
            rect -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.rectList))
            state -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.stateList))
            child -> findNodeList.addAll(
                findNodeByChild(rootNodeList, findBean, findBean.childList)
            )
            path -> {
                rootNodeList.forEach { node ->
                    findBean.pathList?.forEach { pathStr ->
                        pathParse.parse(node, pathStr)?.let {
                            findNodeList.add(it)
                        }
                    }
                }
            }
            //空的选择器
            else -> findNodeList.addAll(rootNodeList)
        }

        //------------------------筛选器------------------------

        //第1层筛选
        if (findBean.index != null) {
            val filterFindNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
            val index = accParse.parseText(findBean.index).firstOrNull()
            filterFindNodeList.addAll(findNodeList.eachRangeItem(index))
            findNodeList.resetAll(filterFindNodeList)
        }

        //第2层筛选
        if (findBean.childIndex != null) {
            val filterFindNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
            val childIndex = accParse.parseText(findBean.childIndex).firstOrNull()
            findNodeList.forEach { node ->
                filterFindNodeList.addAll(node.childList().eachRangeItem(childIndex))
            }
            findNodeList.resetAll(filterFindNodeList)
        }

        //第3层筛选
        if (findBean.parent != null) {
            val filterFindNodeList = mutableListOf<AccessibilityNodeInfoCompat>()
            findNodeList.forEach { node ->
                findParentByNode(node, findBean.parent!!)?.apply {
                    filterFindNodeList.add(this)
                }
            }
            findNodeList.resetAll(filterFindNodeList)
        }

        //------------------------后处理-------------------------

        //需要过滤
        if (findBean.filter != null) {
            findNodeList.removeAll(accParse.filterParse.parse(findNodeList, findBean.filter))
        }

        //临时使用这些节点 use
        if (findBean.use != null) {
            accParse.handleParse.parse(findNodeList, findBean.use).apply {
                result.forceSuccess == forceSuccess || result.forceSuccess
                result.forceFail == forceFail || result.forceFail
            }
        }

        //递归处理
        val after = findBean.after
        var afterResult: FindResult? = null
        if (after != null) {
            if (findBean.afterAlways || findNodeList.isNotEmpty()) {
                afterResult = parse(findNodeList, after).apply {
                    forceSuccess == forceSuccess || result.forceSuccess
                    forceFail == forceFail || result.forceFail
                }
            }
        }

        //-----------------------返回结果--------------------------

        return afterResult ?: result.apply {
            success = findNodeList.isNotEmpty()
            nodeList = findNodeList
            if (success) {
                this.findBean = findBean
            }
        }
    }

    /**检查是否需要中断枚举查找元素
     * 是否中断枚举*/
    fun checkFindLimit(
        nodeList: List<AccessibilityNodeInfoCompat>?,
        findBean: FindBean,
        depth: Int
    ): Boolean {
        val findLimit = findBean.findLimit ?: accParse.accContext.findLimit
        var result = if (findLimit.isNullOrEmpty()) {
            false
        } else {
            accParse.expParse.parseAndCompute(findLimit, inputValue = nodeList.size().toFloat())
        }

        if (!result) {
            //没有超限, 第二层判断
            val findDepth = findBean.findDepth ?: accParse.accContext.findDepth
            result = if (findDepth.isNullOrEmpty()) {
                false
            } else {
                accParse.expParse.parseAndCompute(findDepth, inputValue = depth.toFloat())
            }
        }

        return result
    }

    //</editor-fold desc="parse">

    //<editor-fold desc="window">

    /**当前节点是否要被忽略*/
    fun isIgnorePackageName(node: AccessibilityNodeInfoCompat, packageName: String?): Boolean {
        if (packageName == null) {
            return false
        }
        val nameList = accParse.parsePackageName(packageName, null)
        var ignore = false
        for (name in nameList) {
            ignore = node.packageName.have(name)
            if (ignore) {
                break
            }
        }
        return ignore
    }

    /**当前节点是否需要*/
    fun isNeedPackageName(node: AccessibilityNodeInfoCompat, packageName: String?): Boolean {
        if (packageName == null) {
            return false
        }
        val nameList = accParse.parsePackageName(packageName)
        var need = false
        for (name in nameList) {
            need = node.packageName.have(name)
            if (need) {
                break
            }
        }
        return need
    }

    //根据指定包名, 查找符合的[AccessibilityWindowInfo]
    fun _findWindowBy(packageName: String?): List<AccessibilityWindowInfo> {
        val result = mutableListOf<AccessibilityWindowInfo>()
        if (!packageName.isNullOrEmpty()) {
            accParse.accControl.accService()?.windows?.forEach {
                it.root?.wrap()?.let { node ->
                    if (isNeedPackageName(node, packageName)) {
                        result.add(it)
                    }
                }
            }
        }
        return result
    }

    //根据指定包名和过滤的包名, 查找符合的[AccessibilityNodeInfoCompat]
    fun _findRootNodeBy(
        packageName: String?,
        ignorePackageName: String? = null
    ): List<AccessibilityNodeInfoCompat> {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        when {
            //无包名字段, 则使用活动窗口
            packageName == null && ignorePackageName == null -> {
                accParse.accControl.accService()?.rootInActiveWindow?.wrap()?.let { node ->
                    if (!isIgnorePackageName(node, ignorePackageName)) {
                        result.add(node)
                    }
                }
            }
            //如果包名为空字符, 则支持所有window
            packageName.isNullOrBlank() || packageName == Action.ALL -> {
                accParse.accControl.accService()?.windows?.forEach {
                    it.root?.wrap()?.let { node ->
                        if (!isIgnorePackageName(node, ignorePackageName)) {
                            result.add(node)
                        }
                    }
                }
            }
            else -> {
                _findWindowBy(packageName).forEach { window ->
                    window.root?.wrap()?.let { node ->
                        if (!isIgnorePackageName(node, ignorePackageName)) {
                            result.add(node)
                        }
                    }
                }
            }
        }
        return result
    }

    /**根据[WindowBean]的描述, 获取根节点集合*/
    fun findRootNode(windowBean: WindowBean?): List<AccessibilityNodeInfoCompat>? {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (windowBean == null) {
            //未指定, 采用默认处理
            val taskPackageName = accParse.accControl._taskBean?.packageName
            result.addAll(_findRootNodeBy(taskPackageName))
        } else {
            //解析window约束
            result.addAll(_findRootNodeBy(windowBean.packageName, windowBean.ignorePackageName))
        }

        //矩形约束
        if (windowBean?.rect != null) {
            val removeList = mutableListOf<AccessibilityNodeInfoCompat>()
            result.forEach {
                if (!matchNodeRect(it, windowBean.rect)) {
                    removeList.add(it)
                }
            }
            result.removeAll(removeList)
        }

        return result
    }

    /**规则下的默认根节点集合*/
    fun rootWindowNode(findWindowBean: WindowBean? = null): List<AccessibilityNodeInfoCompat>? {
        return findRootNode(windowBean(findWindowBean))
    }

    fun windowBean(findWindowBean: WindowBean? = null) = accParse.accControl.accSchedule.run {
        findWindowBean
            ?: _runActionBean?.check?.window
            ?: _runActionBean?.window
            ?: _scheduleActionBean?.window
    }

    //</editor-fold desc="window">

    //<editor-fold desc="find">

    /**查找节点*/
    fun findNode(
        originList: List<AccessibilityNodeInfoCompat>,
        findBean: FindBean,
        list: List<String?>?
    ): List<AccessibilityNodeInfoCompat> {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (list.isNullOrEmpty()) {
            return result
        }
        originList.forEach { rootNode ->
            val rootResult = mutableListOf<AccessibilityNodeInfoCompat>()
            val rootMatchMap = hashMapOf<String?, Boolean>()

            rootNode.eachChildDepth { node, depth ->
                list.forEachIndexed { index, str ->
                    if (matchNode(node, findBean, index)) {
                        val path = findBean.pathList?.getOrNull(index)

                        if (path.isNullOrEmpty()) {
                            rootResult.add(node)
                            rootMatchMap[str] = true
                        } else {
                            //路径解析
                            pathParse.parse(node, path)?.let {
                                rootResult.add(it)
                                rootMatchMap[str] = true
                            }
                        }
                    }
                }
                checkFindLimit(rootResult, findBean, depth)
            }

            //必须全部命中
            var allMatch = true
            for (key in list) {
                if (!key.isNullOrBlank()) {
                    val match = rootMatchMap[key]
                    if (match == true) {
                        //匹配到
                    } else {
                        //未匹配
                        allMatch = false
                        break
                    }
                }
            }

            if (allMatch) {
                result.addAll(rootResult)
            }
        }
        return result
    }

    /**查找节点*/
    fun findNodeByChild(
        originList: List<AccessibilityNodeInfoCompat>,
        findBean: FindBean,
        list: List<ChildBean>?
    ): List<AccessibilityNodeInfoCompat> {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (list.isNullOrEmpty()) {
            return result
        }
        originList.forEach { rootNode ->
            rootNode.eachChildDepth { node, depth ->
                if (matchNodeChild(node, list)) {
                    result.add(node)
                    if (checkFindLimit(result, findBean, depth)) {
                        return@eachChildDepth true
                    }
                }
                false
            }
        }
        return result
    }

    /**在当前节点[node]查找符合条件的父节点*/
    fun findParentByNode(
        node: AccessibilityNodeInfoCompat,
        condition: ChildBean
    ): AccessibilityNodeInfoCompat? {
        var result: AccessibilityNodeInfoCompat? = node.parent

        while (result != null) {
            if (matchNode(result, condition)) {
                break
            }
            result = result.parent
        }

        return result
    }

    //</editor-fold desc="find">

    //<editor-fold desc="match">

    /**节点必须要满足的条件*/
    fun matchNode(node: AccessibilityNodeInfoCompat, findBean: FindBean, index: Int): Boolean {
        return matchNode(
            node,
            findBean.textList?.getOrNull(index),
            findBean.clsList?.getOrNull(index),
            findBean.idList?.getOrNull(index),
            findBean.rectList?.getOrNull(index),
            findBean.stateList?.getOrNull(index),
            findBean.childList
        )
    }

    fun matchNode(node: AccessibilityNodeInfoCompat, condition: ChildBean): Boolean {
        return matchNode(
            node,
            condition.rect,
            condition.cls,
            condition.id,
            condition.rect,
            condition.state,
            condition.childList
        )
    }

    /**节点必须要满足的条件*/
    fun matchNode(
        node: AccessibilityNodeInfoCompat,
        text: String?,
        cls: String?,
        id: String?,
        rect: String?,
        state: String?,
        childList: List<ChildBean>?
    ): Boolean {
        var result = true

        var matchText: Boolean? = null
        if (result) {
            matchText = matchNodeText(node, text)
            result = matchText
        }

        var matchClass: Boolean? = null
        if (result) {
            matchClass = matchNodeClass(node, cls)
            result = matchClass
        }

        var matchId: Boolean? = null
        if (result) {
            matchId = matchNodeId(node, id)
            result = matchId
        }

        var matchState: Boolean? = null
        if (result) {
            matchState = matchNodeState(node, state)
            result = matchState
        }

        var matchRect: Boolean? = null
        if (result) {
            matchRect = matchNodeRect(node, rect)
            result = matchRect
        }

        var matchChild: Boolean? = null
        if (result) {
            matchChild = matchNodeChild(node, childList)
            result = matchChild
        }

        if (!result) {
            var notNullNum = 0
            if (matchText != null) {
                notNullNum++
            }
            if (matchClass != null) {
                notNullNum++
            }
            if (matchId != null) {
                notNullNum++
            }
            if (matchState != null) {
                notNullNum++
            }
            if (matchRect != null) {
                notNullNum++
            }
            if (matchChild != null) {
                notNullNum++
            }
            if (isDebug() && notNullNum >= 2 && !node.text().isNullOrEmpty()) {
                //匹配2个及以上失败后, 才打印日志
                accParse.accControl.log(buildString {
                    appendLine(node.toLog())
                    append("match:")
                    append("text:${matchText?.toDC()} ")
                    append("class:${matchClass?.toDC()} ")
                    append("id:${matchId?.toDC()} ")
                    append("state:${matchState?.toDC()} ")
                    append("rect:${matchRect?.toDC()} ")
                    append("child:${matchChild?.toDC()} ")
                }, 2)
            }
        }

        return result
    }

    /**检查节点是否满足text*/
    fun matchNodeText(node: AccessibilityNodeInfoCompat, arg: String?): Boolean {
        val text = accParse.parseText(arg).firstOrNull()
        if (text.isNullOrBlank()) {
            return true
        }
        return node.haveText(text)
    }

    /**检查节点是否满足cls*/
    fun matchNodeClass(node: AccessibilityNodeInfoCompat, cls: String?): Boolean {
        if (cls.isNullOrBlank()) {
            return true
        }
        return node.className.have(cls)
    }

    /**检查节点是否满足id*/
    fun matchNodeId(node: AccessibilityNodeInfoCompat, id: String?): Boolean {
        if (id.isNullOrBlank()) {
            return true
        }
        return node.viewIdName() == node.packageName.id(id)
    }

    /**检查节点是否满足state*/
    fun matchNodeState(node: AccessibilityNodeInfoCompat, state: String?): Boolean {
        if (state.isNullOrBlank()) {
            return true
        }
        var match = true
        when (state) {
            Action.STATE_CLICKABLE -> {
                //需要具备可以点击的状态
                if (!node.isClickable) {
                    match = false
                }
            }
            Action.STATE_UN_CLICKABLE -> {
                //需要具备不可以点击的状态
                if (node.isClickable) {
                    match = false
                }
            }
            Action.STATE_FOCUSABLE -> {
                //需要具备可以获取焦点状态
                if (!node.isFocusable) {
                    match = false
                }
            }
            Action.STATE_UN_FOCUSABLE -> {
                //需要具备不可以获取焦点状态
                if (node.isFocusable) {
                    match = false
                }
            }
            Action.STATE_FOCUSED -> {
                //需要具备焦点状态
                if (!node.isFocused) {
                    match = false
                }
            }
            Action.STATE_UNFOCUSED -> {
                //需要具备无焦点状态
                if (node.isFocused) {
                    match = false
                }
            }
            Action.STATE_SELECTED -> {
                //需要具备选中状态
                if (!node.isSelected) {
                    match = false
                }
            }
            Action.STATE_UNSELECTED -> {
                //需要具备不选中状态
                if (node.isSelected) {
                    match = false
                }
            }
            Action.STATE_SCROLLABLE -> {
                //需要具备可滚动状态
                if (!node.isScrollable) {
                    match = false
                }
            }
            Action.STATE_UN_SCROLLABLE -> {
                //需要具备不可滚动状态
                if (node.isScrollable) {
                    match = false
                }
            }
            Action.STATE_LONG_CLICKABLE -> {
                //需要具备可以长按的状态
                if (!node.isLongClickable) {
                    match = false
                }
            }
            Action.STATE_UN_LONG_CLICKABLE -> {
                //需要具备不可以长按的状态
                if (node.isLongClickable) {
                    match = false
                }
            }
            Action.STATE_ENABLE -> {
                //需要具备激活状态
                if (!node.isEnabled) {
                    match = false
                }
            }
            Action.STATE_DISABLE -> {
                //需要具备禁用状态
                if (node.isEnabled) {
                    match = false
                }
            }
            Action.STATE_PASSWORD -> {
                //需要具备密码状态
                if (!node.isPassword) {
                    match = false
                }
            }
            Action.STATE_UN_PASSWORD -> {
                //需要具备非密码状态
                if (node.isPassword) {
                    match = false
                }
            }
            Action.STATE_CHECKABLE -> {
                if (!node.isCheckable) {
                    match = false
                }
            }
            Action.STATE_UN_CHECKABLE -> {
                if (node.isCheckable) {
                    match = false
                }
            }
            Action.STATE_CHECKED -> {
                if (!node.isChecked) {
                    match = false
                }
            }
            Action.STATE_UNCHECKED -> {
                if (node.isChecked) {
                    match = false
                }
            }
        }
        return match
    }

    /**判断节点是否满足状态中的任意一个*/
    fun matchNodeStateOr(node: AccessibilityNodeInfoCompat, stateList: List<String>): Boolean {
        for (state in stateList) {
            if (matchNodeState(node, state)) {
                return true
            }
        }
        return false
    }

    /**
     * 获取状态中, 满足条件的节点
     * [com.angcyo.acc2.bean.FindBean.stateList]
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

    /**[com.angcyo.acc2.action.Action.ACTION_CLICK]
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

    /**检查节点是否满足rect*/
    fun matchNodeRect(node: AccessibilityNodeInfoCompat, rect: String?): Boolean {
        if (rect == null) {
            //空对象
            return true
        }
        val bound = node.bounds()
        if (rect.isEmpty()) {
            //空字符, 只要节点有大小即可
            return !bound.isEmpty
        }

        return accParse.rectParse.parse(rect, bound)
    }

    /**检查节点是否满足child*/
    fun matchNodeChild(node: AccessibilityNodeInfoCompat, childList: List<ChildBean>?): Boolean {
        if (childList.isNullOrEmpty()) {
            return true
        }

        if (node.childCount != childList.size()) {
            return false
        }

        //是否匹配通过
        var match = true
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val childBean = childList.getOrNull(i)
            if (child != null && childBean != null) {
                if (matchNode(child, childBean)) {
                    if (childBean.filter != null) {
                        //需要满足过滤条件
                        val removeList = accParse.filterParse.parse(listOf(child), childBean.filter)
                        if (removeList.contains(child)) {
                            //不满足过滤条件
                            match = false
                        }
                    }
                } else {
                    match = false
                }

                //忽略结果
                if (childBean.ignore) {
                    match = true
                    continue
                }
            }
        }

        return match
    }

    //</editor-fold desc="match">
}