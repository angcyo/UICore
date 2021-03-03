package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ActionBean(

    //<editor-fold desc="配置">

    /**标识[ActionBean]*/
    var actionId: Long = -1,

    /**操作的标题*/
    var title: String? = null,

    /**操作的概要*/
    var summary: String? = null,

    /**操作的描述*/
    var des: String? = null,

    /**标识一下[ActionBean]*/
    var type: String? = null,

    /**异步执行[ActionBean], 不指定时, 根据情况.自动选择
     * [com.angcyo.acc2.bean.TaskBean.backActionList]默认开始异步, 提高效率
     * */
    var async: Boolean? = null,

    /**
     * 控制自身执行启动的延迟
     * [com.angcyo.acc2.parse.AccParse.parseTime]
     * */
    var start: String? = null,

    /**调试模式下的时间*/
    var debugStart: String? = null,

    /**当前的[ActionBean]依靠的其他[ActionBean]的id,
     * 可用于[com.angcyo.acc2.action.Action.ACTION_JUMP]跳转指令使用*/
    var relyList: List<Long>? = null,

    /**限制最大运行次数后, 异常中断任务.
     * 可以使用-1,取消限制.
     * 可以使用[com.angcyo.acc2.bean.CheckBean.limitRun]覆盖默认行为
     * */
    var limitRunCount: Int = -1,

    /**限制最大运行时长后, 异常中断任务.
     * 可以使用-1,取消限制.
     * 可以使用[com.angcyo.acc2.bean.CheckBean.limitTime]覆盖默认行为
     * */
    var limitRunTime: Int = -1,

    //</editor-fold desc="配置">

    /**在那个[AccessibilityWindowInfo]中获取节点
     * 不指定, 则根据包名默认处理, 包名还未指定, 则使用活跃的窗口*/
    var window: WindowBean? = null,

    //<editor-fold desc="激活">

    /**是否激活[ActionBean], 未激活直接跳过执行*/
    var enable: Boolean = true,

    //记录原始的[enable]状态,[randomEnable]随机激活状态下使用
    @Transient
    var _enable: Boolean? = null,

    /**在[enable=true]的情况下, 额外需要判断的运行条件
     * 只有满足条件的[ActionBean]才能被执行, 不满足条件等同于[enable]为false,
     * 有一个条件满足即可*/
    var conditionList: List<ConditionBean>? = null,

    /**如果[conditionList]条件满足时, 也激活处理*/
    var autoEnable: Boolean = false,

    /**需要先[enable]才会有,随机激活[ActionBean]*/
    var randomEnable: Boolean = false,

    /**指定随机的概率
     * [0-100]
     * [=1] 0.01的概率
     * [<=30] 30%的概率*/
    var randomAmount: String? = null,

    /**[ActionBean]分组, 最优先判断, 只有group判断通过之后, 才会执行剩下的激活逻辑判断.
     * 只有相同分组中的第一个激活, 自身才激活,否则跳过.
     * 多个分组使用[com.angcyo.acc2.action.Action.PACKAGE_SPLIT]分割*/
    var group: String? = null,

    //</editor-fold desc="激活">

    /**未指定[check]时, 可以通过[checkId]在[check]库中根据id查找对应的[CheckBean]*/
    var checkId: Long = -1,

    /**元素解析*/
    var check: CheckBean? = null,

    /**[ActionBean]执行之前, 需要提前执行的[ActionBean]
     * 如果处理成功了, 会中断原本需要执行的[ActionBean]*/
    var before: ActionBean? = null,

    /**在[ActionBean]执行之后, 需要执行的[ActionBean]*/
    var after: ActionBean? = null
)
