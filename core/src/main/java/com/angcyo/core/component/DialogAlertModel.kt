package com.angcyo.core.component

import com.angcyo.core.lifecycle.LifecycleViewModel
import com.angcyo.http.rx.doMain
import com.angcyo.library.ex.nowTime
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
    val dialogAlertList = mutableListOf<DialogAlertData>()

    /**正在弹出的对话框*/
    val dialogAlertData = vmDataNull<DialogAlertData>()

    //存储上一次指定类型通知的时间
    val alertShakeMap = hashMapOf<String, Long>()

    /**添加一个弹窗到队列
     *
     * [checkExist] 是否检查相同类型的弹窗, 如果已存在. 则不追加*/
    fun addDialog(
        type: String,
        data: Any? = null,
        shakeDelay: Long = 30 * 60 * 1000,
        checkExist: Boolean = true
    ) {
        addDialogAlert(DialogAlertData(type, data, shakeDelay = shakeDelay), checkExist)
    }

    fun addDialogAlert(bean: DialogAlertData, checkExist: Boolean = true) {
        val type = bean.type

        if (checkExist) {
            val find = dialogAlertList.find { it.type == type }
            if (find == null) {
                //未找到
                dialogAlertList.add(bean)
            }
        } else {
            dialogAlertList.add(bean)
        }

        //开始弹窗
        checkDialog()
    }

    /**检查下一个*/
    fun doNextAlert() {
        val old = dialogAlertData.value
        old?.let {
            dialogAlertList.remove(it)
        }
        doMain {
            dialogAlertData.value = null
            checkDialog()
        }
    }

    /**检查是否要弹窗*/
    fun checkDialog() {
        if (dialogAlertData.value == null) {
            if (dialogAlertList.isNotEmpty()) {
                dialogAlertList.firstOrNull()?.let { alertBean ->
                    var alert = false
                    val nowTime = nowTime()
                    if (alertBean.shakeDelay > 0) {
                        val lastTime = alertShakeMap[alertBean.type] ?: 0
                        if (nowTime - lastTime > alertBean.shakeDelay) {
                            //需要通知
                            alert = true
                        }
                    } else {
                        alert = true
                    }

                    if (alert) {
                        //需要弹窗, 开始弹窗
                        alertShakeMap[alertBean.type] = nowTime
                        dialogAlertData.postValue(alertBean)
                    } else {
                        //下一个弹窗
                        dialogAlertList.remove(alertBean) //不需要弹窗, 直接移除
                        doNextAlert()
                    }
                }
            }
        }
    }

    /**取消一种类型的弹窗提醒*/
    fun cancelAlert(type: String) {
        dialogAlertList.removeAll { it.type == type }
        cancelCurrent()
    }

    fun cancelAll() {
        dialogAlertList.clear()
        cancelCurrent()
    }

    fun cancelCurrent() {
        val old = dialogAlertData.value
        old?.let {
            it.isCancel = true
            dialogAlertData.postValue(it)
        }
    }
}

/**弹窗的数据*/
data class DialogAlertData(
    //弹窗类型
    val type: String,
    //弹窗数据
    val data: Any?,
    //是否是需要取消
    var isCancel: Boolean = false,
    //距离上一次同类型的通知间隔时长大于此值时才通知, 否则忽略, 默认30分钟内只通知一次. 毫秒
    var shakeDelay: Long = 30 * 60 * 1000,
)