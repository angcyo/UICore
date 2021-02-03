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
     * 不指定, 则根据包名默认处理, 包名还未指定, 则使用活跃的窗口*/
    var window: WindowBean? = null,

    //<editor-fold desc="选择器">

    /**单独存在时, 通过文本获取节点, 集合中满足一条即可.
     * 支持文本变量 从[TaskBean]中获取值
     * 支持正则
     * 匹配顺序:1
     * */
    var textList: List<String>? = null,

    /**单独存在时, 通过类名获取节点, 集合中满足一条即可.
     * 同时存在时, 会进行一一对应满足匹配
     * 支持正则
     * 匹配顺序:2
     * */
    var clsList: List<String>? = null,

    /**单独存在时, 通过id获取节点, 集合中满足一条即可.
     * 同时存在时, 会进行一一对应满足匹配
     * 匹配顺序:3
     * */
    var idList: List<String>? = null,

    /**单独存在时, 通过节点矩形获取节点, 集合中满足一条即可.
     * 同时存在时, 会进行一一对应满足匹配
     * [l:>10 r:<10 w:>=10 h:<=10 w:≈10 a:10dp]
     * [l:>10 l:<20] l大于10并且小于20
     * 匹配顺序:4
     * */
    var rectList: List<String>? = null,

    /**单独存在时, 通过节点状态获取节点, 集合中满足一条即可.
     * 同时存在时, 会进行一一对应满足匹配
     * [clickable] 具备可点击
     * [unclickable] 具备不可点击
     * [focusable] 具备可获取交点
     * [selected] 具备选中状态
     * [unselected] 具备选未中状态
     * [focused] 具备焦点状态
     * [unfocused] 具备无焦点状态
     * 匹配顺序:5
     * */
    var stateList: List<String>? = null,

    //</editor-fold desc="选择器">

    //<editor-fold desc="约束器">

    /**
     * 单独存在时, 通过节点满足指定child约束获取节点, 节点的child必须一一对应满足[childList].
     * 同时存在时, 所有匹配到的节点都必须满足约束
     * 匹配顺序:6
     * */
    var childList: List<ChildBean>? = null,

    /**单独存在时, 从跟节点开始选择元素
     * 同时存在时, 会进行一一对应解析替换
     * 约束路径, 通过上述条件找到node之后, 再使用路径查找到真正的目标. 满足集合中一条即返回
     * 格式: +1 -2 >3 <4
     * [+1] 获取自身的下1个兄弟节点
     * [-2] 获取自身的上2个兄弟节点
     * [>3] 获取第3个子节点, 非索引(index)值
     * [<4] 获取第4个parent
     * [<4clickable] 获取第4个具有clickable状态的parent
     * [<4clickable|focusable]
     * 匹配顺序:7
     * */
    var pathList: List<String>? = null,

    //</editor-fold desc="约束器">

    //<editor-fold desc="筛选器">

    /**
     * 取节点中, 对应的索引, 等同于[com.angcyo.acc2.bean.FilterBean.index]
     * 支持文本变量
     * */
    var index: String? = null,

    /**
     * 取节点中, 对应的child节点
     * [com.angcyo.acc2.bean.FilterBean.index]
     * 支持文本变量
     * */
    var childIndex: String? = null,

    //</editor-fold desc="筛选器">

    //----------------------------------------------------------------

    /**根据过滤条件, 过滤一层*/
    var filter: FilterBean? = null,

    /**根据本次选择元素列表, 继续查找子子元素*/
    var after: FindBean? = null
)
