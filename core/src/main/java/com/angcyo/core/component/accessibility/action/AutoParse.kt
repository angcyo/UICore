package com.angcyo.core.component.accessibility.action

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import androidx.collection.ArrayMap
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library.ex.isListEmpty

/**
 * 解析处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParse() {

    /**解析id时, 需要补全的id全路径包名*/
    var idPackageName: String? = null

    /**根节点在屏幕中的坐标*/
    var _rootNodeRect = Rect()

    /**返回当前界面, 是否包含[constraintList]约束的Node信息
     * [onTargetResult] 当找到目标时, 通过此方法回调目标给调用者. first:对应的约束, second:约束对应的node集合
     * */
    open fun parse(
        service: AccessibilityService,
        filterPackage: List<String>?,
        constraintList: List<ConstraintBean>,
        onTargetResult: (List<Pair<ConstraintBean, List<AccessibilityNodeInfoCompat>>>) -> Unit = {}
    ): Boolean {

        val result: MutableList<Pair<ConstraintBean, List<AccessibilityNodeInfoCompat>>> =
            mutableListOf()

        var targetList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

        constraintList.forEach { constraint ->
            findConstraintNode(constraint, targetList, service, filterPackage)
            if (targetList.isNotEmpty()) {
                result.add(constraint to targetList)
                targetList = mutableListOf()
            }
        }

        if (result.isNotEmpty()) {
            //通过id, 已经找到了目标

            onTargetResult(result)
            return true
        }

        return false
    }

    /**查找满足约束的Node*/
    open fun findConstraintNode(
        constraintBean: ConstraintBean,
        result: MutableList<AccessibilityNodeInfoCompat> = mutableListOf(),
        service: AccessibilityService,
        filterPackage: List<String>?
    ): List<AccessibilityNodeInfoCompat> {
        val rootNodeInfo: AccessibilityNodeInfo =
            service.findNodeInfo(filterPackage).firstOrNull() ?: return result

        rootNodeInfo.getBoundsInScreen(_rootNodeRect)

        val text: List<String>? = constraintBean.textList

        if (text.isListEmpty()) {
            return result
        }
        val packageName: String = idPackageName ?: service.packageName

        val rootNodeWrap: AccessibilityNodeInfoCompat = rootNodeInfo.wrap()

        var tempList: MutableList<AccessibilityNodeInfoCompat>

        //列表中的所有文本是否都匹配通过
        val matchMap = ArrayMap<Int, List<AccessibilityNodeInfoCompat>>()
        for (index: Int in text!!.indices) {
            tempList = mutableListOf()
            try {

                //完整id 是需要包含包名的
                val isIdText: Boolean = constraintBean.idList?.getOrNull(index) == 1
                val subText: String = if (isIdText) packageName.id(text[index]) else text[index]

                if (!isIdText && subText.isEmpty()) {
                    //text匹配模式下, 空字符串处理
                    tempList.add(rootNodeWrap)
                    matchMap[index] = tempList
                } else {
                    rootNodeWrap.unwrap().findNode(tempList) { nodeInfoCompat ->
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
                            findNode = if (nodeInfoCompat.haveText(subText)) {
                                1
                            } else {
                                -1
                            }
                        }

                        if (findNode == 1) {
                            findNode = if (match(constraintBean, nodeInfoCompat, index)) {
                                //其他约束匹配成功
                                if (!constraintBean.pathList.isNullOrEmpty()) {
                                    parseConstraintPath(
                                        constraintBean,
                                        constraintBean.pathList?.getOrNull(index),
                                        nodeInfoCompat,
                                        tempList
                                    )
                                    -1
                                } else {
                                    1
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

        return result
    }

    /**其他规则匹配*/
    open fun match(
        constraintBean: ConstraintBean,
        node: AccessibilityNodeInfoCompat,
        index: Int
    ): Boolean {
        val cls = constraintBean.clsList?.getOrNull(index)
        val rect = constraintBean.rectList?.getOrNull(index)

        //是否匹配成功
        var result = false

        if (!cls.isNullOrEmpty() && !cls.contains(node.className)) {
            //但是类名不同
            result = false
        } else {
            //类名命中

            if (rect != null) {
                //坐标约束

                val bound = node.bounds()

                if (node.isValid()) {
                    //如果设置了矩形匹配规则, 那么这个node的rect一定要是有效的
                    rect.let {
                        if (it.isEmpty()) {
                            //空字符只要宽高大于0, 就命中
                            result = node.isValid()
                        } else {

                            it.split("-").apply {
                                val p1 = getOrNull(0)?.toPointF(
                                    _rootNodeRect.width(),
                                    _rootNodeRect.height()
                                )
                                val p2 = getOrNull(1)?.toPointF(
                                    _rootNodeRect.width(),
                                    _rootNodeRect.height()
                                )

                                if (p1 == null && p2 == null) {
                                } else {
                                    if (p2 == null) {
                                        //只设置了单个点
                                        if (bound.contains(p1!!.x.toInt(), p1.y.toInt())) {
                                            result = true
                                        }
                                    } else {
                                        //设置了多个点
                                        if (bound.contains(
                                                p1!!.x.toInt(),
                                                p1.y.toInt(),
                                                p2.x.toInt(),
                                                p2.y.toInt()
                                            )
                                        ) {
                                            result = true
                                        }
                                    }
                                }
                            }
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
            val state = constraintBean.stateList
            if (!state.isListEmpty()) {
                var match = true
                state!!.forEach {
                    when (it) {
                        ConstraintBean.STATE_CLICKABLE -> {
                            //需要具备可以点击的状态
                            if (!node.isClickable) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_UNCLICKABLE -> {
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
                    }
                }

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
}