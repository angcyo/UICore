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
     * 支持随机 [com.angcyo.acc2.action.Action.RANDOM]
     * [random:5] 随机获取5个
     * 匹配顺序:1*/
    var index: String? = null,

    /**如果元素包含指定的元素, 集合中满足一项即可.
     * 匹配顺序:2*/
    var containList: List<FindBean>? = null,

    /**如果元素不包含指定的元素, 集合中满足一项即可.
     * 匹配顺序:3*/
    var notContainList: List<FindBean>? = null,

    /**过滤一下只满足矩形的条件的元素 , 集合中满足一项即可.
     * 匹配顺序:4*/
    var rectList: List<String>? = null,

    /**如果[androidx.core.view.accessibility.AccessibilityNodeInfoCompat]节点的文本, 在此列表中.
     * 则忽略此节点
     * 支持文本变量, 支持文本使用[com.angcyo.acc2.action.Action.TEXT_SPLIT]分割
     * 匹配顺序:5
     * */
    var ignoreTextList: List<String>? = null,

    /**如果[androidx.core.view.accessibility.AccessibilityNodeInfoCompat]节点的文本, 不在此列表中.
     * 则忽略此节点
     * 支持文本变量, 支持文本使用[com.angcyo.acc2.action.Action.TEXT_SPLIT]分割
     * 匹配顺序:6
     * */
    var haveTextList: List<String>? = null,

    /**节点直系child数量的条件判断
     * 格式如下:
     * [>=2] 数量大于等于2
     * [>3] 数量大于3
     * [2] 数量等于2
     * [=2] 数量等于2
     * [<3] 数量小于3
     * 空字符 表示直接过
     * null 忽略此条件
     * 匹配顺序:7
     * */
    var childCount: String? = null,

    /**节点视图结构中, 无child节点的节点总数量满足此条件, 则继续. 否则失败.
     * 支持的数据格式参考[childCount]
     * 如果包含[*]的格式, 表示所有节点总数量. 如"<=10*" ">=20*"
     * 否则就是无child节点的节点总数量
     * 匹配顺序:8
     * */
    var sizeCount: String? = null,

    //<editor-fold desc="后处理">

    /**再次过滤*/
    var after: FilterBean? = null

    //</editor-fold desc="后处理">
)