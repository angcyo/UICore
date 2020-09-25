package com.angcyo.core.component.accessibility.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * [com.angcyo.core.component.accessibility.action.AutoParser.parse]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/19
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ParseResult(

    /**当前解析使用的约束条件*/
    var constraint: ConstraintBean,

    /**所有的[constraint], 用于激活/禁用*/
    var constraintList: List<ConstraintBean>,

    /**匹配到的节点集合*/
    var nodeList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf(),

    /**条件筛选过滤后的节点集合.
     * 如果为null, 表示没有开启条件过滤
     * */
    var conditionNodeList: MutableList<AccessibilityNodeInfoCompat>? = null,

    /**是否是[notTextList]匹配的结果*/
    var notTextMatch: Boolean = false
)

/**是否开启了筛选条件*/
fun ParseResult.isHaveCondition() = conditionNodeList != null

/**返回需要处理的节点列表*/
fun ParseResult.resultHandleNodeList() =
    if (notTextMatch) nodeList else if (isHaveCondition()) conditionNodeList else nodeList