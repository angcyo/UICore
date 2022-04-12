package com.angcyo.library.ex

import android.content.Context
import android.graphics.RectF
import android.text.TextUtils
import android.view.View
import com.angcyo.library.*
import kotlin.math.absoluteValue

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/21
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

typealias RSize = String?

fun View.calcLayoutWidthHeight(
    rLayoutWidth: RSize, rLayoutHeight: RSize,
    parentWidth: Int, parentHeight: Int,
    rLayoutWidthExclude: Int = 0, rLayoutHeightExclude: Int = 0
): IntArray {
    return context.calcLayoutWidthHeight(
        rLayoutWidth,
        rLayoutHeight,
        parentWidth,
        parentHeight,
        rLayoutWidthExclude,
        rLayoutHeightExclude
    )
}

/**
 * 支持格式0.3pw 0.5ph, p表示[parent]的多少倍数, s表示[screen]的多少倍数
 * */
fun Context.calcLayoutWidthHeight(
    rLayoutWidth: RSize, rLayoutHeight: RSize,
    parentWidth: Int, parentHeight: Int,
    rLayoutWidthExclude: Int = 0, rLayoutHeightExclude: Int = 0
): IntArray {
    val size = intArrayOf(-1, -1)
    if (TextUtils.isEmpty(rLayoutWidth) && TextUtils.isEmpty(rLayoutHeight)) {
        return size
    }
    size[0] = calcSize(rLayoutWidth, parentWidth, parentHeight, rLayoutWidthExclude)
    size[1] = calcSize(rLayoutHeight, parentWidth, parentHeight, rLayoutHeightExclude)
    return size
}

fun Context.calcLayoutMaxHeight(
    rMaxHeight: RSize,
    parentWidth: Int,
    parentHeight: Int,
    exclude: Int = 0,
    def: Int = -1
): Int {
    return calcSize(rMaxHeight, parentWidth, parentHeight, exclude, def)
}

/**[exp] 计算表达式, 支持 sh ph px dip, 正数是倍数, 负数是减去倍数的值*/

fun calcSize(
    exp: RSize,
    pWidth: Int = _screenWidth,
    pHeight: Int = _screenHeight,
    exclude: Int = 0,
    def: Int = -1
): Int {
    return app().calcSize(exp, pWidth, pHeight, exclude, def)
}

/**计算表达式, 支持 sh ph px dip, 正数是倍数, 负数是减去倍数的值*/
fun RSize.toRSize(
    pWidth: Int = _screenWidth,
    pHeight: Int = _screenHeight,
    exclude: Int = 0,
    def: Int = -1,
    context: Context = app(),
): Int = context.calcSize(this, pWidth, pHeight, exclude, def)

fun View.calcSize(
    exp: String?,
    pWidth: Int = getScreenWidth(),
    pHeight: Int = getScreenHeight(),
    exclude: Int = 0,
    def: Int = -1
): Int {
    return context.calcSize(exp, pWidth, pHeight, exclude, def)
}

fun Context.calcSize(
    exp: String?,
    pWidth: Int = getScreenWidth(),
    pHeight: Int = getScreenHeight(),
    exclude: Int = 0,
    def: Int = -1
): Int {
    var result = def
    if (!exp.isNullOrBlank()) {
        fun _get(ut: String, height: Int): Boolean {
            if (exp.contains(ut, true)) {
                val ratio = exp.replace(ut, "", true).toFloatOrNull()
                ratio?.let {
                    result = if (it >= 0) {
                        (it * (height - exclude)).toInt()
                    } else {
                        (height - it.absoluteValue * height - exclude).toInt()
                    }
                }
                return true
            }
            return false
        }

        fun _getDp(ut: String, density: Int): Boolean {
            if (exp.contains(ut, true)) {
                val ratio = exp.replace(ut, "", true).toFloatOrNull()
                ratio?.let {
                    result = if (it >= 0) {
                        ((it * density) - exclude).toInt()
                    } else {
                        (pHeight - it.absoluteValue * density - exclude).toInt()
                    }
                }
                return true
            }
            return false
        }

        when {
            _get("sh", getScreenHeight()) -> Unit
            _get("ph", pHeight) -> Unit
            _get("sw", getScreenWidth()) -> Unit
            _get("pw", pWidth) -> Unit
            _getDp("dip", dpi) -> Unit
            _getDp("dp", dpi) -> Unit
            _getDp("px", 1) -> Unit
        }
    }
    return result
}

/**禁止[Parent]拦截[TouchEvent]*/
fun View?.disableParentInterceptTouchEvent(disable: Boolean = true) {
    this?.parent?.requestDisallowInterceptTouchEvent(disable)
}

/**调整矩形的宽高到指定的值*/
fun RectF.adjustSizeWithCenter(width: Float, height: Float) {
    val w = width()
    val h = height()

    val ws = w - width
    val hs = h - height
    inset(ws / 2, hs / 2)
}

/**左上角*/
fun RectF.adjustSizeWithLT(width: Float, height: Float) {
    val w = width()
    val h = height()

    val ws = w - width
    val hs = h - height

    right -= ws
    bottom -= hs
}