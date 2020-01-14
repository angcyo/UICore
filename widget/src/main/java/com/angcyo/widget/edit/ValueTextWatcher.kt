package com.angcyo.widget.edit

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.angcyo.library.ex.decimal
import com.angcyo.widget.base.resetSelectionText
import com.angcyo.widget.base.setSelectionLast

/**
 * 限制输入框输入的数值
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/14
 */

class ValueTextWatcher(val editText: EditText? = null) : TextWatcher {

    companion object {
        const val MAX_VALUE = Int.MAX_VALUE.toFloat()
        const val MIN_VALUE = -9_999_999f

        fun install(
            editText: EditText? = null,
            update: Boolean = true,
            action: ValueTextWatcher.() -> Unit = {}
        ) {
            editText?.addTextChangedListener(ValueTextWatcher(editText).apply {
                this.action()
            })
            if (update) {
                editText?.text = editText?.text
            }
        }
    }

    /**最大值*/
    var maxFilterValue: Float = MAX_VALUE

    /**最小值*/
    var minFilterValue: Float = MIN_VALUE

    /**小数点后几位*/
    var decimalCount = 2

    /**是否允许为空, 否则空用0展示*/
    var allowEmpty = false

    fun isDecimalType(): Boolean {
        return decimalCount > 0
    }

    fun Float.toValue(): String {
        if (isDecimalType()) {
            return "${this.decimal(decimalCount)}"
        }
        return "${this.toInt()}"
    }

    override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
        if (sequence.isNullOrBlank()) {
            if (!allowEmpty) {
                editText?.resetSelectionText(0f.toValue(), 0)
                editText?.setSelectionLast()
                return
            }
            return
        }

        sequence.toString().toFloatOrNull()?.let { value ->
            //限制最大数值
            if (value > maxFilterValue) {
                editText?.resetSelectionText(maxFilterValue.toValue(), 0)
                editText?.setSelectionLast()
                return
            }

            //限制最小数值
            if (value < minFilterValue) {
                editText?.resetSelectionText(minFilterValue.toValue(), 0)
                editText?.setSelectionLast()
                return
            }

            //显示小数点后几位
            if (isDecimalType()) {
                val lastIndexOf = sequence.lastIndexOf(".")
                if (lastIndexOf != -1 && sequence.length - lastIndexOf - 1 > decimalCount) {
                    editText?.resetSelectionText(
                        sequence.substring(0, lastIndexOf + decimalCount + 1),
                        0
                    )
                }
            }

            //剔除前面的0
            if (sequence.length > 1) {
                if (sequence[0] == '0' && sequence[1] != '.') {
                    editText?.resetSelectionText(sequence.subSequence(1, sequence.length), 0)
                }
                //如果是小数点开头,补齐0
                if (sequence[0] == '.') {
                    editText?.resetSelectionText("0$sequence", 1)
                }
            }

            return
        }
    }

    override fun afterTextChanged(editable: Editable?) {}

    override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}
}