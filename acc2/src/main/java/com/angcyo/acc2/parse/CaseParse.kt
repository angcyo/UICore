package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.CaseBean
import com.angcyo.acc2.bean.getTextList
import com.angcyo.library.ex.size
import com.angcyo.library.ex.subEnd
import com.angcyo.library.ex.subStart

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/04
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class CaseParse(val accParse: AccParse) : BaseParse() {

    /**解析符合条件的[CaseBean]*/
    fun parse(caseList: List<CaseBean>, originList: List<AccessibilityNodeInfoCompat>?): CaseBean? {
        for (case in caseList) {
            if (parse(case, originList)) {
                return case
            }
        }
        return null
    }

    /**[case]是否符合*/
    fun parse(case: CaseBean, originList: List<AccessibilityNodeInfoCompat>?): Boolean {

        var matchCaseBean: CaseBean? = null

        //1.textCount
        val textCount = case.textCount
        if (textCount != null) {
            var allMatch = true
            val allTextCountList = textCount.split(Action.ARG_SPLIT2)
            for (_textCount in allTextCountList) {
                val key = _textCount.subStart(Action.ARG_SPLIT)
                val value = _textCount.subEnd(Action.ARG_SPLIT)
                val textList = accParse.accControl._taskBean?.getTextList(key)
                allMatch = accParse.expParse.parseAndCompute(
                    value,
                    inputValue = textList.size().toFloat()
                )
                if (!allMatch) {
                    break
                }
            }
            if (allMatch) {
                matchCaseBean = case
            }
        }

        //2.stateList
        val stateList = case.stateList
        if (originList != null && stateList != null) {
            var allMatch = true
            for (i in 0 until originList.size()) {
                val node = originList.getOrNull(i)
                val state = stateList.getOrNull(i)

                if (state != null && node != null) {
                    allMatch = accParse.findParse.matchNodeState(node, state)

                    if (!allMatch) {
                        break
                    }
                }
            }

            matchCaseBean = if (allMatch) {
                case
            } else {
                null
            }
        }

        return matchCaseBean != null
    }
}