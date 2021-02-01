package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ActionBean(

    /**标识[ActionBean]*/
    var actionId: Long = 0,

    /**操作的标题*/
    var title: String? = null,

    /**操作的描述*/
    var des: String? = null,

    /**是否激活[ActionBean], 未激活直接跳过执行*/
    var enable: Boolean = true,

    /**在那个[AccessibilityWindowInfo]中获取节点
     * 不指定, 则根据包名默认处理, 包名还未指定, 则使用活跃的窗口*/
    var window: WindowBean? = null,

    /**在[enable=true]的情况下, 额外需要判断的运行条件
     * 只有满足条件的[ActionBean]才能被执行, 不满足条件等同于[enable]为false,
     * 有一个条件满足即可*/
    var conditionList: List<ConditionBean>? = null,

    /**如果[conditionList]条件满足时, 也激活处理*/
    var autoEnable: Boolean = false,

    /**
     * 控制自身执行启动的延迟
     * [com.angcyo.acc2.parse.AccParse.parseTime]
     * */
    var start: String? = null,

    /**元素解析*/
    var check: CheckBean? = null,

    /**未指定[check]时, 可以通过[checkId]在[check]库中根据id查找对应的[CheckBean]*/
    var checkId: Long = -1,

    /**[ActionBean]执行之前, 需要提前执行的[ActionBean]
     * 如果处理成功了, 会中断原本需要执行的[ActionBean]*/
    var before: ActionBean? = null,

    /**在[ActionBean]执行之后, 需要执行的[ActionBean]*/
    var after: ActionBean? = null,

    /**当前的[ActionBean]依靠的其他[ActionBean]的id,
     * 可用于[com.angcyo.acc2.action.Action.ACTION_JUMP]跳转指令使用*/
    var relyList: List<Long>? = null,

    /**异步执行[ActionBean], 非主线[ActionBean]时有效*/
    var async: Boolean = false
)
