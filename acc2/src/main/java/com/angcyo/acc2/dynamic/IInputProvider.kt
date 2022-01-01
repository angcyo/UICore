package com.angcyo.acc2.dynamic

import androidx.annotation.Keep
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.BaseAction
import com.angcyo.acc2.bean.InputTextBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext

/**
 * 文本输入时的数据提供者
 *
 * [com.angcyo.acc2.action.InputAction]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/01/01
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

@Keep
interface IInputProvider {

    /**返回文本输入的数量*/
    fun getInputTextCount(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        actionBean: BaseAction,
        action: String,
        inputTextBean: InputTextBean?
    ): Int? {
        return null
    }

    /**@return null 时, 会进行下一个[IInputProvider]调用]*/
    fun getInputTextList(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        actionBean: BaseAction,
        action: String,
        inputTextBean: InputTextBean?
    ): List<String?>? {
        return null
    }

}