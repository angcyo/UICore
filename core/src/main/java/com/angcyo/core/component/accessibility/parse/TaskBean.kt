package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.AutoParseInterceptor
import com.angcyo.core.component.accessibility.BaseAccessibilityAction
import com.angcyo.core.component.accessibility.ILogPrint
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

    var id: Long = -1,
    var rwid: Long = -1,

    //----------------------------------------------------------

    /**任务id, 不参与auto parse*/
    var taskId: Long = -1,
    /**任务描述, 不参与auto parse*/
    var taskDes: String? = null,

    /**当前任务名称 */
    var name: String? = null,

    //--------------------------------------------------------------

    /**任务对应的包名, 比如(抖音的包名, 快手的包名), 多个包名使用`;`分割*/
    var packageName: String? = null,

    /**为[ActionBean]的[ConstraintBean]中的[wordInputIndexList]提供数据*/
    var wordList: List<String>? = null,

    /**为[ActionBean]的[interval]提供配置
     * 格式:[actionId:xxx.xx.x] 比如:[100000:5000,500,5] 表示指定[ActionBean]id等于100000对象的[interval]=5000,500,5
     * [actionId-actionId:xxx.xx.x] 范围内id对应的[ActionBean]都赋值.
     * 如果[ActionBean]的[interval]已经有值, 则不会覆盖
     * */
    var actionIntervalList: List<String>? = null,

    /**
     * 组成任务的所有Action
     * */
    var actions: List<ActionBean>? = null,

    //--------------------------------------------------------------

    /**收集所有action getText获取到的文本, 都将保存在此, 通过key-value的形式
     * 需要指定action的[formKey], 才会保存*/
    var getTextResultMap: Map<String, List<CharSequence>>? = null,

    /**[Task]完成后, 需要执行的网络请求*/
    var form: FormBean? = null,

    //--------------------------------------------------------------

    /**任务完成后, 是否启动主程序.(不管失败或者成功)*/
    var finishToApp: Boolean = true,

    /**当任务需要统一处理[leave] */
    var leaveCount: Long = BaseAccessibilityAction.DEFAULT_INTERCEPTOR_LEAVE_COUNT,

    /**当拦截器离开主程序界面多少次后, 触发的指令.任务需要统一处理离开界面的处理*/
    var leave: List<ConstraintBean>? = null
)

/**转成拦截器*/
fun TaskBean.toInterceptor(
    onEnableAction: (action: ActionBean) -> Boolean = { it.enable },
    onConvertCheckById: (checkId: Long) -> CheckBean? = { null }, //将[checkId]转换成[checkBean]
    onConfigAction: (AutoParseAction) -> Unit = {},
    onActionGetTextResult: (action: AutoParseAction, List<CharSequence>) -> Unit = { _, _ -> } //[getText]动作, 返回的文本信息
): AutoParseInterceptor {
    return AutoParseInterceptor(this).apply {
        //2020-08-23
        interceptorLeaveCount.maxCountLimit = leaveCount

        val taskPackageName: String? = packageName
        //如果任务中包含了指定的包名
        if (!taskPackageName.isNullOrEmpty()) {
            taskPackageName.split(";").forEach {
                filterPackageNameList.add(it)
            }
        }
        intervalMode()

        //[actionIntervalList]
        val actionIntervalMap = parseActionInterval()

        //action动作执行的日志输出
        interceptorLog = object : ILogPrint() {
            override fun log(msg: CharSequence?) {
                super.log(msg)
                AutoParseInterceptor.log(msg)
            }
        }

        actions?.forEach {
            if (onEnableAction(it)) {
                if (it.actionId > 0) {
                    //重置interval
                    it.interval = actionIntervalMap.getOrDefault(it.actionId, it.interval)
                }

                //to [AutoParseAction]
                val autoParseAction = it.toAction(filterPackageNameList.firstOrNull() ?: "")

                //如果未指定[check]对象, 则根据[checkId]查找对应的[CheckBean]
                if (autoParseAction.actionBean?.check == null) {
                    val checkId = autoParseAction.actionBean?.checkId ?: -1
                    if (checkId > 0L) {
                        autoParseAction.actionBean?.check = onConvertCheckById.invoke(checkId)
                    }
                }

                //action动作执行的日志输出
                autoParseAction.actionLog = object : ILogPrint() {
                    override fun log(msg: CharSequence?) {
                        super.log(msg)
                        AutoParseInterceptor.log("$name($actionIndex/${actionList.size})|${autoParseAction.actionTitle}|${_actionControl.lastMethodName()}->$msg")
                    }
                }

                //获取到的文本回调
                autoParseAction.onGetTextResult = { textFormKey, textList ->
                    try {
                        if (textList != null) {
                            if (getTextResultMap == null) {
                                getTextResultMap = hashMapOf()
                            }
                            val map = getTextResultMap
                            if (map !is HashMap) {
                                getTextResultMap = hashMapOf()
                            }

                            //优先使用自带的formKey参数, 其次使用action的[formKey]
                            val formKey = textFormKey
                                ?: autoParseAction.actionBean?.formKey /* ?: autoParseAction.hashCode().toString()*/

                            val oldValue = (map as HashMap)[formKey]
                            formKey?.let {
                                if (textList.isEmpty()) {
                                    map[formKey] = oldValue ?: textList
                                } else {
                                    map[formKey] = textList
                                }
                            }

                            //callback
                            onActionGetTextResult(autoParseAction, textList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }// end onGetTextResult

                //根据给定的[wordInputIndexList]返回对应的文本信息
                autoParseAction.onGetWordTextListAction = {
                    AutoParser.parseWordTextList(wordList, it)
                }

                //action表单提交时的参数配置
                autoParseAction.onConfigParams = {
                    //直接使用[AutoParseInterceptor]的配置信息
                    this.onConfigParams?.invoke(it)
                }

                onConfigAction(autoParseAction)

                //判断是否是back check
                if (it.check?.back == null) {
                    actionList.add(autoParseAction)
                } else {
                    actionOtherList.add(autoParseAction)
                }
            }//...end enable
        }
    }
}

/**解析[actionId-actionId:xxx.xx.x]格式, 将[actionId]和[interval]解析成map格式*/
fun TaskBean.parseActionInterval(): Map<Long, String?> {
    val result = HashMap<Long, String?>()
    actionIntervalList?.forEach { format ->
        try {
            val indexOf = format.indexOf(":", 0, true)

            if (indexOf != -1) {
                //找到
                val ids = format.substring(0, indexOf)
                val interval = format.substring(indexOf + 1, format.length)

                val idIndexOf = ids.indexOf("-")
                if (idIndexOf == -1) {
                    //指定了一个id
                    ids.toLongOrNull()?.let {
                        result[it] = interval
                    }
                } else {
                    //指定的是一个id范围
                    val startId = ids.substring(0, idIndexOf).toLongOrNull() ?: -1
                    val endId = ids.substring(idIndexOf + 1, ids.length).toLongOrNull() ?: -1
                    for (i in startId..endId) {
                        result[i] = interval
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}