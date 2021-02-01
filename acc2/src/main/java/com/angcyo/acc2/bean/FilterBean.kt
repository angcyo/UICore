package com.angcyo.acc2.bean

/**
 * 对结果进行过滤
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class FilterBean(
    /**获取一组元素中指定索引位置的元素
     * 支持范围[0~-1]第一个到倒数第一个
     * 支持文本变量
     * 匹配顺序:1*/
    var index: String? = null,

    /**如果元素包含指定的元素, 集合中满足一项即可.
     * 匹配顺序:2*/
    var containList: List<FindBean>? = null,

    /**如果元素不包含指定的元素, 集合中满足一项即可.
     * 匹配顺序:3*/
    var notContainList: List<FindBean>? = null,
)