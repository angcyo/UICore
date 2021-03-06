package com.angcyo.item.style

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.angcyo.library.UndefinedDrawable
import com.angcyo.library.ex.undefined_color
import com.angcyo.library.ex.undefined_float
import com.angcyo.library.ex.undefined_size
import com.angcyo.widget.base.*
import org.buffer.android.buffertextinputlayout.BufferTextInputLayout

/**
 * 文本样式配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class TextStyleConfig : ViewStyleConfig() {
    var text: CharSequence? = null
    var hint: CharSequence? = null
    var textBold: Boolean = false
    var textColor: Int = undefined_color
    var textColors: ColorStateList? = null
    var textSize: Float = undefined_float
    var textGravity: Int = Gravity.LEFT or Gravity.CENTER_VERTICAL

    /**四向图标, 需要指定bounds*/
    var leftDrawable: Drawable? = UndefinedDrawable()
    var topDrawable: Drawable? = UndefinedDrawable()
    var rightDrawable: Drawable? = UndefinedDrawable()
    var bottomDrawable: Drawable? = UndefinedDrawable()
    var drawablePadding = undefined_size

    /**生效*/
    override fun updateStyle(view: View) {
        super.updateStyle(view)

        if (view is TextView) {
            with(view) {
                text = this@TextStyleConfig.text

                //兼容
                val parent = view.parent
                if (parent is BufferTextInputLayout) {
                    parent.hint = this@TextStyleConfig.hint
                } else {
                    hint = this@TextStyleConfig.hint
                }

                gravity = textGravity

                setBoldText(textBold)

                //颜色, 防止复用. 所以在未指定的情况下, 要获取默认的颜色.
                val colors = when {
                    this@TextStyleConfig.textColors != null -> {
                        this@TextStyleConfig.textColors
                    }
                    textColor != undefined_color -> {
                        ColorStateList.valueOf(textColor)
                    }
                    else -> {
                        textColors
                    }
                }
                if (colors != this@TextStyleConfig.textColors) {
                    this@TextStyleConfig.textColors = colors
                }
                setTextColor(colors)

                //字体大小同理.
                val size = if (this@TextStyleConfig.textSize != undefined_float) {
                    this@TextStyleConfig.textSize
                } else {
                    textSize
                }
                this@TextStyleConfig.textSize = size
                setTextSize(TypedValue.COMPLEX_UNIT_PX, size)

                //padding
                if (drawablePadding == undefined_size) {
                    drawablePadding = compoundDrawablePadding
                }
                compoundDrawablePadding = drawablePadding

                //四向图标修改
                if (leftDrawable is UndefinedDrawable) {
                    leftDrawable = leftIco()
                }
                if (topDrawable is UndefinedDrawable) {
                    topDrawable = topIco()
                }
                if (rightDrawable is UndefinedDrawable) {
                    rightDrawable = rightIco()
                }
                if (bottomDrawable is UndefinedDrawable) {
                    bottomDrawable = bottomIco()
                }
                setCompoundDrawablesRelative(
                    leftDrawable,
                    topDrawable,
                    rightDrawable,
                    bottomDrawable
                )
            }
        }
    }
}