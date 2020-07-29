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

    /**任务id, 不参与auto parse*/
    var taskId: Long = 0,
    /**任务描述, 不参与auto parse*/
    var taskDes: String? = null,

    /**任务对应的包名, 比如(抖音的包名, 快手的包名)*/
    var packageName: String? = null,

    /**当前任务名称 */
    var name: String? = null,

    /**为[ActionBean]的[ConstraintBean]中的[wordInputIndexList]提供数据*/
    var wordList: List<String>? = null,

    /**
     * 组成任务的所有Action
     * */
    var actions: List<ActionBean>? = null,

    /**getText获取到的文本, 都将保存在此, 通过key-value的形式*/
    var getTextResultMap: Map<String, List<CharSequence>>? = null,

    /**[Task]完成后, 需要执行的网络请求*/
    var form: FormBean? = null
)

/**转成拦截器*/
fun TaskBean.toInterceptor(
    onConvertCheckById: (checkId: Long) -> CheckBean? = { null }, //将[checkId]转换成[checkBean]
    onGetTextResult: (action: AutoParseAction, List<CharSequence>) -> Unit = { _, _ -> } //[getText]动作, 返回的文本信息
): AutoParseInterceptor {
    return AutoParseInterceptor(this).apply {
        val taskPackageName: String? = packageName
        //如果任务中包含了指定的包名
        if (!taskPackageName.isNullOrEmpty()) {
            taskPackageName.split(";").forEach {
                filterPackageNameList.add(it)
            }
        }
        intervalMode()

        actions?.forEach {
            val action = it.toAction(filterPackageNameList.firstOrNull() ?: "")

            //如果未指定[check]对象, 则根据[checkId]查找对应的[CheckBean]
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
            action.onGetTextResult = { textList ->
                try {
                    if (getTextResultMap == null) {
                        getTextResultMap = hashMapOf()
                    }
                    val map = getTextResultMap
                    if (map !is HashMap) {
                        getTextResultMap = hashMapOf()
                    }
                    val formKey = action.actionBean?.formKey ?: action.hashCode().toString()
                    (map as HashMap)[formKey] = textList

                    onGetTextResult(action, textList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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