package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.AccSchedule
import com.angcyo.acc2.control.log
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

    /**记录文本key输入的次数*/
    val inputCountMap = hashMapOf<String, Int>()

    companion object {

        /**
         * 可以通过key[com.angcyo.acc2.action.Action.LAST_INPUT]引用到
         *
         * [com.angcyo.acc2.parse.TextParse.parse]
         * */
        var lastInputText: String? = null
    }

    init {
        lastInputText = null
    }

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_INPUT) || action.cmd(Action.ACTION_SET_TEXT)
    }

    fun getCmd(action: String) =
        if (action.startsWith(Action.ACTION_INPUT)) Action.ACTION_INPUT else Action.ACTION_SET_TEXT

    override fun onScheduleStart(scheduled: AccSchedule) {
        super.onScheduleStart(scheduled)
    }

    override fun onScheduleEnd(scheduled: AccSchedule) {
        super.onScheduleEnd(scheduled)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        val arg = action.subEnd(Action.ARG_SPLIT)
        val textParse = control.accSchedule.accParse.textParse

        var textKey: String? = null
        var inputCount = 0

        //执行set text时的文本
        val text = if (arg.isNullOrEmpty()) {
            randomString() //随机生成文本
        } else {
            //指定key, 文本已经输入的次数
            textKey = textParse.parseTextKey(arg).firstOrNull() ?: action.arg("key")
            if (textKey.isNullOrEmpty()) {
                textKey = control.accSchedule._scheduleActionBean?.actionId?.toString()
            }
            if (!textKey.isNullOrEmpty()) {
                inputCount = inputCountMap[textKey] ?: 0
            }

            //指定获取文本的index
            val indexArg = arg.arg("index")
            var inputArg = arg
            var indexText: String? = null
            if (!indexArg.isNullOrEmpty()) {
                indexText = textParse.parse(indexArg).firstOrNull()
                inputArg = arg.replace(indexArg, "")//清除index参数
            }

            //待输入的文本池
            val inputTextList = textParse.parse(
                inputArg,
                false,
                getHandleTextParamBeanByAction(getCmd(action))
            )

            //获取目标
            if (indexText.isNullOrEmpty()) {
                //未指定index, 随机返回一个
                control.log("随机获取输入文本:$inputTextList")
                inputTextList.randomGet(1).firstOrNull()
            } else if (indexText == Action.ORDER) {
                //按顺序返回
                control.log("顺序获取输入文本第${inputCount}个:$inputTextList")
                inputTextList.getSafe(inputCount)
            } else {
                val index = indexText.toIntOrNull()
                if (index == null) {
                    //其他无法识别的文本内容, 随机获取
                    control.log("随机获取输入文本,无效参数[${indexText}]:$inputTextList")
                    inputTextList.randomGet(1).firstOrNull()
                } else {
                    //指定了index
                    control.log("指定获取输入文本索引第${inputCount}个:$inputTextList")
                    inputTextList.getSafe(index)
                }
            }
        }

        lastInputText = text

        nodeList?.forEach { node ->
            val result = node.setNodeText(text)
            success = result || success
            if (success) {
                if (!textKey.isNullOrEmpty()) {
                    //保存输入次数
                    inputCountMap[textKey] = inputCount + 1
                }
            }
            control.log("输入文本[$text]:$result\n${node.toLog()}")
        }
    }
}