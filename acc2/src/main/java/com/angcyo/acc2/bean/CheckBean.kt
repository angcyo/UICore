package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class CheckBean(
    /**check的id, 不参与识别逻辑*/
    var checkId: Long = -1,

    /**check的标题, 不参与识别逻辑*/
    var title: String? = null,

    /**check的描述, 不参与识别逻辑*/
    var des: String? = null,

    /**在那个[android.view.accessibility.AccessibilityWindowInfo]中获取节点
     * 不指定, 则根据包名默认处理, 包名还未指定, 则使用活跃的窗口
     * 优先级:[FindBean->CheckBean->ActionBean]*/
    var window: WindowBean? = null,

    /**如果包含目标元素
     * 所有集合中, 选中的元素都会收集在一起*/
    var event: List<FindBean>? = null,

    /**如果未包含目标元素时, 需要进行的操作*/
    var other: List<HandleBean>? = null,

    /**包含目标元素或者[event]为空的情况下,目标元素需要进行的操作*/
    var handle: List<HandleBean>? = null,

    /**如果[handle]处理失败后, 需要进行的操作*/
    var fail: List<HandleBean>? = null,

    /**如果[handle]处理成功后, 需要进行的操作*/
    var success: List<HandleBean>? = null,

    /**运行次数超限后触发
     * [com.angcyo.acc2.bean.ActionBean.limitRunCount]*/
    var limitRun: List<HandleBean>? = null,

    /**运行时长超限后触发
     * [com.angcyo.acc2.bean.ActionBean.limitRunTime]
     * [limitTime]处理成功后, 会继续后续处理, 失败则跳过[ActionBean]的执行*/
    var limitTime: List<HandleBean>? = null,
)
