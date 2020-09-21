package com.angcyo.core.component.accessibility.action.a

import android.net.Uri
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.AutoParser
import com.angcyo.core.component.accessibility.action.ErrorActionException
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.library.ex.patternList
import com.angcyo.library.ex.startIntent
import com.angcyo.library.utils.PATTERN_URL

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class UrlAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_URL
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        //打开url

        //需要打开的url参数
        var targetUrl: String? = null
        //包名参数
        var targetPackageName: String? = null

        //解析2个点的坐标
        val indexOf = arg?.indexOf(":", 0, true) ?: -1
        if (indexOf == -1) {
            //未找到
            targetUrl = arg
        } else {
            //找到
            targetUrl = arg?.substring(0, indexOf)
            targetPackageName = arg?.substring(indexOf + 1, arg!!.length)
        }

        //转换一下
        targetUrl = autoParseAction.getWordTextList(targetUrl, targetUrl)

        //解析对应的url
        targetUrl?.let {
            if (!it.startsWith("http")) {
                targetUrl = it.patternList(PATTERN_URL).firstOrNull()
            }
        }

        var value = false
        if (!targetUrl.isNullOrEmpty()) {
            //解析包名
            targetPackageName = AutoParser.parseTargetPackageName(
                targetPackageName,
                autoParseAction._targetPackageName()
            )

            targetPackageName?.let {
                value = service.startIntent {
                    setPackage(it)
                    data = Uri.parse(targetUrl)
                } != null

                autoParseAction.handleActionLog("使用:[$targetPackageName]打开[$targetUrl]:$value")
            }

            if (!value) {
                autoParseAction.doActionFinish(ErrorActionException("无法打开[$targetPackageName][$targetUrl]"))
            }
        }
        return value
    }
}