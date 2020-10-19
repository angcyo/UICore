package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.text
import com.angcyo.library.ex.patternList

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class GetTextAction : BaseAction() {

    var onGetTextAction: ((String?, List<CharSequence>?) -> Unit)? = null

    init {
        handleAction = ConstraintBean.ACTION_GET_TEXT
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {

        //获取到的文本列表
        val getTextResultList: MutableList<CharSequence> = mutableListOf()
        //需要的formKey
        var getTextFormKey = arg

        val textRegexList = constraintBean.getTextRegexList
        handleNodeList.forEach {
            it.text()?.also { text ->
                val resultTextList = mutableListOf<String>()

                if (textRegexList == null) {
                    //未指定正则匹配规则
                    resultTextList.add(text.toString())
                } else {
                    textRegexList.forEach {
                        text.patternList(it).let { list ->
                            //正则匹配过滤后的文本列表
                            if (list.isNotEmpty()) {
                                resultTextList.addAll(list)
                            }
                        }
                    }

                    //未匹配到正则时, 使用默认
                    if (resultTextList.isEmpty()) {
                        resultTextList.add(text.toString())
                    }
                }

                //汇总所有文本
                getTextResultList.addAll(resultTextList)
            }
        }
        val value = getTextResultList.isNotEmpty()
        autoParseAction.handleActionLog("获取文本[$getTextResultList]:${value}")

        onGetTextAction?.invoke(getTextFormKey, if (value) getTextResultList else null)
        return value
    }
}