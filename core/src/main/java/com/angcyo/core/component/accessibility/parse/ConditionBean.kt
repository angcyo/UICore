package com.angcyo.core.component.accessibility.parse

/**
 * 约束条件获取到的node list, 还需要通过条件过滤掉一些不符合条件的node
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/19
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

data class ConditionBean(
    /**节点child数量的条件判断
     * [>=2] 数量大于等于2
     * [>3] 数量大于3
     * [2] 数量等于2
     * [=2] 数量等于2
     * [<3] 数量小于3
     * 空字符 表示直接过
     * null 忽略此条件
     * */
    var childCount: String? = null,

    /**节点中, 必须包含指定的所有文本
     * null 忽略此条件*/
    var containsText: List<String>? = null,

    /**节点中, 不包含指定的所有文本
     * null 忽略此条件*/
    var notContainsText: List<String>? = null
)