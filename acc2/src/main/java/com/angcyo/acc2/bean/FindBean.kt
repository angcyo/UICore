package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class FindBean(

    //<editor-fold desc="上下文">

    /**在那个[AccessibilityWindowInfo]中获取节点
     * 不指定, 则根据包名默认处理, 包名还未指定, 则使用活跃的窗口*/
    var window: WindowBean? = null,

    //</editor-fold desc="上下文">

    //<editor-fold desc="激活条件">

    /**只有满足条件的[FindBean]才能被执行, 不满足条件会跳过执行,
     * 有一个条件满足即可*/
    var conditionList: List<ConditionBean>? = null,

    //</editor-fold desc="激活条件">

    //<editor-fold desc="选择器">

    /**单独存在时, 通过文本获取节点, 所有文本都命中节点时才有效.
     * 同时存在时, 需要一一对应全部满足匹配
     * 支持文本变量 从[TaskBean]中获取值
     * 支持正则
     * 匹配顺序:1
     * 特殊变量[appName],任务包名对应的程序名
     * */
    var textList: List<String>? = null,

    /**单独存在时, 通过类名获取节点, 所有类名都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * 支持正则
     * 匹配顺序:2
     * */
    var clsList: List<String>? = null,

    /**单独存在时, 通过id获取节点, 所有id都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * 匹配顺序:3
     * */
    var idList: List<String>? = null,

    /**单独存在时, 通过节点矩形获取节点, 所有矩形都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * [l:>10 r:<10 w:>=10 h:<=10 w:≈10 a:10dp]
     * [l:>10 l:<20] l大于10并且小于20
     * [l:>0.01 l:<0.99] 比例
     * [l:>1.02r] 强制使用比例
     * 匹配顺序:4
     * */
    var rectList: List<String>? = null,

    /**单独存在时, 通过节点状态获取节点, 所有状态都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * [clickable] 具备可点击
     * [unclickable] 具备不可点击
     * [focusable] 具备可获取交点
     * [selected] 具备选中状态
     * [unselected] 具备选未中状态
     * [focused] 具备焦点状态
     * [unfocused] 具备无焦点状态
     *
     * [unselected:5*] 5个parent节点之内都满足状态才行
     * 如果包含[*], 表示所有节点都必须满足状态条件, 否则只要有一个满足状态条件即可
     *
     * 所有状态:
     * [com.angcyo.acc2.action.Action.STATE_CLICKABLE]
     * [com.angcyo.acc2.action.Action.STATE_UNSELECTED]
     * 匹配顺序:5
     * */
    var stateList: List<String>? = null,

    //</editor-fold desc="选择器">

    //<editor-fold desc="查询效率">

    /**[com.angcyo.acc2.parse.AccContext.findLimit]*/
    var findLimit: String? = null,

    /**[com.angcyo.acc2.parse.AccContext.findDepth]*/
    var findDepth: String? = null,

    //</editor-fold desc="查询效率">


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

    /**1:
     * 取节点中, 对应的索引, 等同于[com.angcyo.acc2.bean.FilterBean.index]
     * 支持文本变量
     * */
    var index: String? = null,

    /**2:
     * 取节点中, 对应的child节点
     * [com.angcyo.acc2.bean.FilterBean.index]
     * 支持文本变量
     * */
    var childIndex: String? = null,

    /**3:
     * 根据上述查找到的节点, 取满足条件对应的父节点*/
    var parent: ChildBean? = null,

    //</editor-fold desc="筛选器">

    //<editor-fold desc="过滤">

    /**根据过滤条件, 过滤一层*/
    var filter: FilterBean? = null,

    //</editor-fold desc="过滤">

    //<editor-fold desc="后处理">

    /**临时使用这些查询到的节点
     * 强制结束/强制成功会影响[com.angcyo.acc2.bean.CheckBean.handle]的流程处理
     * 直接返回的处理结果暂时未处理*/
    var use: List<HandleBean>? = null,

    //</editor-fold desc="后处理">

    //----------------------------------------------------------------

    /**无论上述选择器有没有选中元素, 都进行[after]选择.
     * 默认只在上述选择器选中元素之后, 才会进行[after]*/
    var afterAlways: Boolean = false,

    /**根据本次选择元素列表, 继续查找子子元素*/
    var after: FindBean? = null
)
