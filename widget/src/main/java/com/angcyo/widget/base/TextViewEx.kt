package com.angcyo.widget.base

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.TextPaint
import android.widget.TextView
import androidx.annotation.DrawableRes

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun TextView.setLeftIco(id: Int) {
    setLeftIco(getDrawable(id))
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
    setTopIco(getDrawable(id))
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
    setRightIco(getDrawable(id))
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
    setBottomIco(getDrawable(id))
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
    if (add) {
        this.flags = this.flags or flat
    } else {
        this.flags = this.flags and flat.inv()
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