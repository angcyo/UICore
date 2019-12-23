package com.angcyo.widget.base

import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.DrawableRes

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

public fun TextView.setLeftIco(id: Int) {
    setLeftIco(getDrawable(id))
}

public fun TextView.setLeftIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        drawable,
        compoundDrawables[1],
        compoundDrawables[2],
        compoundDrawables[3]
    )
}

public fun TextView.setTopIco(id: Int) {
    setTopIco(getDrawable(id))
}

public fun TextView.setTopIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        compoundDrawables[0],
        drawable,
        compoundDrawables[2],
        compoundDrawables[3]
    )
}

public fun TextView.setRightIco(@DrawableRes id: Int) {
    setRightIco(getDrawable(id))
}

public fun TextView.setRightIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        compoundDrawables[0],
        compoundDrawables[1],
        drawable,
        compoundDrawables[3]
    )
}

public fun TextView.setBottomIco(id: Int) {
    setBottomIco(getDrawable(id))
}

public fun TextView.setBottomIco(drawable: Drawable?) {
    val compoundDrawables: Array<Drawable> = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(
        compoundDrawables[0],
        compoundDrawables[1],
        compoundDrawables[2],
        drawable
    )
}

