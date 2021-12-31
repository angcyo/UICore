package com.angcyo.core.component

import com.angcyo.core.lifecycle.LifecycleViewModel
import com.angcyo.http.rx.doMain
import com.angcyo.viewmodel.vmDataNull

/**
 *
 * 弹窗模型, 用来实现 对话框 1个1个轮流弹窗
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/31
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DialogAlertModel : LifecycleViewModel() {

    /**等待弹出的对话框*/
    val dialogList = mutableListOf<DialogBean>()

    /**正在弹出的对话框*/
    val dialogAlertData = vmDataNull<DialogBean>()

    /**添加一个弹窗到队列
     *
     * [checkExist] 是否检查相同类型的弹窗, 如果已存在. 则不追加*/
    fun addDialog(type: String, data: Any? = null, checkExist: Boolean = true) {
        if (checkExist) {
            val find = dialogList.find { it.type == type }
            if (find == null) {
                //未找到
                dialogList.add(DialogBean(type, data))
            }
        } else {
            dialogList.add(DialogBean(type, data))
        }

        //开始弹窗
        checkDialog()
    }

    /**检查下一个*/
    fun doNextAlert() {
        val old = dialogAlertData.value
        old?.let {
            dialogList.remove(it)
        }
        doMain {
            dialogAlertData.value = null
            checkDialog()
        }
    }

    /**检查是否要弹窗*/
    fun checkDialog() {
        if (dialogAlertData.value == null) {
            if (dialogList.isNotEmpty()) {
                dialogList.firstOrNull()?.let {
                    //开始弹窗
                    dialogAlertData.postValue(it)
                }
            }
        }
    }

    /**取消一种类型的弹窗提醒*/
    fun cancelAlert(type: String) {
        dialogList.removeAll { it.type == type }
        val old = dialogAlertData.value
        old?.let {
            it.isCancel = true
            dialogAlertData.postValue(it)
        }
    }
}

/**弹窗的数据*/
data class DialogBean(
    //弹窗类型
    val type: String,
    //弹窗数据
    val data: Any?,
    //是否是需要取消
    var isCancel: Boolean = false,
)