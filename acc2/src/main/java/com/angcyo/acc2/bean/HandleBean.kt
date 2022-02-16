package com.angcyo.acc2.bean

import com.angcyo.acc2.action.InputAction
import com.angcyo.acc2.dynamic.IHandleDynamic

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

    /**
     * 执行[HandleBean]时的sleep
     * [com.angcyo.acc2.parse.AccParse.parseTime]
     * */
    var wait: String? = null,

    /**是否不解析[findList],
     * 当设置了[handleClsList], 开启此属性, 提高性能*/
    var noFind: Boolean? = null,

    /**处理[findList]时,需要使用的根节点
     * [com.angcyo.acc2.action.Action.RESULT] 使用[com.angcyo.acc2.bean.CheckBean.event]返回的节点,进行后续处理
     * [null] 使用默认的[com.angcyo.acc2.parse.FindParse.rootWindowNode]节点*/
    var rootNode: String? = null,

    /**重新选择新的元素, 否则直接使用[com.angcyo.acc2.bean.CheckBean.event]获取到的元素
     * 当指定了[findList], 又想使用[com.angcyo.acc2.bean.CheckBean.event]的节点时, 设置[rootNode]为[com.angcyo.acc2.action.Action.RESULT]*/
    var findList: List<FindBean>? = null,

    /**当前界面不包含[outFindList]查找到的元素时, 成立*/
    var outFindList: List<FindBean>? = null,

    /**记录当前[findList]未找到节点的次数*/
    var _noFindCount: Long = 0,

    /**多少次[findList]未找到节点时, 才处理[noFindHandleList]中的[HandleBean]*/
    var noFindHandleCount: Long = 0,

    /**
     * 当[findList]未找到任何节点时, 需要处理的[HandleBean],
     * 处理结果, 会直接赋给当前的[HandleBean]
     * */
    var noFindHandleList: List<HandleBean>? = null,

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

    /**调试模式下测试专用的[actionList]*/
    var debugActionList: List<String>? = null,

    /**
     * 当有元素选择时需要执行的具体操作
     * [com.angcyo.acc2.action.Action]*/
    var actionList: List<String>? = null,

    /**[handleClsList]参数存储池*/
    var handleClsParams: Map<String, Any>? = null,

    /**直接接管[com.angcyo.acc2.bean.HandleBean]的处理*/
    var handleClsList: List<String>? = null,

    //实例化后的类
    @Transient var _handleObjList: List<IHandleDynamic>? = null,

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

    /**[InputAction]整个集合列表输入结束之后需要执行的操作.
     * 返回结果不影响主流程, 部位跳转命令有效*/
    var handleActionEndActionList: List<String>? = null,

    /**是否成功之后才执行[handleAfter]*/
    var handleAfterOnSuccess: Boolean = false,

    /**处理动作之后要处理的[HandleBean]
     * 直接返回值, 不会影响流程*/
    var handleAfter: List<HandleBean>? = null,

    //</editor-fold desc="处理动作">

    //<editor-fold desc="操作记录">

    /**表单请求
     * [com.angcyo.acc2.bean.OperateBean.form]
     * [com.angcyo.acc2.bean.HandleBean.form]
     * [com.angcyo.acc2.bean.ActionBean.form]
     * [com.angcyo.acc2.bean.TaskBean.form]
     * */
    var form: FormBean? = null,

    /**如果指定了操作记录, 那么会回调[com.angcyo.acc2.control.ControlListener.onHandleOperate]
     * 将会在[HandleBean]处理结束之后执行*/
    var operate: OperateBean? = null

    //</editor-fold desc="操作记录">
)
