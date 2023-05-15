package com.angcyo.item.style

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.angcyo.library.UndefinedDrawable
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.tooltipText
import com.angcyo.library.ex.undefined_color
import com.angcyo.library.ex.undefined_float
import com.angcyo.library.ex.undefined_size
import com.angcyo.library.ex.visible
import com.angcyo.widget.base.bottomIco
import com.angcyo.widget.base.leftIco
import com.angcyo.widget.base.rightIco
import com.angcyo.widget.base.setBoldText
import com.angcyo.widget.base.setInputHint
import com.angcyo.widget.base.topIco

/**
 * 文本样式配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class TextStyleConfig : ViewStyleConfig() {

    /**显示的文本内容*/
    var text: CharSequence? = null

    /**长按的提示文本*/
    var tooltipText: CharSequence? = null

    /**当[text]为null时, 隐藏控件*/
    var goneOnTextEmpty: Boolean = false

    /**提示文本内容*/
    var hint: CharSequence? = null
    var textBold: Boolean = false
    var textColor: Int = undefined_color
    var textColors: ColorStateList? = null

    @Pixel
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

            if (text == null) {
                text = view.text
            }

            if (hint == null) {
                hint = view.hint
            }

            if (tooltipText == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tooltipText = view.tooltipText
            }

            with(view) {
                text = this@TextStyleConfig.text
                view.tooltipText(this@TextStyleConfig.tooltipText)

                //[TextView]设置最小宽度和最小高度
                if (viewMinWidth != undefined_size) {
                    minWidth = viewMinWidth

                }
                if (viewMinHeight != undefined_size) {
                    minHeight = viewMinHeight
                }

                if (goneOnTextEmpty) {
                    visible(!this@TextStyleConfig.text.isNullOrEmpty())
                }

                //兼容
                view.setInputHint(this@TextStyleConfig.hint)

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