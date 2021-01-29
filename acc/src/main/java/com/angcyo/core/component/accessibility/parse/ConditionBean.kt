package com.angcyo.core.component.accessibility.parse

/**
 * 约束条件获取到的node list, 还需要通过条件过滤掉一些不符合条件的node
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/19
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

data class ConditionBean(

    /**是否使用根节点判断, 否则则使用约束后的节点. 注意性能*/
    var root: Boolean = false,

    /**节点child数量的条件判断
     * 格式如下:
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
    @Deprecated("废弃:请使用[check]")
    var containsText: List<String>? = null,

    /**节点中, 不包含指定的所有文本
     * null 忽略此条件*/
    @Deprecated("废弃:请使用[check]")
    var notContainsText: List<String>? = null,

    /**[check]的操作条件.
     * [is]
     * [not]*/
    var op: String? = null,

    /**仅具备条件约束的[ConstraintBean], 需要先设置[op]*/
    var check: ConstraintBean? = null,

    /**所有的[ConstraintBean]都符合[op]*/
    var checkList: List<ConstraintBean>? = null
) {
    companion object {
        /**节点必须满足[check]条件约束才行*/
        const val OP_IS = "is"

        /**节点必须不满足[check]条件约束才行*/
        const val OP_NOT = "not"
    }
}