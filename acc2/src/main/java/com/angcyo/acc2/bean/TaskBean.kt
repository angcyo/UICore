package com.angcyo.acc2.bean

import com.angcyo.acc2.action.Action
import com.angcyo.library.ex.uuid

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class TaskBean(

    //<editor-fold desc="配置信息">

    var uuid: String? = uuid(),

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

    //</editor-fold desc="配置信息">

    //<editor-fold desc="浮窗配置">

    /**是否使用全屏浮窗提示*/
    var fullscreen: Boolean = false,

    /**浮窗是否不需要拦截手势*/
    var notTouchable: Boolean = false,

    /**显示手势提示框*/
    var showTouchTip: Boolean = true,

    /**显示节点提示框*/
    var showNodeTip: Boolean = true,

    //</editor-fold desc="浮窗配置">

    //<editor-fold desc="参数">

    /**任务需要处理那个程序的信息
     * 不支持正则*/
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
    var wordList: List<String>? = null,

    /** 优先级低于[textListMap]
     * 文本池, 供$[xxx] 获取使用
     * [xxx]命令保存的文本, 供后续使用*/
    var textMap: HashMap<String, String?>? = null,

    /**优先从[textListMap]里面取数据,其次从[textMap]取数据*/
    var textListMap: HashMap<String, List<String?>?>? = null,

    //</editor-fold desc="文本池">

    /**操作步骤*/
    var actionList: List<ActionBean>? = null,

    /**[actionList]未处理时的操作步骤*/
    var backActionList: List<ActionBean>? = null,

    /**在每个[ActionBean]执行之前, 都要执行的[ActionBean]
     * 如果处理成功了, 会中断原本需要执行的[ActionBean]*/
    var before: ActionBean? = null,

    /**在每个[ActionBean]执行之后, 都要执行的[ActionBean]*/
    var after: ActionBean? = null,
)

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

/**从[TaskBean]中获取指定[key]的文本*/
fun TaskBean.getTextList(key: String?): List<String?>? {
    return textListMap?.get(key) ?: textMap?.get(key)?.run {
        listOf(this)
    }
}