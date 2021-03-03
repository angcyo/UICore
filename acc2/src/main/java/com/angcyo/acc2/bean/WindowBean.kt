package com.angcyo.acc2.bean

/**
 * 选择对应的[AccessibilityWindowInfo], 所有条件必须都满足.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class WindowBean(

    /**当前[packageName]和[ignorePackageName]都是[null]时, 则取Active窗口*/
    /**当[packageName]为空字符或*时,则取所有窗口*/

    /**通过包名, 选择window,
     * 支持正则
     * 支持分割[com.angcyo.acc2.action.Action.PACKAGE_SPLIT]
     * 匹配顺序:1
     * [com.angcyo.acc2.parse.AccParse.parsePackageName]
     * */
    var packageName: String? = null,

    /**忽略指定包名的window
     * 支持正则
     * 支持分割[com.angcyo.acc2.action.Action.PACKAGE_SPLIT]
     * 匹配顺序:2
     * 默认把主app忽略
     * [main] 主程序,
     * [target] 目标程序
     * [com.angcyo.acc2.parse.AccParse.parsePackageName]
     * */
    var ignorePackageName: String? = "main",

    /**
     * 约束window的root节点矩形满足条件
     * [com.angcyo.acc2.bean.FindBean.rectList]
     * 匹配顺序:3*/
    var rect: String? = null,
)