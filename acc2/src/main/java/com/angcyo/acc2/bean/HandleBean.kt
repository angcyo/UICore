package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class HandleBean(

    //<editor-fold desc="激活条件">

    /**是否激活当前的[HandleBean]*/
    var enable: Boolean = true,

    /**
     * 满足其中一个条件,[HandleBean]才会被执行*/
    var conditionList: List<ConditionBean>? = null,

    //</editor-fold desc="激活条件">

    //<editor-fold desc="前处理">

    /**处理[findList]时,需要使用的根节点
     * [com.angcyo.acc2.action.Action.RESULT] 使用[com.angcyo.acc2.bean.CheckBean.event]返回的节点,进行后续处理
     * [null] 使用默认的[com.angcyo.acc2.parse.FindParse.rootWindowNode]节点*/
    var rootNode: String? = null,

    /**重新选择新的元素, 否则直接使用[com.angcyo.acc2.bean.CheckBean.event]获取到的元素
     * 当指定了[findList], 又想使用[com.angcyo.acc2.bean.CheckBean.event]的节点时, 设置[rootNode]为[com.angcyo.acc2.action.Action.RESULT]*/
    var findList: List<FindBean>? = null,

    /**过滤目标元素, 只处理之后的元素*/
    var filter: FilterBean? = null,

    /**
     * 取过滤后的第几个节点进行操作, 参考[com.angcyo.acc2.bean.FilterBean.index]
     * 支持[com.angcyo.acc2.action.Action.DEF]
     * */
    var index: String? = null,

    //</editor-fold desc="前处理">

    //<editor-fold desc="后处理">

    /**不管执行有没有成功, 都返回[false]
     * 优先处理此属性*/
    var ignore: Boolean = false,

    /**不管执行是否成功, 都跳过之后的[HandleBean]处理*/
    var jump: Boolean = false,

    /**当执行成功后, 跳过之后的[HandleBean]处理*/
    var jumpOnSuccess: Boolean = false,

    //</editor-fold desc="后处理">

    //<editor-fold desc="处理动作">

    /**处理动作之前要处理的[HandleBean]
     * 直接返回值, 不会影响流程*/
    var handleBefore: List<HandleBean>? = null,

    /**如果满足case, 则使用[CaseBean]中的[com.angcyo.acc2.bean.CaseBean.actionList]替换.
     * 满足一个即返回*/
    var caseList: List<CaseBean>? = null,

    /**文本特殊处理参数, 如长尾词替换处理
     * [com.angcyo.acc2.bean.HandleBean.textParam]
     * [com.angcyo.acc2.bean.TaskBean.textParam]
     * [com.angcyo.acc2.bean.CaseBean.textParam]
     * */
    var textParam: TextParamBean? = null,

    /**
     * 当有元素选择时需要执行的具体操作
     * [com.angcyo.acc2.action.Action]*/
    var actionList: List<String>? = null,

    /**
     * 当[conditionList]不满足时, 需要执行的具体操作.如果未指定, 则跳过当前的[HandleBean]
     * [com.angcyo.acc2.action.Action]*/
    var conditionActionList: List<String>? = null,

    /**
     * 当没有元素选择时需要执行的具体操作, 如果未指定则继续使用[actionList]
     * [com.angcyo.acc2.action.Action]*/
    var noActionList: List<String>? = null,

    /**[actionList] [noActionList] 执行失败时, 需要执行的动作. 不受[ignore]影响*/
    var failActionList: List<String>? = null,

    /**[actionList] [noActionList] 执行成功时, 需要执行的动作. 不受[ignore]影响*/
    var successActionList: List<String>? = null,

    /**处理动作之后要处理的[HandleBean]
     * 直接返回值, 不会影响流程*/
    var handleAfter: List<HandleBean>? = null,

    //</editor-fold desc="处理动作">

    //<editor-fold desc="操作记录">

    /**如果指定了操作记录, 那么会回调[com.angcyo.acc2.control.ControlListener.onHandleOperate]
     * 将会在[HandleBean]处理结束之后执行*/
    var operate: OperateBean? = null

    //</editor-fold desc="操作记录">
)
