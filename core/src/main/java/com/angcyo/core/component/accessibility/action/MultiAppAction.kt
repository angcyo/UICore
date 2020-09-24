package com.angcyo.core.component.accessibility.action

import com.angcyo.core.component.accessibility.parse.ActionBean
import com.angcyo.core.component.accessibility.parse.CheckBean
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library.component.appBean
import com.angcyo.library.ex.str
import kotlin.math.max

/**
 * 多开窗口检测
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MultiAppAction : AutoParseAction() {

    /**需要判断的app包名*/
    var packageName: String? = null

    /**强制指定需要判断的app名称, 如果未指定, 则自动从[packageName]拿到应用名称*/
    var appName: String? = null

    /**如果检测到多开, 则默认打开第0个应用*/
    var defaultOpenIndex = 0

    /**构建action*/
    fun initAction() {

        val name: String? = appName ?: packageName?.appBean()?.appName?.str()

        if (name.isNullOrEmpty()) {
            return
        }

        actionBean = ActionBean().apply {
            check = CheckBean().apply {
                back = listOf(ConstraintBean().apply {
                    //boundRect = ">=0.5:<=0.8"
                    textList = listOf(name)
                    stateList = listOf("clickable:5")
                    rectList = listOf("0.01,0.7-0.99,0.99")
                    nodeCount = ">=${max(2, defaultOpenIndex + 1)}"
                    handleNodeList = listOf(defaultOpenIndex)
                    actionList = listOf(ConstraintBean.ACTION_CLICK)
                })
            }
        }
    }
}