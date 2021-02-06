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

    /**任务的标题*/
    var title: String? = null,

    /**任务的描述*/
    var des: String? = null,

    /**任务类型, 用于查找匹配使用*/
    var type: String? = null,

    //</editor-fold desc="配置信息">

    /**任务需要处理那个程序的信息
     * 不支持正则*/
    var packageName: String? = null,

    /**操作步骤*/
    var actionList: List<ActionBean>? = null,

    /**[actionList]未处理时的操作步骤*/
    var backActionList: List<ActionBean>? = null,

    /**
     * [com.angcyo.acc2.bean.ActionBean.limitRunCount]*/
    var limitRunCount: Int = -1,

    //<editor-fold desc="文本池">

    /**字符串输入供给
     * [$0 ]
     * [$0 $2 $3]
     * [$0~$-1]*/
    var wordList: List<String>? = null,

    /**
     * 文本池, 供$[xxx] 获取使用
     * [xxx]命令保存的文本, 供后续使用*/
    var textMap: HashMap<String, String?>? = null,

    //</editor-fold desc="文本池">

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