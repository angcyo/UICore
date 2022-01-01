package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.InputTextBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.AccSchedule
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.*

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class InputAction : BaseAction() {

    /**记录文本key输入的次数, 用于执行[com.angcyo.acc2.action.Action.ORDER]*/
    val inputCountMap = hashMapOf<String, Int>()

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_INPUT)
    }

    fun getCmd(action: String) =
        if (action.startsWith(Action.ACTION_INPUT)) Action.ACTION_INPUT else Action.ACTION_SET_TEXT

    override fun onScheduleStart(scheduled: AccSchedule) {
        super.onScheduleStart(scheduled)
    }

    override fun onScheduleEnd(scheduled: AccSchedule) {
        super.onScheduleEnd(scheduled)
        inputCountMap.clear()
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        var noTextArg = false

        var arg = if (action.startsWith("${Action.ACTION_INPUT}${Action.ARG_SPLIT}")) {
            action.subEnd(Action.ARG_SPLIT)
        } else {
            noTextArg = true //未配置输入参数 input 裸命令
            action.subEnd(Action.ARG_SPLIT_SPACE)
        }

        val textParse = control.accSchedule.accParse.textParse

        var textKey: String? = null
        var textRegex: String? = null

        var inputCount = 0

        //文本列表的数量
        var textListSize = -1

        //执行set text时的文本
        val text = if (arg.isNullOrEmpty()) {
            randomString() //随机生成文本
        } else {
            //指定key, 文本已经输入的次数
            textKey = textParse.parseTextKey(arg).firstOrNull() ?: action.arg("key")
            if (textKey.isNullOrEmpty()) {
                textKey = control.accSchedule._scheduleActionBean?.actionId?.toString()
            } else {
                arg = arg.replace(" key:${textKey}", "")//清除key参数
            }
            inputCount = inputCountMap[textKey] ?: 0

            textRegex = action.arg("regex")
            if (!textRegex.isNullOrEmpty()) {
                arg = arg.replace(" regex:${textRegex}", "")//清除regex参数
            }

            //指定获取文本的index
            val indexArg = arg.arg("index")
            var inputArg = arg
            var indexText: String? = null
            if (!indexArg.isNullOrEmpty()) {
                indexText = textParse.parse(indexArg).firstOrNull()
                inputArg = arg.replace(" index:${indexArg}", "")//清除index参数
            }

            val textParamBean = getHandleTextParamBeanByAction(getCmd(action))
            val inputTextBean = InputTextBean(
                action,
                indexText,
                textKey,
                textRegex,
                inputCount,
                textParamBean
            )

            var textSize: Int? = null
            val inputTextList = if (noTextArg) {
                //通过文本提供器, 提供文本
                textSize = textParse.getInputTextCount(
                    control,
                    controlContext,
                    nodeList,
                    this@InputAction,
                    action,
                    inputTextBean
                )

                textParse.getInputTextList(
                    control,
                    controlContext,
                    nodeList,
                    this@InputAction,
                    action,
                    inputTextBean
                )
            } else {
                //待输入的文本池
                textParse.parse(
                    inputArg,
                    false,
                    textParamBean
                )
            }

            textListSize = textSize ?: inputTextList.size()

            //获取目标
            if (indexText.isNullOrEmpty()) {
                //未指定index, 随机返回一个
                controlContext.log {
                    append("随机获取输入文本:$inputTextList")
                }
                inputTextList.randomGet(1).firstOrNull()
            } else if (indexText == Action.ORDER) {
                //按顺序返回
                controlContext.log {
                    append("顺序获取输入文本第${inputCount}个:$inputTextList")
                }
                inputTextList?.getSafe(inputCount)
            } else {
                val index = indexText.toIntOrNull()
                if (index == null) {
                    //其他无法识别的文本内容, 随机获取
                    controlContext.log {
                        append("随机获取输入文本,无效参数[${indexText}]:$inputTextList")
                    }
                    inputTextList.randomGet(1).firstOrNull()
                } else {
                    //指定了index
                    controlContext.log {
                        append("指定获取输入文本索引第${inputCount}个:$inputTextList")
                    }
                    inputTextList?.getSafe(index)
                }
            }
        }

        //save, 保存最后一次输入
        //control.accSchedule.inputTextList.add(text)
        control.accSchedule.saveInputText(text, textKey)

        nodeList?.forEach { node ->
            val result = node.setNodeText(text)
            success = result || success
            if (success) {
                if (!textKey.isNullOrEmpty()) {
                    val count = inputCount + 1
                    //保存输入次数
                    inputCountMap[textKey] = count

                    if (count >= textListSize) {
                        //输入结束标识
                        control.accSchedule.inputFinishList.add(textKey)
                    }

                    if (textListSize in 1 until count) {
                        //文本输入结束后
                        controlContext.handle?.let {
                            val handleParse = control.accSchedule.accParse.handleParse
                            handleParse.onTextInputEnd(it, controlContext, nodeList, action)
                        }
                    }
                }
            }

            controlContext.log {
                append("输入文本[$text]:$result\n${node.toLog()}")
            }
        }
    }
}