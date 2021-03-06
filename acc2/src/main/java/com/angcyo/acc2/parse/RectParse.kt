package com.angcyo.acc2.parse

import android.graphics.Rect
import com.angcyo.acc2.action.Action

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class RectParse(val accParse: AccParse) : BaseParse() {

    /**解析矩形, 并且判断目标[rect], 是否满足条件*/
    fun parse(rectMatch: String?, rect: Rect): Boolean {
        if (rectMatch == null) {
            return true
        }
        if (rectMatch.isEmpty()) {
            return !rect.isEmpty
        }
        val expParse = accParse.expParse

        val left = rect.left.toFloat()
        val right = rect.right.toFloat()

        //left
        val leftValueList = ExpParse.getValue(rectMatch, "l:|left:")
        expParse.ratioRef = accParse.accContext.getBound().width().toFloat()
        for (value in leftValueList) {
            if (!expParse.parseAndCompute(value, Action.OP, left)) {
                return false
            }
        }

        //right
        val rightValueList = ExpParse.getValue(rectMatch, "r:|right:")
        for (value in rightValueList) {
            if (!expParse.parseAndCompute(value, Action.OP, right)) {
                return false
            }
        }

        //width
        val widthValueList = ExpParse.getValue(rectMatch, "w:|width:")
        for (value in widthValueList) {
            if (!expParse.parseAndCompute(value, Action.OP, rect.width().toFloat())) {
                return false
            }
        }

        //------------------------------------

        val top = rect.top.toFloat()
        val bottom = rect.bottom.toFloat()

        //top
        val topValueList = ExpParse.getValue(rectMatch, "t:|top:")
        expParse.ratioRef = accParse.accContext.getBound().height().toFloat()
        for (value in topValueList) {
            if (!expParse.parseAndCompute(value, Action.OP, top)) {
                return false
            }
        }

        //bottom
        val bottomValueList = ExpParse.getValue(rectMatch, "b:|bottom:")
        for (value in bottomValueList) {
            if (!expParse.parseAndCompute(value, Action.OP, bottom)) {
                return false
            }
        }

        //height
        val heightValueList = ExpParse.getValue(rectMatch, "h:|height:")
        for (value in heightValueList) {
            if (!expParse.parseAndCompute(value, Action.OP, rect.height().toFloat())) {
                return false
            }
        }
        return true
    }
}