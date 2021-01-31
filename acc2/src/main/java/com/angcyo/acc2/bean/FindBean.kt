package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class FindBean(

    /**在那个[AccessibilityWindowInfo]中获取节点
     * 不指定, 则根据包名默认处理*/
    var window: WindowBean? = null,

    //<editor-fold desc="选择器">

    /**单独存在时, 通过文本获取节点, 集合中满足一条即可.
     * 支持变量从[TaskBean]中获取值
     * 支持正则
     * */
    var textList: List<String>? = null,

    /**单独存在时, 通过类名获取节点, 集合中满足一条即可.
     * 批量存在时, 会进行一一对应满足匹配
     * 支持正则
     * */
    var clsList: List<String>? = null,

    /**单独存在时, 通过id获取节点, 集合中满足一条即可.
     * 批量存在时, 会进行一一对应满足匹配
     * */
    var idList: List<String>? = null,

    /**单独存在时, 通过节点矩形获取节点, 集合中满足一条即可.
     * 批量存在时, 会进行一一对应满足匹配
     * [l:>10 r:<10 w:>=10 h:<=10 w:≈10]
     * */
    var rectList: List<String>? = null,

    /**单独存在时, 通过节点状态获取节点, 集合中满足一条即可.
     * 批量存在时, 会进行一一对应满足匹配
     * [clickable] 具备可点击
     * [unclickable] 具备不可点击
     * [focusable] 具备可获取交点
     * [selected] 具备选中状态
     * [unselected] 具备选未中状态
     * [focused] 具备焦点状态
     * [unfocused] 具备无焦点状态
     * */
    var stateList: List<String>? = null,

    //</editor-fold desc="选择器">

    //<editor-fold desc="约束器">

    //</editor-fold desc="约束器">

)
