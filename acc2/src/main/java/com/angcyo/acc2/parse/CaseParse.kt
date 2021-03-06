package com.angcyo.acc2.parse

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
    fun parse(caseList: List<CaseBean>): CaseBean? {
        for (case in caseList) {
            if (parse(case)) {
                return case
            }
        }
        return null
    }

    /**[case]是否符合*/
    fun parse(case: CaseBean): Boolean {

        var matchCaseBean: CaseBean? = null

        //1.
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

        return matchCaseBean != null
    }
}