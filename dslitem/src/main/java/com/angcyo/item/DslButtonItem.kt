package com.angcyo.item

import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.undefined_float
import com.angcyo.widget.DslButton
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.button
import com.angcyo.widget.colorState

/**
 * 带有[DslButton]的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslButtonItem : DslAdapterItem() {

    var itemButtonText: CharSequence? = null
        set(value) {
            field = value
            itemButtonStyle.text = value
        }

    var itemButtonStyle = ButtonStyleConfig().apply {
        textGravity = Gravity.CENTER
    }

    var itemButtonConfig: (DslButton) -> Unit = {

    }

    init {
        itemLayoutId = R.layout.dsl_button_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.button(R.id.lib_button)?.apply {
            itemButtonStyle.updateStyle(this)
            itemButtonConfig(this)

            setOnClickListener(_clickListener)
        }
    }

    open fun configButtonStyle(action: ButtonStyleConfig.() -> Unit) {
        itemButtonStyle.action()
    }
}

open class ButtonStyleConfig : TextStyleConfig() {

    companion object {
        //主题渐变样式
        val BUTTON_STYLE_THEME = 1

        //白底样式
        val BUTTON_STYLE_WHITE = 2

        //边框->主题渐变
        val BUTTON_STYLE_FILL = 3
    }

    /**特定样式*/
    var style: Int = BUTTON_STYLE_THEME

    /**样式[BUTTON_STYLE_THEME] [BUTTON_STYLE_FILL]时, 渐变的颜色*/
    var styleThemeColors = intArrayOf(
        _color(R.color.colorPrimary),
        _color(R.color.colorPrimaryDark)
    )

    /**样式[BUTTON_STYLE_THEME] [BUTTON_STYLE_FILL]时, 文本的颜色*/
    var styleThemeTextColor = Color.WHITE

    /**样式[BUTTON_STYLE_WHITE]时, solid的颜色*/
    var styleWhiteSolidColor = Color.WHITE

    /**样式[BUTTON_STYLE_FILL]时, solid的颜色*/
    var styleFillSolidColor = Color.TRANSPARENT

    /**样式[BUTTON_STYLE_WHITE] [BUTTON_STYLE_FILL]时, 文本的颜色*/
    var styleWhiteTextColor = _color(R.color.text_general_color)

    /**[BUTTON_STYLE_FILL]*/
    var styleFillStrokeWidth = 2 * dpi

    /**[BUTTON_STYLE_FILL]*/
    var styleFillStrokeColor = _color(R.color.colorPrimary)

    var buttonRadius: Float = undefined_float

    init {
        themeStyle()
    }

    override fun updateStyle(textView: TextView) {
        super.updateStyle(textView)

        if (textView is DslButton) {

            //不支持不同圆角大小的样式
            if (buttonRadius == undefined_float) {
                buttonRadius = textView.normalRadii.first()
            }
            textView.setButtonRadius(buttonRadius)

            when (style) {
                BUTTON_STYLE_THEME -> {
                    textView.enableTextStyle = true
                    textView.setButtonGradientColors(styleThemeColors)
                    textView.setButtonTextColor(textColor)
                    textView.setButtonStrokeWidth(0)
                }
                BUTTON_STYLE_WHITE -> {
                    textView.enableTextStyle = true
                    textView.setButtonGradientColors(null)
                    textView.setButtonSolidColor(styleWhiteSolidColor)
                    textView.setButtonTextColor(textColor)
                    textView.setButtonStrokeWidth(0)
                }
                BUTTON_STYLE_FILL -> {
                    textView.enableTextStyle = false
                    textView.setButtonGradientColors(null)
                    textView.setButtonSolidColor(styleFillSolidColor)
                    textView.setButtonStrokeWidth(styleFillStrokeWidth)
                    textView.setButtonStrokeColor(styleFillStrokeColor)

                    textView.pressStrokeWidth = 0
                    textView.pressGradientColors = styleThemeColors
                }
            }

            textView.updateDrawable()
        }
    }

    /**使用主题样式*/
    fun themeStyle() {
        style = BUTTON_STYLE_THEME
        textColor = styleThemeTextColor
    }

    /**白色样式*/
    fun whiteStyle() {
        style = BUTTON_STYLE_WHITE
        textColor = styleWhiteTextColor
    }

    /**填充样式*/
    fun fillStyle() {
        style = BUTTON_STYLE_FILL
        textColors = colorState(
            DslButton.ATTR_PRESSED to styleThemeTextColor,
            DslButton.ATTR_NORMAL to styleWhiteTextColor
        )
    }
}