package com.angcyo.core.component.accessibility.action

/**
 * [com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor.onDoAction] 流程监督
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/30
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ActionControl {

    companion object {
        const val METHOD_onCheckEventOut = "onCheckEventOut"
        const val METHOD_doActionWidth = "doActionWidth"
        const val METHOD_checkOtherEvent = "checkOtherEvent"
        const val METHOD_doAction = "doAction"
        const val METHOD_checkEvent = "checkEvent"

        const val ACTION_randomization = "randomization"
    }

    /**当前正在执行的方法*/
    var methodNameList: List<String>? = null

    /**当前执行的方法, 处理时:所用的指令*/
    var actionNameList: List<String?>? = null

    /**记录执行方法*/
    fun addMethodName(methodName: String) {
        if (methodNameList == null || methodNameList !is MutableList) {
            methodNameList = mutableListOf()
        }
        (methodNameList as? MutableList?)?.add(methodName)

        _initAction()
    }

    fun _initAction() {
        if (actionNameList == null || actionNameList !is MutableList) {
            actionNameList = mutableListOf()
        }
        for (i in actionNameList!!.size until (methodNameList?.size ?: 0)) {
            (actionNameList as? MutableList?)?.add(null)
        }
    }

    /**追加执行动作指令*/
    fun addActionName(actionName: String) {
        val index = methodNameList?.lastIndex ?: -1
        if (actionNameList != null) {
            if (index in 0..actionNameList!!.lastIndex) {
                val old = actionNameList?.getOrNull(index)
                if (old == null) {
                    (actionNameList as? MutableList?)?.set(index, actionName)
                } else {
                    (actionNameList as? MutableList?)?.set(index, "$old|$actionName")
                }
            }
        }
    }

    /**最后一个方法, 执行的动作指令*/
    fun lastMethodAction(methodName: String? = null): String? {
        val index = if (methodName.isNullOrEmpty()) {
            methodNameList?.lastIndex ?: -1
        } else {
            methodNameList?.lastIndexOf(methodName) ?: -1
        }
        if (actionNameList != null) {
            if (index in 0..actionNameList!!.lastIndex) {
                val old = actionNameList?.getOrNull(index)
                return old
            }
        }
        return null
    }

    /**最后一个方法*/
    fun lastMethodName(): String? = methodNameList?.lastOrNull()

    /**最后一步[ohter]里面是否执行了指令*/
    fun isLastOtherHandle(): Boolean {
        return lastMethodAction(METHOD_onCheckEventOut) != null ||
                lastMethodAction(METHOD_checkOtherEvent) != null

    }
}