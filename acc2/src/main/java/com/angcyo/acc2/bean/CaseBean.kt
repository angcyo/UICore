package com.angcyo.acc2.bean

/**
 * case
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/04
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class CaseBean(

    //<editor-fold desc="条件判断">

    /**case:如果指定key对应的文本数量满足条件时, 符合case
     * [com.angcyo.acc2.bean.FindBean.rectList]
     *
     * [xxx:>=100]
     * [xxx:>=100 xxx:<=100] 有多个时, 并且的关系
     * */
    var textCount: String? = null,

    //</editor-fold desc="条件判断">

    //<editor-fold desc="替换处理">

    /**文本特殊处理参数, 如长尾词替换处理.
     * [com.angcyo.acc2.bean.HandleBean.textParam]
     * [com.angcyo.acc2.bean.TaskBean.textParam]
     * [com.angcyo.acc2.bean.CaseBean.textParam]
     * */
    var textParam: TextParamBean? = null,

    /**用于替换[com.angcyo.acc2.bean.HandleBean.actionList]
     * 如果未指定, 还是用的原来的*/
    var actionList: List<String>? = null,

    //</editor-fold desc="替换处理">
)
