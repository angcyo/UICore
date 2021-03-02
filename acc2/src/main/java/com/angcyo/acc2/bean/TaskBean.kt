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

    //<editor-fold desc="参数">

    /**是否使用全屏浮窗提示*/
    var fullscreen: Boolean = false,

    /**浮窗是否不需要拦截手势*/
    var notTouchable: Boolean = false,

    /**任务需要处理那个程序的信息
     * 不支持正则*/
    var packageName: String? = null,

    /**
     * [com.angcyo.acc2.bean.ActionBean.limitRunCount]*/
    var limitRunCount: Int = -1,

    /**
     * [com.angcyo.acc2.bean.ActionBean.limitRunTime]*/
    var limitRunTime: Int = -1,

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
fun TaskBean.putListMap(key: String?, value: String?) {
    if (textListMap == null) {
        textListMap = hashMapOf()
    }
    if (key != null) {
        textListMap?.apply {
            val list = get(key)?.toMutableList() ?: mutableListOf()
            list.add(value)
            put(key, list)
        }
    }
}

fun TaskBean.putListMap(key: String?, value: List<String?>?) {
    if (textListMap == null) {
        textListMap = hashMapOf()
    }
    if (key != null) {
        textListMap?.put(key, value)
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