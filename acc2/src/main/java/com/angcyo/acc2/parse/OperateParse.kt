package com.angcyo.acc2.parse

import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.bean.OperateBean
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.ex.subEnd
import com.angcyo.library.ex.subStart
import com.angcyo.library.ex.toStr
import com.angcyo.library.toastQQ

/**
 * 操作记录解析
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/04
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class OperateParse(val accParse: AccParse) : BaseParse() {

    /**解析操作记录*/
    fun parse(handleBean: HandleBean, operateBean: OperateBean, handleResult: HandleResult) {

        //1 用空格分割多个 key:value 组合
        if (!operateBean.text.isNullOrEmpty()) {
            operateBean.text?.split(Action.ARG_SPLIT2)?.let {
                parseOperateText(operateBean, it)
            }
        }

        //2 不用空格隔开的 key:value 集合
        parseOperateText(operateBean, operateBean.textList)

        /*------------------------------后--------------------------------*/

        //toast
        if (isDebugType()) {
            operateBean.map?.toStr()?.let {
                toastQQ(it)
            }
        }

        //解析后的值在 operateBean.map 中保存

        //操作记录的回调
        accParse.accControl.controlListenerList.forEach {
            it.onHandleOperate(accParse.accControl, handleBean, operateBean, handleResult)
        }
        accParse.accControl._taskBean?._listenerObjList?.forEach {
            it.onHandleOperate(accParse.accControl, handleBean, operateBean, handleResult)
        }
    }

    fun parseOperateText(operateBean: OperateBean, textList: List<String>?) {
        if (textList.isNullOrEmpty()) {
            return
        }

        val map = operateBean.map?.toMutableMap() ?: hashMapOf()
        textList.forEach { text ->
            //从text中解析出 key 和 value
            val key = text.subStart(Action.ARG_SPLIT)
            val value = text.subEnd(Action.ARG_SPLIT)
            if (key != null) {
                map[key] = accParse.textParse.parse(value, true)
            }
        }

        operateBean.map = map
    }

}