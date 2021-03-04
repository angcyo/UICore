package com.angcyo.acc2.bean

/**
 * case
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/04
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class CaseBean(

    /**case:如果指定key对应的文本数量满足条件时, 符合case
     * [com.angcyo.acc2.bean.FindBean.rectList]
     *
     * [xxx:>=100]
     * [xxx:>=100 xxx:<=100] 有多个时, 并且的关系
     * */
    var textCount: String? = null,

    /**用于替换[com.angcyo.acc2.bean.HandleBean.actionList]
     * 如果未指定, 还是用的原来的*/
    var actionList: List<String>? = null,
)
