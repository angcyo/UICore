package com.angcyo.widget.base

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.angcyo.library.L
import com.angcyo.library.ex.add
import com.angcyo.library.ex.remove
import com.angcyo.library.utils.getMember
import java.util.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun TextView.setLeftIco(id: Int) {
    setLeftIco(loadDrawable(id))
}

fun TextView.setLeftIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable?> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        drawable,
        compoundDrawables[1],
        compoundDrawables[2],
        compoundDrawables[3]
    )
}

fun TextView.setTopIco(id: Int) {
    setTopIco(loadDrawable(id))
}

fun TextView.setTopIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable?> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        compoundDrawables[0],
        drawable,
        compoundDrawables[2],
        compoundDrawables[3]
    )
}

fun TextView.setRightIco(@DrawableRes id: Int) {
    setRightIco(loadDrawable(id))
}

fun TextView.setRightIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable?> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        compoundDrawables[0],
        compoundDrawables[1],
        drawable,
        compoundDrawables[3]
    )
}

fun TextView.setBottomIco(id: Int) {
    setBottomIco(loadDrawable(id))
}

fun TextView.setBottomIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable?> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        compoundDrawables[0],
        compoundDrawables[1],
        compoundDrawables[2],
        drawable
    )
}


/**
 * 设置是否显示删除线
 */
fun TextView.setDeleteLine(bool: Boolean) {
    addFlags(bool, Paint.STRIKE_THRU_TEXT_FLAG)
}

/**
 * 设置是否显示下划线
 */
fun TextView.setUnderLine(bool: Boolean) {
    addFlags(bool, Paint.UNDERLINE_TEXT_FLAG)
}

/**
 * 设置是否加粗文本
 */
fun TextView.setBoldText(bool: Boolean) {
    addFlags(bool, Paint.FAKE_BOLD_TEXT_FLAG)
}

/**
 * 设置是否斜体
 */
fun TextView.setItalic(bool: Boolean) {
    if (bool) {
        setTypeface(typeface, Typeface.ITALIC)
    } else {
        setTypeface(typeface, Typeface.NORMAL)
        //setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL);
    }
}

fun TextView.addFlags(add: Boolean, flat: Int) {
    val paint: TextPaint = paint
    paint.addPaintFlags(add, flat)
    postInvalidate()
}

fun Paint.addPaintFlags(add: Boolean, flat: Int) {
    flags = if (add) {
        flags.add(flat)
    } else {
        flags.remove(flat)
    }
}

/**枚举所有[span]*/
fun TextView.spans(action: (index: Int, span: Any) -> Unit) {
    val text = text
    if (text is Spanned) {
        val spans = text.getSpans(0, text.length, Any::class.java)
        spans.forEachIndexed(action)
    }
}

/**清空所有[TextWatcher]*/
fun TextView.clearListeners() {
    try {
        val mListeners: ArrayList<*>? =
            getMember(TextView::class.java, "mListeners") as? ArrayList<*>
        mListeners?.clear()
    } catch (e: Exception) {
        L.e(e)
    }
}

fun TextView?.setMaxLine(maxLine: Int = 1) {
    this?.run {
        if (maxLine <= 1) {
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
        } else {
            isSingleLine = false
            maxLines = maxLine
        }
    }
}

/**单行输入切换*/
fun TextView?.setSingleLineMode(singleLine: Boolean = true, maxLength: Int = -1) {
    this?.run {
        if (singleLine) {
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
        } else {
            isSingleLine = false
            maxLines = Int.MAX_VALUE
        }
        if (maxLength >= 0) {
            setFilter(InputFilter.LengthFilter(maxLength))
        }
    }
}

fun TextView.addFilter(filter: InputFilter) {
    val oldFilters = filters
    val newFilters = arrayOfNulls<InputFilter>(oldFilters.size + 1)
    System.arraycopy(oldFilters, 0, newFilters, 0, oldFilters.size)
    newFilters[oldFilters.size] = filter
    filters = newFilters
}

fun TextView.setFilter(filter: InputFilter, update: Boolean = true) {
    val newFilters = arrayOfNulls<InputFilter>(1)
    newFilters[0] = filter
    filters = newFilters

    if (update) {
        text = text
    }
}