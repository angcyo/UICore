package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class FindBean(

    /**标识配置*/
    var key: String? = null,

    /**标识配置*/
    var des: String? = null,

    /**标识配置, 拾取文本时的正则
     * 支持配置 decode:true 参数, 必须放在字符串最后面, 用空格隔开
     * [com.angcyo.acc2.action.PutTextAction]*/
    var regex: String? = null,

    /**标识配置, 拾取文本时, 追加到列表中, 否则默认就是取最后一个文本的文本 */
    var putArray: Boolean? = null,

    //<editor-fold desc="上下文">

    /**在那个[AccessibilityWindowInfo]中获取节点
     * 不指定, 则根据包名默认处理, 包名还未指定, 则使用活跃的窗口
     * 优先级:[FindBean->CheckBean->ActionBean]*/
    var window: WindowBean? = null,

    //</editor-fold desc="上下文">

    //<editor-fold desc="激活条件">

    /**只有满足条件的[FindBean]才能被执行, 不满足条件会跳过执行,
     * 有一个条件满足即可*/
    var conditionList: List<ConditionBean>? = null,

    //</editor-fold desc="激活条件">

    //<editor-fold desc="选择器">

    /**集合匹配条件是否是或者的关系.
     * 如果是或者的关系, 则集合中匹配任意一项命中即可,
     * 否则需要全部匹配才行.*/
    var or: Boolean = false,

    /**单独存在时, 通过文本获取节点, 所有文本都命中节点时才有效.
     * 同时存在时, 需要一一对应全部满足匹配
     * 支持文本变量 从[TaskBean]中获取值
     * 支持正则
     * 匹配顺序:1
     * 特殊变量[com.angcyo.acc2.action.Action.APP_NAME],任务包名对应的程序名
     * */
    var textList: List<String?>? = null,

    /**单独存在时, 通过类名获取节点, 所有类名都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * 支持正则
     * 匹配顺序:2
     * */
    var clsList: List<String?>? = null,

    /**单独存在时, 通过id获取节点, 所有id都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * 匹配顺序:3
     * */
    var idList: List<String?>? = null,

    /**单独存在时, 通过节点矩形获取节点, 所有矩形都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * [l:>10 r:<10 w:>=10 h:<=10 w:≈10 a:10dp]
     * [l:>10 l:<20] l大于10并且小于20
     * [l:>0.01 l:<0.99] 比例, 支持同一变量的多个表达式判断
     * [l:>1.02r] 强制使用比例
     * 匹配顺序:4
     * */
    var rectList: List<String?>? = null,

    /**单独存在时, 通过节点状态获取节点, 所有状态都命中节点时才有效.
     * 同时存在时, 会进行一一对应满足匹配
     * [clickable longClickable] 具备可点击, 具备可长按. 用空格分割多个
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
    var stateList: List<String?>? = null,

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
    var childList: List<NodeBean>? = null,

    /**单独存在时, 从跟节点开始选择元素
     * 同时存在时, 则会从上述匹配的节点,一一解析对应path进行新节点替换
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
     * 根据上述查找到的节点, 取满足条件对应的父节点.
     * 直到匹配通过为止, 否则返回null
     * 如果是空节点约束, 则直接使用root节点集合*/
    var parent: NodeBean? = null,

    /**3.x
     * 根据上述查找到的节点, 取满足条件对应的子节点.
     * 直到匹配通过为止, 否则返回null*/
    var child: NodeBean? = null,

    /**4:
     * 根据上述查找到的节点, 获取所有的直系child子节点*/
    var allChild: Boolean? = null,

    /**5:
     * 根据上述查找到的节点,递归parent节点, 直到找到了符合条件的节点.
     * */
    var upList: List<FindBean>? = null,

    //</editor-fold desc="筛选器">

    //<editor-fold desc="过滤">

    /**根据过滤条件, 过滤一层*/
    var filter: FilterBean? = null,

    /**
     * 如果上面查找到, 过滤后的节点总数量满足指定条件时, 则返回找到的节点集合.
     * 否则, 清空节点集合并返回
     * [com.angcyo.acc2.bean.FilterBean.sizeCount]*/
    var findNodeCount: String? = null,

    //</editor-fold desc="过滤">

    //<editor-fold desc="后处理">

    /**遍历上述找到的节点列表, 替换成[FindBean]找到的新节点*/
    var each: List<FindBean>? = null,

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
