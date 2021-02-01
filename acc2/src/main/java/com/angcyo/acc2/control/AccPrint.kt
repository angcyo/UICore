package com.angcyo.acc2.control

import com.angcyo.library.L
import com.angcyo.library.ex.des

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AccPrint {

    /**输出日志*/
    open fun log(accControl: AccControl, log: String?) {
        L.i(log)
    }

    /**下一个步骤的提示
     * [title] 步骤的标题
     * [des] 步骤的描述
     * [time] 执行步骤的延迟
     * */
    open fun next(accControl: AccControl, title: String?, des: String?, time: Long) {
        L.w("next[$time]->$title${des.des()}".wrap())
    }
}