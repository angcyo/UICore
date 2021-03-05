package com.angcyo.acc2.bean

/**
 * 文本参数处理, 文本长尾词替换
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/05
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class TextParamBean(

    /**生效的指令.
     * [com.angcyo.acc2.action.Action.ACTION_GET_TEXT]
     * [com.angcyo.acc2.action.Action.ACTION_APPEND_TEXT]
     * [com.angcyo.acc2.action.Action.ACTION_PUT_TEXT]
     * [com.angcyo.acc2.action.Action.ACTION_INPUT]
     * [com.angcyo.acc2.action.Action.ACTION_SET_TEXT]
     * 默认只在[com.angcyo.acc2.action.InputAction]指令中有效*/
    var handleAction: List<String>? = null,

    /**长尾词列表比如: 你/我/他*/
    var tailList: List<String?>? = null,

    /**长尾词需要替换的新词列表.
     * 替换列表与[tailList]的索引一一对应*/
    var tailUpList: List<List<String?>?>? = null,
)
