package com.angcyo.acc2.bean

import com.angcyo.acc2.action.Action
import com.angcyo.acc2.dynamic.IInputProvider
import com.angcyo.acc2.dynamic.ITaskDynamic
import com.angcyo.library.ex.uuid

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class TaskBean(

    /**额外数据, 存放点
     * 方便在定制化app中使用*/
    var map: HashMap<String, Any?>? = null,

    /**表单请求
     * [com.angcyo.acc2.bean.OperateBean.form]
     * [com.angcyo.acc2.bean.HandleBean.form]
     * [com.angcyo.acc2.bean.ActionBean.form]
     * [com.angcyo.acc2.bean.TaskBean.form]
     * */
    var form: FormBean? = null,

    /**[com.angcyo.acc2.bean.TaskBean.form]
     * [com.angcyo.acc2.action.Action.ACTION_REQUEST_FORM]
     * [com.angcyo.acc2.action.Action.ACTION_FORM]
     * */
    var formMap: Map<String, FormBean?>? = null,

    /**是否初始化了*/
    var _init: Boolean = false,

    /**控制任务的日志
     * [com.angcyo.acc2.bean.ActionBean.log]*/
    var log: Boolean? = null,

    //<editor-fold desc="配置信息">

    /**任务uuid*/
    var uuid: String? = uuid(),

    /**任务id*/
    var taskId: Long = -1,

    /**任务的标题*/
    var title: String? = null,

    /**任务的描述*/
    var des: String? = null,

    /**任务类型, 用于查找匹配使用*/
    var type: String? = null,

    /**是否响应适配信息, 仅在测试页面有效*/
    var adaptive: Boolean = false,

    var enable: Boolean = true,

    /** 判断优先:1
     * 配置用于激活在[actionList]中的[ActionBean],
     * 支持通过[com.angcyo.acc2.bean.ActionBean.actionId]激活
     * 支持通过[com.angcyo.acc2.bean.ActionBean.group]名字, 激活分组中的第一个[ActionBean]
     * 多个用[com.angcyo.acc2.action.Action.PACKAGE_SPLIT]分割*/
    var enableAction: String? = null,

    /**判断优先:2
     * 随机激活[actionList]中的[ActionBean]
     * [enableAction]*/
    var randomEnableAction: String? = null,

    /**判断优先:3
     * 禁用[actionList]中的[ActionBean]
     * [enableAction]*/
    var disableAction: String? = null,

    /**Touch操作时, 需要随机的坐标位置*/
    var touchFrom: Int? = null,

    var touchUntil: Int? = null,

    //</editor-fold desc="配置信息">

    //<editor-fold desc="浮窗配置">

    /**是否使用全屏浮窗提示*/
    var fullscreen: Boolean = false,

    /**浮窗是否不需要拦截手势*/
    var notTouchable: Boolean = false,

    /**显示手势提示框*/
    var showTouchTip: Boolean? = null,

    /**显示节点提示框*/
    var showNodeTip: Boolean? = null,

    //</editor-fold desc="浮窗配置">

    //<editor-fold desc="参数">

    /**任务需要处理那个程序的信息
     * - 多个包名用[com.angcyo.acc2.action.Action.PACKAGE_SPLIT]分割
     * - [*] 表示所有包 [com.angcyo.acc2.action.Action.ALL]
     * - 不支持正则
     * */
    var packageName: String? = null,

    /**统一配置每个[ActionBean]中的[limitRunCount]参数
     * [com.angcyo.acc2.bean.ActionBean.limitRunCount]*/
    var limitRunCount: Int = -1,

    /**统一配置每个[ActionBean]中的[limitRunTime]参数
     * [com.angcyo.acc2.bean.ActionBean.limitRunTime]*/
    var limitRunTime: Long = -1,

    /**限制整个[TaskBean]运行时长
     * [com.angcyo.acc2.bean.ActionBean.limitRunTime]*/
    var taskLimitRunTime: Long = -1,

    /**当[taskLimitRunTime]超出限制时,需要处理的操作.如果未指定,则默认异常处理.
     * [com.angcyo.acc2.bean.CheckBean.limitTime]
     * 无论[taskLimitTime]执行成功与否, 都会终止任务.*/
    var taskLimitTime: ActionBean? = null,

    /**任务完成后, 是否启动主程序.(不管失败或者成功)*/
    var finishToApp: Boolean = true,

    //</editor-fold desc="参数">

    //<editor-fold desc="文本池">

    /**字符串输入供给
     * [$0 ]
     * [$0 $2 $3]
     * [$0~$-1]*/
    var wordList: List<String?>? = null,

    /** 优先级低于[textListMap]
     * 文本池, 供$[xxx] 获取使用
     * [xxx]命令保存的文本, 供后续使用*/
    var textMap: HashMap<String, String?>? = null,

    /**优先从[textListMap]里面取数据,其次从[textMap]取数据*/
    var textListMap: HashMap<String, List<String?>?>? = null,

    /**文本特殊处理参数, 如长尾词替换处理
     * [com.angcyo.acc2.bean.HandleBean.textParam]
     * [com.angcyo.acc2.bean.TaskBean.textParam]
     * [com.angcyo.acc2.bean.CaseBean.textParam]
     * */
    var textParam: TextParamBean? = null,

    //</editor-fold desc="文本池">

    /**操作步骤*/
    var actionList: List<ActionBean>? = null,

    /**[actionList]未处理时的操作步骤*/
    var backActionList: List<ActionBean>? = null,

    /**在每个[ActionBean]执行之前, 都要执行的[ActionBean]
     * 如果处理成功了, 会中断原本需要执行的[ActionBean]
     * [com.angcyo.acc2.bean.ActionBean.before]*/
    var before: ActionBean? = null,

    /**在每个[ActionBean]执行之后, 都要执行的[ActionBean]
     * [com.angcyo.acc2.bean.ActionBean.after]*/
    var after: ActionBean? = null,

    /**每个[ActionBean]执行时, 离开了主程序, 需要执行的[ActionBean]
     * [com.angcyo.acc2.bean.ActionBean.leave]*/
    var leave: ActionBean? = null,

    /**统一设置每个[ActionBean]丢失节点后, 需要执行的[ActionBean]
     * [com.angcyo.acc2.bean.ActionBean.lose]*/
    var lose: ActionBean? = null,

    /**循环间隔执行的[ActionBean]
     * [com.angcyo.acc2.bean.ActionBean.interval]*/
    var intervalList: List<ActionBean>? = null,

    //<editor-fold desc="动态cls监听任务">

    /**任务监听的动态class*/
    var listenerClsList: List<String>? = null,

    //实例化后的类
    @Transient var _listenerObjList: List<ITaskDynamic>? = null,

    //</editor-fold desc="动态cls监听任务">

    // <editor-fold desc="动态cls 文本提供">

    /**文本提供的动态class*/
    var inputProviderClsList: List<String>? = null,

    //实例化后的类
    @Transient var _inputProviderObjList: List<IInputProvider>? = null,

    //</editor-fold desc="动态cls 文本提供">
)

/**初始化配置*/
fun TaskBean.initConfig() {

    val list = mutableListOf(actionList, backActionList)

    for (l in list) {
        l?.let {

            //激活指定的ActionBean
            enableAction?.split(Action.PACKAGE_SPLIT)?.forEach { str ->
                if (str.isNotEmpty()) {
                    str.toLongOrNull()
                        ?.let { id -> findActionById(id, l).forEach { it.enable = true } }
                    findFirstActionByGroup(str, l)?.let { it.enable = true }
                }
            }

            //随机激活指定的ActionBean
            randomEnableAction?.split(Action.PACKAGE_SPLIT)?.forEach { str ->
                if (str.isNotEmpty()) {
                    str.toLongOrNull()
                        ?.let { id -> findActionById(id, l).forEach { it.randomEnable = true } }
                    findFirstActionByGroup(str, l)?.let { it.randomEnable = true }
                }
            }

            //禁用指定的ActionBean
            disableAction?.split(Action.PACKAGE_SPLIT)?.forEach { str ->
                if (str.isNotEmpty()) {
                    str.toLongOrNull()
                        ?.let { id -> findActionById(id, l).forEach { it.enable = false } }
                    findFirstActionByGroup(str, l)?.let { it.enable = false }
                }
            }
        }
    }
}

/**通过[group]查找分组中的第一个[ActionBean]*/
fun TaskBean.findFirstActionByGroup(
    group: String? /*不支持分割*/,
    list: List<ActionBean>? = actionList
): ActionBean? {
    if (group.isNullOrEmpty()) {
        return null
    }
    var result: ActionBean? = null
    list?.apply {
        for (action in this) {
            if (action.group?.split(Action.PACKAGE_SPLIT)?.contains(group) == true) {
                result = action
                break
            }
        }
    }
    return result
}

fun TaskBean.findActionById(
    actionId: Long,
    list: List<ActionBean>? = actionList
): List<ActionBean> {
    val result = mutableListOf<ActionBean>()
    list?.apply {
        for (action in this) {
            if (action.actionId == actionId) {
                result.add(action)
            }
        }
    }
    return result
}

/**设置[com.angcyo.acc2.bean.TaskBean.textMap]数据*/
fun TaskBean.putMap(key: String?, value: String?) {
    if (textMap == null) {
        textMap = hashMapOf()
    }
    if (key != null) {
        textMap?.put(key, value)
    }
}

/**设置[com.angcyo.acc2.bean.TaskBean.textListMap]数据*/
fun TaskBean.putListMap(key: String?, value: String?, append: Boolean = true) {
    if (textListMap == null) {
        textListMap = hashMapOf()
    }
    if (key != null) {
        textListMap?.apply {
            val list = if (append) {
                get(key)?.toMutableList()
            } else {
                null
            } ?: mutableListOf()
            list.add(value)
            put(key, list)
        }
    }
}

fun TaskBean.putListMap(key: String?, value: List<String?>?, append: Boolean = true) {
    if (value.isNullOrEmpty()) {
        return
    }
    if (textListMap == null) {
        textListMap = hashMapOf()
    }
    if (key != null) {
        textListMap?.apply {
            val list = if (append) {
                get(key)?.toMutableList()
            } else {
                null
            } ?: mutableListOf()
            list.addAll(value)
            put(key, list)
        }
    }
}

/**追加[com.angcyo.acc2.bean.TaskBean.textMap]数据*/
fun TaskBean.appendMap(key: String?, value: String?) {
    if (textMap == null) {
        textMap = hashMapOf()
    }
    if (key != null) {
        textMap?.apply {
            val old = get(key)
            if (old.isNullOrEmpty()) {
                put(key, value)
            } else {
                put(key, "${old}${Action.TEXT_SPLIT}${value}")
            }
        }
    }
}

/**删除指定的[key]*/
fun TaskBean.deleteMapKey(key: String?) {
    if (key != null) {
        textMap?.remove(key)
        textListMap?.remove(key)
    }
}

/**从[TaskBean]中获取指定[key]的文本*/
fun TaskBean.getTextList(key: String?): List<String?>? {
    return textListMap?.get(key) ?: textMap?.get(key)?.run {
        listOf(this)
    }
}

/**map value*/
fun TaskBean.mapVal(key: String?): Any? = map?.get(key)

fun TaskBean.mapVal(key: String, value: Any?) {
    if (map == null) {
        map = hashMapOf()
    }
    map?.put(key, value)
}

fun TaskBean.mapStringVal(key: String?): String? = map?.get(key)?.toString()

fun TaskBean.mapLongVal(key: String?): Long? = mapStringVal(key)?.toLongOrNull()