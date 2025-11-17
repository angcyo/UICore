package com.angcyo.item.keyboard

import android.app.Dialog
import android.content.Context
import android.text.InputFilter
import android.view.View
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
import com.angcyo.library.ex._color
import com.angcyo.library.ex._string
import com.angcyo.library.ex.add
import com.angcyo.library.ex.alphaRatio
import com.angcyo.library.ex.clampValue
import com.angcyo.library.ex.decimal
import com.angcyo.library.ex.find
import com.angcyo.library.ex.getValueFrom
import com.angcyo.library.ex.have
import com.angcyo.library.ex.inflate
import com.angcyo.library.ex.remove
import com.angcyo.library.ex.toStr
import com.angcyo.library.ex.visible
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.addFilter
import com.angcyo.widget.base.appendDslItem
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.span.span
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 自定义的数字键盘
 *
 * [NumberKeyboardPopupConfig]
 * [NumberKeyboardDialogConfig]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/12/04
 */
class NumberKeyboardDialogConfig : BaseDialogConfig() {

    companion object {
        /**默认小数点后几位*/
        var numberDecimalCount = 2
    }

    /**是否要显示标题*/
    var showNumberDialogTitle: Boolean = false

    /**当前的数值*/
    var numberValue: Any? = null
        set(value) {
            if (value !is Unit) {
                field = value
                dialogMessage = if (numberValueType is Long || numberValueType is Int) {
                    value?.toString()?.toFloatOrNull()?.roundToInt()?.toString()
                } else {
                    value?.toString()?.toFloatOrNull()?.decimal(decimalCount, fadedUp = isFadedUp)
                }
            }
        }

    /**用来判断当前值的类型, 具有类型的数值*/
    var numberValueType: Any? = null
        get() = field ?: numberValue

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
    var maxInputLength = 6

    /**如果是小数, 则顶多能输入到小数点后几位*/
    var decimalCount = numberDecimalCount

    /**如果是小数, 是否四舍五入*/
    var isFadedUp: Boolean = true

    /**
     * @return true 表示自动销毁window
     * */
    var onClickNumberAction: (number: String) -> Boolean = { onInputValue(it) }

    /**
     * 输入完成的回调
     * @return true 表示拦截默认处理
     * */
    var onNumberResultAction: (number: Any?) -> Boolean = { false }

    /**首次输入时, 覆盖输入.
     * 比如:默认值是888, 那么首次输入多少就是多少
     * */
    var firstOverrideInput: Boolean = true

    /**是否是首次输入*/
    private var _isFirstInput: Boolean = true

    /**是否选中了文本*/
    private var _isSelectInput: Boolean = false

    /**编辑的值放这里*/
    private val resultBuilder = StringBuilder()

    /**当前输入后的值*/
    private val _numberValue: Any?
        get() {
            updateDialogMessage(true)//clamp
            val valueStr = resultBuilder.toString()
            return getValueFrom(valueStr, numberValueType)
        }

    init {
        dialogLayoutId = R.layout.dialog_number_keyboard_layout
        positiveButtonText = _string(R.string.ui_finish)
        dimAmount = 0.2f//不需要背景变暗

        positiveButtonListener = { dialog, _ ->
            if (!onNumberResultAction(_numberValue)) {
                dialog.hideSoftInput()
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        if (!showNumberDialogTitle) {
            //不显示则清空标题
            dialogTitle = null
        }
        super.initDialogView(dialog, dialogViewHolder)
    }

    override fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initControlLayout(dialog, dialogViewHolder)

        if (numberValueType is Long || numberValueType is Int) {
            //移除小数输入
            removeDecimal()
        }

        dialogViewHolder.tv(R.id.lib_hint_text_view)?.apply {
            val hint = numberInputHint ?: defNumberHint()
            text = hint
            visible(hint != null)
        }
        dialogViewHolder.tv(R.id.dialog_message_view)?.apply {
            visible()//默认显示
            //输入限制
            if (maxInputLength >= 0) {
                addFilter(InputFilter.LengthFilter(maxInputLength))
            }
            clickIt {
                _tipClearInput(!_isSelectInput)
            }
        }
        resultBuilder.clear()
        resultBuilder.append(dialogMessage ?: "")
        val list = mutableListOf<DslAdapterItem>()
        for (i in 1..9) {
            list.add(createNumberItem(dialog, "$i"))
        }

        val weight0Num = if (keyboardStyle.have(STYLE_DECIMAL)) 0.3333f else 0.6666f/*list.add(
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
            rootView.find<View>(R.id.lib_keyboard_packup_view)?.apply {
                visible(!keyboardStyle.have(STYLE_PLUS_MINUS))
                clickIt {
                    dialog.dismiss()
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

        if (firstOverrideInput) {
            _tipClearInput()
        }
    }

    private fun defNumberHint(): CharSequence? {
        val tip = _string(R.string.ui_valid_range_tip)
        return if (numberMinValue != null && numberMaxValue != null) {
            "$tip${formatValue(numberMinValue)}-${formatValue(numberMaxValue)}"
        } else if (numberMinValue != null) {
            "$tip${formatValue(numberMinValue)}~"
        } else if (numberMaxValue != null) {
            "$tip~${formatValue(numberMaxValue)}"
        } else {
            null
        }
    }

    /**格式化数值*/
    fun formatValue(value: Any?): String? {
        if (value == null) {
            return null
        }
        if (value is Float) {
            return value.decimal(decimalCount, fadedUp = isFadedUp)
        }
        if (value is Double) {
            return value.decimal(decimalCount, fadedUp = isFadedUp)
        }
        return value.toString()
    }

    private fun updateDialogMessage(clamp: Boolean = true) {
        _dialogViewHolder?.tv(R.id.dialog_message_view)?.apply {
            text = if (clamp) clampValue(
                resultBuilder.toString(), numberValueType, numberMinValue, numberMaxValue
            ).toString() else resultBuilder.toString()

            //输入限制
            resultBuilder.clear()
            resultBuilder.append(text)
        }
    }

    /**提示当前的输入会清空已存在的内容*/
    fun _tipClearInput(tip: Boolean = true) {
        _isSelectInput = false
        _isFirstInput = false
        _dialogViewHolder?.tv(R.id.dialog_message_view)?.apply {
            text = span {
                append(text.toStr()) {
                    if (tip) {
                        backgroundColor = _color(R.color.colorAccent).alphaRatio(0.5f)
                        _isSelectInput = true
                        _isFirstInput = true
                    }
                }
            }
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
    private fun onInputValue(value: String): Boolean {
        if (firstOverrideInput && _isFirstInput) {
            resultBuilder.clear()
        }
        _isFirstInput = false
        keyboardInputValueParse(resultBuilder, null, false, value, 1f, 10f, decimalCount)

        /*var clamp = !NumberKeyboardPopupConfig.isControlInputNumber(value)
        if (numberValueType is Float) {
            if (isFloatMinAdjustValue(resultBuilder.toString(), numberMinValue)) {
                clamp = false
            }
        }*/
        updateDialogMessage(false)
        return false
    }

    /**用最小值, 最大值和对应的比例, 更新[numberValue]
     * [progress] 进度[0~100] [min~max]值
     * [fraction] [0~1f]比例*/
    fun updateProgressValue(progress: Float, fraction: Float? = null) {
        val min = numberMinValue
        val max = numberMaxValue

        val minFloat = min?.toString()?.toFloatOrNull() ?: 0f
        val maxFloat = max?.toString()?.toFloatOrNull() ?: 100f
        val f = fraction ?: ((progress - minFloat) / (maxFloat - minFloat))
        if (min != null && max != null) {
            if (numberValueType is Double) {
                val maxD = max as Double
                val minD = min as Double
                numberValue = minD + (maxD - minD) * f
            } else if (numberValueType is Float) {
                val maxF = max as Float
                val minF = min as Float
                numberValue = minF + (maxF - minF) * f
            } else if (numberValueType is Long) {
                val maxL = max as Long
                val minL = min as Long
                numberValue = (minL + (maxL - minL) * f).roundToLong()
            } else if (numberValueType is Int) {
                val maxI = max as Int
                val minI = min as Int
                numberValue = (minI + (maxI - minI) * f).roundToInt()
            } else {
                numberValue = progress
            }
        } else {
            numberValue = progress
        }
    }

    /**获取当前输入的值, 对应的进度
     * [0~100].[min~max]比例值*/
    fun getProgressValueFraction(): Float? {
        return _numberValue?.toString()?.toFloatOrNull()/*val min = numberMinValue
        val max = numberMaxValue
        if (min != null && max != null) {
            val value = _numberValue ?: return null
            val minFloat = min.toString().toFloat()
            val maxFloat = max.toString().toFloat()
            return (value.toString()
                .toFloat() - minFloat) / (maxFloat - minFloat) * (maxFloat - minFloat)
        }
        return null*/
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
