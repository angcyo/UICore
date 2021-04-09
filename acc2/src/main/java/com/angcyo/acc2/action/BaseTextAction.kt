package com.angcyo.acc2.action

import com.angcyo.acc2.control.AccControl

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/04/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseTextAction : BaseAction() {

    /**替换文本*/
    open fun replaceText(control: AccControl, arg: String?, def: String): String {
        return control.accSchedule.accParse.textParse.parse(arg, true).firstOrNull() ?: def
    }
}