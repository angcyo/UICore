package com.angcyo.core.component.accessibility.action

import android.accessibilityservice.AccessibilityService
import androidx.collection.ArrayMap
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.parse.ParseBean
import com.angcyo.library.ex.isListEmpty

/**
 * 解析处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParse {

    /**返回当前界面, 是否包好[bean]指定的标识信息
     * [onTargetResult] 当找到目标时, 通过此方法回调目标给调用者. first:对应的约束, second:约束对应的node集合
     * */
    open fun parse(
        service: AccessibilityService,
        bean: ParseBean,
        onTargetResult: (List<Pair<ConstraintBean, List<AccessibilityNodeInfoCompat>>>) -> Unit = {}
    ): Boolean {

        val result: MutableList<Pair<ConstraintBean, List<AccessibilityNodeInfoCompat>>> =
            mutableListOf()

        var targetList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

        //优先判断 id
        bean.ids?.forEach { paramConstraint ->
            findConstraintNode(paramConstraint, targetList, true, service)
            if (targetList.isNotEmpty()) {
                result.add(paramConstraint to targetList)
                targetList = mutableListOf()
            }
        }

        if (result.isNotEmpty()) {
            //通过id, 已经找到了目标

            onTargetResult(result)
            return true
        }

        //其次判断 text
        bean.texts?.forEach { paramConstraint ->
            findConstraintNode(paramConstraint, targetList, false, service)
            if (targetList.isNotEmpty()) {
                result.add(paramConstraint to targetList)
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
        isIdText: Boolean = false, //完整id 是需要包含包名的
        service: AccessibilityService
    ): List<AccessibilityNodeInfoCompat> {
        val rootNodeInfo = service.rootNodeInfo() ?: return result

        val text = constraintBean.text

        if (text.isListEmpty()) {
            return result
        }
        val packageName: String = service.packageName

        val rootNodeWrap: AccessibilityNodeInfoCompat = rootNodeInfo.wrap()

        var tempList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

        //列表中的所有文本是否都匹配通过
        val matchMap = ArrayMap<Int, List<AccessibilityNodeInfoCompat>>()
        for (index: Int in text!!.indices) {
            tempList = mutableListOf()
            try {
                val subText: String = if (isIdText) packageName.id(text[index]) else text[index]
                if (!isIdText && subText.isEmpty()) {
                    //text匹配模式下, 空字符串处理
                    tempList.add(rootNodeWrap)
                    matchMap[index] = tempList
                } else {
                    rootNodeWrap.unwrap().findNode(tempList) {
                        if (isIdText) {
                            //id 全等匹配
                            val idName = it.viewIdName()
                            if (subText == idName) {
                                if (match(constraintBean, it, index)) {
                                    1
                                } else {
                                    -1
                                }
                            } else {
                                -1
                            }
                        } else {
                            //文本包含匹配
                            if (it.haveText(subText)) {
                                if (match(constraintBean, it, index)) {
                                    1
                                } else {
                                    -1
                                }
                            } else {
                                -1
                            }
                        }
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
        val cls = constraintBean.cls?.getOrNull(index)
        return if (!cls.isNullOrEmpty() && !cls.contains(node.className)) {
            //但是类名不同
            false
        } else {
            //类名命中
            if (!constraintBean.rect.isListEmpty()) {
                //坐标约束

                //是否匹配通过
                var matchRect = false

                val bound = node.bounds()

                if (node.isValid()) {
                    //如果设置了矩形匹配规则, 那么这个node的rect一定要是有效的
                    constraintBean.rect?.forEach {
                        if (it.isEmpty()) {
                            //空字符只要宽高大于0, 就命中
                            matchRect = node.isValid()
                        } else {

                            it.split("-").apply {
                                val p1 = getOrNull(0)?.toPointF()
                                val p2 = getOrNull(1)?.toPointF()

                                if (p1 == null && p2 == null) {
                                } else {
                                    if (p2 == null) {
                                        //只设置了单个点
                                        if (bound.contains(p1!!.x.toInt(), p1.y.toInt())) {
                                            matchRect = true
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
                                            matchRect = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                matchRect
            } else {
                //other
                true
            }
        }
    }
}