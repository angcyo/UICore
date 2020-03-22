package com.angcyo.library.ex

import android.content.Context
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

fun View.calcLayoutWidthHeight(
    rLayoutWidth: String?, rLayoutHeight: String?,
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
    rLayoutWidth: String?, rLayoutHeight: String?,
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
    rMaxHeight: String?,
    parentWidth: Int,
    parentHeight: Int,
    exclude: Int = 0
): Int {
    return calcSize(rMaxHeight, parentWidth, parentHeight, exclude)
}

/**[exp] 计算表达式, 支持 sh ph px dip, 正数是倍数, 负数是减去倍数的值*/

fun calcSize(
    exp: String?,
    pWidth: Int = _screenWidth,
    pHeight: Int = _screenHeight,
    exclude: Int = 0
): Int {
    return app().calcSize(exp, pWidth, pHeight, exclude)
}

fun View.calcSize(
    exp: String?,
    pWidth: Int = getScreenWidth(),
    pHeight: Int = getScreenHeight(),
    exclude: Int = 0
): Int {
    return context.calcSize(exp, pWidth, pHeight, exclude)
}

fun Context.calcSize(
    exp: String?,
    pWidth: Int = getScreenWidth(),
    pHeight: Int = getScreenHeight(),
    exclude: Int = 0
): Int {
    var result = -1
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
            _get("sh", getScreenHeight()) -> {
            }
            _get("ph", pHeight) -> {
            }
            _get("sw", getScreenWidth()) -> {
            }
            _get("pw", pWidth) -> {
            }
            _getDp("dip", dpi) -> {
            }
            _getDp("px", 1) -> {

            }
        }
    }
    return result
}
