package com.angcyo.acc2.bean

/**
 * 输入文本时的参数打包
 *
 * 比如 index key regex 等
 *
 * [com.angcyo.acc2.action.InputAction]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/01/01
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class InputTextBean(

    /**整个命令
     * ["input:123 key:remark regex:\\d+ index:order"]
     * */
    val action: String,

    /**文本list中的索引取值
     * [com.angcyo.acc2.action.Action.ORDER]*/
    val index: String?,

    /**输入的关键字*/
    val key: String?,

    /**正则*/
    val regex: String?,

    /**当前[key]对应的输入次数*/
    val inputCount: Int,

    /**长尾词*/
    val textParamBean: TextParamBean?,
)