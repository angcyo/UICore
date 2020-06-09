package com.angcyo.item.style

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.angcyo.library.ex.undefined_color
import com.angcyo.library.ex.undefined_float
import com.angcyo.widget.base.setBoldText

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

    /**生效*/
    override fun updateStyle(view: View) {
        super.updateStyle(view)

        if (view is TextView) {
            with(view) {
                text = this@TextStyleConfig.text
                hint = this@TextStyleConfig.hint

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
            }
        }
    }
}