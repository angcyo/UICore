package com.angcyo.widget.edit

import android.graphics.Canvas
import android.text.TextPaint
import android.widget.EditText
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.orString
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.mW

/**
 * 绘制一个淡色的输入提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class InputTipEditDelegate(editText: EditText) : FocusEditDelegate(editText) {

    /**需要匹配的数据池*/
    var inputHitTipTextList: MutableList<String> = mutableListOf()

    /**命中后的数据*/
    var hitInputText: String? = null

    /**匹配命中的输入文本*/
    fun _hitInputText(origin: String) {
        hitInputText = inputHitTipTextList.find { it.startsWith(origin) }
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        _hitInputText(text.orString(""))
    }

    override fun onFocusChanged(focused: Boolean) {
        super.onFocusChanged(focused)
        if (!focused) {
            //丢失焦点
            if (hitInputText != null) {
                //命中文本了
                editText.setText(hitInputText)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val view = editText
        val layout = view.layout

        val needDrawInputTip =
            layout.lineCount == 1 && view.isFocused && !hitInputText.isNullOrEmpty()

        if (needDrawInputTip) {
            //需要绘制

            val viewWidth = view.mW()
            val viewHeight = view.mH()

            val scrollX = view.scrollX
            val scrollY = view.scrollY

            val textPaint: TextPaint = view.paint
            val originText = view.text?.toString() ?: ""

            val originTextWidth = textPaint.measureText(originText, 0, originText.length)
            val originTextDrawLeft = view.paddingLeft + layout.getLineLeft(0)

            //只处理了竖直居中的情况
            canvas.save()
            val oldPaintColor = textPaint.color
            textPaint.color = view.currentTextColor.alpha(0x40)

            val lineHeight: Int = layout.getLineDescent(0) - layout.getLineAscent(0)
            val top: Int = viewHeight / 2 - lineHeight / 2
            val bottom: Int =
                view.paddingTop + (viewHeight - view.paddingTop - view.paddingBottom) / 2 + lineHeight / 2

            //只绘制末尾的文本区域
            canvas.clipRect(
                originTextWidth + originTextDrawLeft,
                0f,
                viewWidth.toFloat(),
                viewHeight.toFloat()
            )

            canvas.drawText(
                hitInputText!!,
                originTextDrawLeft.toFloat(), bottom - layout.getLineDescent(0).toFloat(), textPaint
            )

            canvas.restore()
            textPaint.color = oldPaintColor
        }
    }
}