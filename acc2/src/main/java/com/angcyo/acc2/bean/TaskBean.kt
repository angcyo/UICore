package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class TaskBean(

    /**任务需要处理那个程序的信息
     * 支持正则*/
    var packageName: String? = null,

    /**任务的标题*/
    var title: String? = null,

    /**任务的描述*/
    var des: String? = null,

    /**操作步骤*/
    var actionList: List<ActionBean>? = null,

    /**[actionList]未处理时的操作步骤*/
    var backActionList: List<ActionBean>? = null,

    /**字符串输入供给*/
    var wordList: List<String>? = null,

    /**
     * 文本池, 供$[xxx] 获取使用
     * [xxx]命令保存的文本, 供后续使用*/
    var textMap: HashMap<String, String?>? = null,

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