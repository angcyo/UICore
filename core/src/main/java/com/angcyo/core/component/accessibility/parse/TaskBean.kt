package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.AutoParseInterceptor
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.AutoParser
import com.angcyo.core.component.accessibility.intervalMode

/**
 * 每个任务的数据结构, action约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class TaskBean(
    /**任务对应的包名, 比如(抖音的包名, 快手的包名)*/
    var packageName: String? = null,

    /**当前任务名称 */
    var name: String? = null,

    /**为[ActionBean]的[ConstraintBean]中的[wordInputIndexList]提供数据*/
    var wordList: List<String>? = null,

    /**
     * 组成任务的所有Action
     * */
    var actions: List<ActionBean>? = null
)

/**转成拦截器*/
fun TaskBean.toInterceptor(
    onConvertCheckById: (checkId: Long) -> CheckBean? = { null }, //将[checkId]转换成[checkBean]
    onGetTextResult: (action: AutoParseAction, List<CharSequence>) -> Unit = { _, _ -> } //[getText]动作, 返回的文本信息
): AutoParseInterceptor {
    return AutoParseInterceptor(this).apply {
        if (!packageName.isNullOrEmpty()) {
            filterPackageNameList.add(packageName!!)
        }
        intervalMode()

        actions?.forEach {
            val action = it.toAction(packageName ?: "")

            //根据[checkId]查找对应的[CheckBean]
            if (action.actionBean?.check == null) {
                val checkId = action.actionBean?.checkId ?: -1
                if (checkId > 0L) {
                    action.actionBean?.check = onConvertCheckById.invoke(checkId)
                }
            }

            //action动作执行的日志输出
            action.onLogPrint = {
                AutoParseInterceptor.log("$name($actionIndex/${actionList.size}) ${action.actionTitle} $it")
            }

            //获取到的文本回调
            action.onGetTextResult = {
                onGetTextResult(action, it)
            }

            //根据给定的[wordInputIndexList]返回对应的文本信息
            action.onGetWordTextListAction = {
                AutoParser.parseWordTextList(wordList, it)
            }

            //判断是否是back check
            if (it.check?.back == null) {
                actionList.add(action)
            } else {
                actionOtherList.add(action)
            }
        }
    }
}