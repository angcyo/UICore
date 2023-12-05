package com.angcyo.item.keyboard

import android.app.Dialog
import android.content.Context
import android.text.InputFilter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.dialog.hideSoftInput
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.R
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.CONTROL_BACKSPACE
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.CONTROL_CLEAR
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.CONTROL_DECIMAL
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.CONTROL_PLUS_MINUS
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.STYLE_DECIMAL
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.STYLE_PLUS_MINUS
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.keyboardInputValueParse
import com.angcyo.item.style.itemText
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex._string
import com.angcyo.library.ex.add
import com.angcyo.library.ex.clampValue
import com.angcyo.library.ex.find
import com.angcyo.library.ex.getValueFrom
import com.angcyo.library.ex.have
import com.angcyo.library.ex.inflate
import com.angcyo.library.ex.remove
import com.angcyo.library.ex.visible
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.addFilter
import com.angcyo.widget.base.appendDslItem
import com.angcyo.widget.base.clickIt

/**
 * 自定义的数字键盘
 *
 * [NumberKeyboardPopupConfig]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/12/04
 */
class NumberKeyboardDialogConfig : BaseDialogConfig() {

    /**当前的数值*/
    var numberValue: Any? = null
        set(value) {
            field = value
            dialogMessage = value?.toString()
        }

    /**限制输入的最小/最大值*/
    var numberMinValue: Any? = null
    var numberMaxValue: Any? = null

    /**输入提示*/
    var numberInputHint: CharSequence? = null

    /**键盘输入样式, 用来控制需要显示那些按键
     * [STYLE_DECIMAL] 小数
     * [STYLE_PLUS_MINUS] 正负号
     * */
    var keyboardStyle: Int = 0xff.remove(STYLE_PLUS_MINUS)

    /**
     * 最大输入字符限制
     * */
    var maxInputLength = 20

    /**
     * @return true 表示自动销毁window
     * */
    var onClickNumberAction: (number: String) -> Boolean = { onInputValue(it) }

    /**
     * 输入完成的回调
     * @return true 表示拦截默认处理
     * */
    var onNumberResultAction: (number: Any?) -> Boolean = { false }

    /**编辑的值放这里*/
    private val resultBuilder = StringBuilder()

    init {
        dialogLayoutId = R.layout.dialog_number_keyboard_layout
        positiveButtonText = _string(R.string.ui_finish)
        dimAmount = 0f//不需要背景变暗

        positiveButtonListener = { dialog, _ ->
            val valueStr = resultBuilder.toString()
            if (!onNumberResultAction(getValueFrom(valueStr, numberValue))) {
                dialog.hideSoftInput()
                dialog.dismiss()
            }
        }
    }

    override fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initControlLayout(dialog, dialogViewHolder)

        if (numberValue is Long || numberValue is Int) {
            //移除小数输入
            removeDecimal()
        }

        dialogViewHolder.tv(R.id.lib_hint_text_view)?.apply {
            val hint = numberInputHint ?: defNumberHint()
            text = hint
            visible(hint != null)
        }
        dialogViewHolder.tv(R.id.dialog_message_view)?.apply {
            //输入限制
            if (maxInputLength >= 0) {
                addFilter(InputFilter.LengthFilter(maxInputLength))
            }
        }
        resultBuilder.clear()
        resultBuilder.append(dialogMessage ?: "")
        val list = mutableListOf<DslAdapterItem>()
        for (i in 1..9) {
            list.add(createNumberItem(dialog, "$i"))
        }

        val weight0Num =
            if (keyboardStyle.have(STYLE_DECIMAL) && keyboardStyle.have(STYLE_PLUS_MINUS)) 0.3333f
            else if (keyboardStyle.have(STYLE_DECIMAL) || keyboardStyle.have(STYLE_PLUS_MINUS)) {
                0.6666f
            } else 1f
        /*list.add(
            createNumberItem(
                dialog,
                "0",
                weight0Num
            )
        )

        if (keyboardStyle.have(STYLE_DECIMAL)) {
            list.add(
                createNumberItem(
                    dialog,
                    CONTROL_DECIMAL
                )
            )
        }

        if (keyboardStyle.have(STYLE_PLUS_MINUS)) {
            list.add(
                createNumberItem(
                    dialog,
                    CONTROL_PLUS_MINUS
                )
            )
        }*/

        dialogViewHolder.group(R.id.lib_flow_layout)?.apply {
            appendDslItem(list)

            // 0 . ±
            val rootView = inflate(R.layout.lib_number_keyboard_last_item_layout)
            rootView.find<TextView>(R.id.lib_text_view)?.apply {
                updateLayoutParams<LinearLayout.LayoutParams> {
                    weight = weight0Num
                }
                clickIt {
                    onClickNumberAction("0")
                }
            }
            rootView.find<TextView>(R.id.lib_decimal_view)?.apply {
                visible(keyboardStyle.have(STYLE_DECIMAL))
                clickIt {
                    onClickNumberAction(CONTROL_DECIMAL)
                }
            }
            rootView.find<TextView>(R.id.lib_plus_minus_view_view)?.apply {
                visible(keyboardStyle.have(STYLE_PLUS_MINUS))
                clickIt {
                    onClickNumberAction(CONTROL_PLUS_MINUS)
                }
            }
        }

        //back 回退/删除键
        dialogViewHolder.click(R.id.lib_keyboard_backspace_view) {
            onClickNumberAction(CONTROL_BACKSPACE)
        }
        dialogViewHolder.longClick(R.id.lib_keyboard_backspace_view) {
            onClickNumberAction(CONTROL_CLEAR)
        }

        updateDialogMessage()
    }

    private fun defNumberHint(): CharSequence? {
        return if (numberMinValue != null && numberMaxValue != null) {
            "[${numberMinValue}~${numberMaxValue}]"
        } else if (numberMinValue != null) {
            "[${numberMinValue}~"
        } else if (numberMaxValue != null) {
            "~${numberMaxValue}]"
        } else {
            null
        }
    }

    private fun updateDialogMessage() {
        _dialogViewHolder?.tv(R.id.dialog_message_view)?.apply {
            text = clampValue(
                resultBuilder.toString(),
                numberValue,
                numberMinValue,
                numberMaxValue
            ).toString()

            //输入限制
            resultBuilder.clear()
            resultBuilder.append(text)
        }
    }

    /**数字键和.号*/
    fun createNumberItem(dialog: Dialog, number: String, weight: Float? = null): DslAdapterItem =
        KeyboardNumberItem().apply {
            itemLayoutId = R.layout.lib_number_keyboard_item_layout
            itemText = number
            itemWeight = weight
            itemClick = {
                onClickNumberAction(number)
            }
        }

    /**自动绑定
     * [onClickNumberAction]*/
    fun onInputValue(value: String): Boolean {
        keyboardInputValueParse(resultBuilder, null, false, value, 1f, 10f)
        updateDialogMessage()
        return false
    }

    /**移除键盘样式
     * [com.angcyo.item.keyboard.NumberKeyboardPopupConfig.STYLE_DECIMAL]
     * [com.angcyo.item.keyboard.NumberKeyboardPopupConfig.STYLE_INCREMENT]
     * [com.angcyo.item.keyboard.NumberKeyboardPopupConfig.STYLE_PLUS_MINUS]
     * */
    fun removeKeyboardStyle(style: Int) {
        keyboardStyle = keyboardStyle.remove(style)
    }

    /**移除小数输入*/
    fun removeDecimal() {
        removeKeyboardStyle(STYLE_DECIMAL)
    }

    /**移除正负数输入*/
    fun removePlusMinus() {
        removeKeyboardStyle(STYLE_PLUS_MINUS)
    }

    fun addKeyboardStyle(style: Int) {
        keyboardStyle = keyboardStyle.add(style)
    }
}

@DSL
fun Context.numberKeyboardDialog(config: NumberKeyboardDialogConfig.() -> Unit): Dialog {
    return NumberKeyboardDialogConfig().run {
        configBottomDialog(this@numberKeyboardDialog)
        config()
        show()
    }
}
