package com.angcyo.library.extend

import android.graphics.drawable.Drawable

/**
 * 默认是在左边绘制的[Drawable]
 * [IToRightDrawable]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/12/30
 */
interface IToDrawable {
    fun toDrawable(): Drawable?
}