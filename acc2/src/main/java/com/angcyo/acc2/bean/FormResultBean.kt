package com.angcyo.acc2.bean

/**
 * 表单请求返回的数据结构信息, 用于参与AccControl流程控制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class FormResultBean(

    /**替换任务中的[wordList]
     * [com.angcyo.acc2.bean.TaskBean.wordList]*/
    var wordList: List<String?>? = null,

    /**需要额外执行的[ActionBean]
     * [com.angcyo.acc2.bean.TaskBean.actionList]*/
    var actions: List<ActionBean>? = null,

    /**需要处理的[HandleBean]
     * 处理结果, 将会影响主流程
     *[com.angcyo.acc2.bean.CheckBean.handle]*/
    var handleList: List<HandleBean>? = null,

    /**需要执行的指令
     * 执行结果, 将会影响主流程
     *[com.angcyo.acc2.bean.HandleBean.actionList]*/
    var actionList: List<String>? = null
)