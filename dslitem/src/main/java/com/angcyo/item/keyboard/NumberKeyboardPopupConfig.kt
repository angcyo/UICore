package com.angcyo.item.keyboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.popup.ShadowAnchorPopupConfig
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.R
import com.angcyo.item.style.itemText
import com.angcyo.library.L
import com.angcyo.library.annotation.DSL
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.*
import com.angcyo.library.utils.getFloatNum
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.appendDslItem
import com.angcyo.widget.span.span

/**
 * 数字键盘弹窗window, 支持小数/支持+-自增
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/14
 */
class NumberKeyboardPopupConfig : ShadowAnchorPopupConfig() {

    companion object {

        //---

        /**退格*/
        const val CONTROL_BACKSPACE = "-0"

        /**自增*/
        const val CONTROL_INCREMENT = "+1"

        /**自减*/
        const val CONTROL_DECREMENT = "-1"

        /**快速自增*/
        const val CONTROL_FAST_INCREMENT = "++1"

        /**快速自减*/
        const val CONTROL_FAST_DECREMENT = "--1"

        //---

        /**需要点输入, 小数输入*/
        const val STYLE_DECIMAL = 0x01

        /**需要自增/自减*/
        const val STYLE_INCREMENT = 0x02

        /**输入延迟*/
        var DEFAULT_INPUT_DELAY = 2000L

        /**键盘输入解析*/
        fun keyboardInputValueParse(
            newValueBuild: StringBuilder,
            firstValue: String?,
            shakeInput: Boolean,
            op: String,
            step: Float,
            longStep: Float,
        ): String {
            val oldValue: String? = if (shakeInput) {
                firstValue
            } else {
                newValueBuild.toString()
            }
            when (op) {
                CONTROL_BACKSPACE -> {
                    //退格操作
                    if (newValueBuild.isNotEmpty()) {
                        newValueBuild.deleteCharAt(newValueBuild.lastIndex)
                    } else if (oldValue.isNullOrEmpty()) {
                        //no
                    } else {
                        newValueBuild.append(oldValue.substring(0, oldValue.lastIndex))
                    }
                }

                CONTROL_DECREMENT -> {
                    //自减
                    newValueBuild.clear()
                    newValueBuild.append(oldValue?.toDoubleOrNull()?.run { "${this - step}" }
                        ?: "${-step}")
                }

                CONTROL_INCREMENT -> {
                    //自增
                    newValueBuild.clear()
                    newValueBuild.append(oldValue?.toDoubleOrNull()?.run { "${this + step}" }
                        ?: "$step")
                }

                CONTROL_FAST_DECREMENT -> {
                    //长按自减
                    newValueBuild.clear()
                    newValueBuild.append(oldValue?.toDoubleOrNull()?.run { "${this - longStep}" }
                        ?: "${-step}")
                }

                CONTROL_FAST_INCREMENT -> {
                    //长按自增
                    newValueBuild.clear()
                    newValueBuild.append(oldValue?.toDoubleOrNull()?.run { "${this + longStep}" }
                        ?: "$step")
                }

                "." -> {
                    val value = newValueBuild.toString()
                    if (value.contains(".")) {
                        //如果已经包含了点, 则忽略
                    } else {
                        newValueBuild.append(op)
                    }
                }

                else -> {
                    newValueBuild.append(op)
                }
            }
            val result = newValueBuild.toString()
            L.v("input result:$result")
            return result
        }

        /**是否是控制输入[number]*/
        fun isControlInputNumber(number: String): Boolean =
            number == CONTROL_BACKSPACE || number == CONTROL_DECREMENT || number == CONTROL_FAST_DECREMENT || number == CONTROL_INCREMENT || number == CONTROL_FAST_INCREMENT || number == "."
    }

    /**按键的大小*/
    var numberItemSize: Int? = null

    /**点击按键的回调
     * [number] -0,表示退格
     * [number] -1,表示--
     * [number] --1,表示长按--
     * [number] +1,表示++
     * [number] ++1,表示长按++
     * @return true 表示自动销毁window*/
    var onClickNumberAction: (number: String) -> Boolean = { onInputValue(it) }

    /**回调此方法, 直接拿到输入后的值
     * 不能覆盖[onClickNumberAction]方法*/
    var onNumberResultAction: (number: Float) -> Unit = { }

    /**格式化文本内容, 比如90°, 则应该返回90*/
    var onFormatTextAction: (text: String) -> String = { text ->
        if (text.endsWith(".") || !text.contains(".")) {
            //xx. 的情况
            text
        } else {
            val float = text.toDoubleOrNull()
            if (float == null) {
                "${text.getFloatNum() ?: text}"
            } else {
                "$float"
            }
        }
    }

    /**计算之后的结果, 格式化*/
    var onFormatValueAction: (value: String) -> String = { value ->
        value
    }

    /**键盘输入样式, 用来控制需要显示那些按键
     * [STYLE_DECIMAL]
     * [STYLE_INCREMENT]
     * */
    var keyboardStyle: Int = 0xff

    /**自动绑定输入的控件*/
    var keyboardBindTextView: TextView? = null

    /**自增的步长*/
    var incrementStep: Float = 1f

    /**长按的自增长步长*/
    var longIncrementStep: Float = 10f

    /**绑定限流, 如果需要实时输入, 请关闭限流
     * <=0 关闭限流
     * */
    var bindPendingDelay = DEFAULT_INPUT_DELAY

    /**是否激活抖动输入, 关闭之后, 那就相当于普通键盘.
     *
     * 抖动输入, 表示每次输出上屏时, 都自动清除旧数据, 这样每次输入都是从空的输入开始
     *
     * [bindPendingDelay] 限流依旧有效
     * */
    var enableShakeInput: Boolean = false

    /**首次输入时, 覆盖输入.
     * 比如:默认值是888, 那么首次输入多少就是多少
     * */
    var firstOverrideInput: Boolean = true

    //意图
    var _pendingRunnable: Runnable? = null

    val newValueBuilder: StringBuilder = StringBuilder()

    val mainHandle = Handler(Looper.getMainLooper())

    init {
        contentLayoutId = R.layout.lib_keyboard_number_keyboard_layout
    }

    /**默认值*/
    var _firstValue: String = ""

    override fun initContentLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        super.initContentLayout(window, viewHolder)
        val list = mutableListOf<DslAdapterItem>()
        for (i in 1..9) {
            list.add(createNumberItem(window, "$i"))
        }
        if (keyboardStyle.have(STYLE_DECIMAL)) {
            list.add(createNumberItem(window, "."))
        } else {
            list.add(DslAdapterItem().apply {
                initKeyboardItem()
                itemLayoutId = R.layout.lib_keyboard_empty_item_layout
            })
        }
        list.add(createNumberItem(window, "0"))
        list.add(createNumberImageItem(window))
        if (keyboardStyle.have(STYLE_INCREMENT)) {
            list.add(createNumberIncrementItem(window))
        }
        viewHolder.group(R.id.lib_flow_layout)?.appendDslItem(list)

        //
        _firstValue = onFormatTextAction(keyboardBindTextView?.text?.toString() ?: "")

        if (firstOverrideInput) {
            newValueBuilder.clear()
            _tipClearInput()
        } else if (!enableShakeInput) {
            newValueBuilder.clear()
            newValueBuilder.append(_firstValue)
        }
    }

    /**提示当前的输入会清空已存在的内容*/
    fun _tipClearInput() {
        keyboardBindTextView?.apply {
            //采用背景颜色提醒
            text = span {
                append(text) {
                    backgroundColor = _color(R.color.colorAccent).alphaRatio(0.5f)
                }
            }
        }
    }

    private fun DslAdapterItem.initKeyboardItem() {
        numberItemSize?.let { size ->
            itemWidth = size
            itemHeight = size
        }
    }

    /**数字键和.号*/
    fun createNumberItem(window: TargetWindow, number: String): DslAdapterItem =
        KeyboardNumberItem().apply {
            initKeyboardItem()
            itemText = number
            itemClick = {
                if (onClickNumberAction(number)) {
                    if (window is PopupWindow) {
                        window.dismiss()
                    }
                }
            }
        }

    /**回退键*/
    fun createNumberImageItem(window: TargetWindow): DslAdapterItem =
        KeyboardNumberImageItem().apply {
            initKeyboardItem()
            itemClick = {
                if (onClickNumberAction(CONTROL_BACKSPACE)) {
                    if (window is PopupWindow) {
                        window.dismiss()
                    }
                }
            }
        }

    /**自增/自减键*/
    fun createNumberIncrementItem(window: TargetWindow): DslAdapterItem =
        KeyboardNumberIncrementItem().apply {
            itemIncrementAction = { plus, longPress ->
                val value = if (longPress) {
                    if (plus) CONTROL_FAST_INCREMENT else CONTROL_FAST_DECREMENT
                } else {
                    if (plus) CONTROL_INCREMENT else CONTROL_DECREMENT
                }
                if (onClickNumberAction(value)) {
                    if (window is PopupWindow) {
                        window.dismiss()
                    }
                }
            }
        }

    var _isBeforeControlInput = false

    /**自动绑定
     * [onClickNumberAction]*/
    fun onInputValue(value: String): Boolean {
        _pendingRunnable?.let { mainHandle.removeCallbacks(it) }

        val defaultValue = onFormatTextAction(keyboardBindTextView?.text?.toString() ?: "")

        if (!enableShakeInput) {
            if (isControlInputNumber(value) || _isBeforeControlInput) {
                newValueBuilder.clear()
                newValueBuilder.append(defaultValue)
            }
            _isBeforeControlInput = isControlInputNumber(value)
        }

        val result = keyboardInputValueParse(
            newValueBuilder, defaultValue, enableShakeInput, value, incrementStep, longIncrementStep
        ).apply {
            toFloatOrNull()?.let { toValue ->
                _pendingRunnable = Runnable {
                    _pendingRunnable = null
                    onNumberResultAction(toValue)
                    if (enableShakeInput) {
                        //清空输入
                        newValueBuilder.clear()
                        _tipClearInput()
                    }
                }
                if (bindPendingDelay > 0) {
                    mainHandle.postDelayed(_pendingRunnable!!, bindPendingDelay)
                } else {
                    if (value == CONTROL_BACKSPACE) {
                        //退格操作, 强制限流
                        mainHandle.postDelayed(_pendingRunnable!!, DEFAULT_INPUT_DELAY)
                    } else {
                        _pendingRunnable?.run()
                    }
                }
            }
        }
        val value = onFormatValueAction(result)
        keyboardBindTextView?.text = value
        if (!enableShakeInput) {
            newValueBuilder.clear()
            newValueBuilder.append(value)
        }
        return false
    }

    /**移除键盘样式
     * [com.angcyo.item.keyboard.NumberKeyboardPopupConfig.STYLE_DECIMAL]
     * [com.angcyo.item.keyboard.NumberKeyboardPopupConfig.STYLE_INCREMENT]
     * */
    fun removeKeyboardStyle(style: Int) {
        keyboardStyle = keyboardStyle.remove(style)
    }

    override fun dismissPopupWindow(popupWindow: PopupWindow) {
        super.dismissPopupWindow(popupWindow)
        if (bindPendingDelay > 0) {
            _pendingRunnable?.let {
                mainHandle.removeCallbacks(it)
                it.run()//立即执行
            }
        }
    }
}

/**Dsl
 * [com.angcyo.item.keyboard.NumberKeyboardPopupConfig.onClickNumberAction]
 * [com.angcyo.item.keyboard.NumberKeyboardPopupConfig.onNumberResultAction]
 * */
@DSL
fun Context.keyboardNumberWindow(
    anchor: View?, config: NumberKeyboardPopupConfig.() -> Unit
): TargetWindow {
    //显示键盘之前, 需要隐藏键盘
    lastContext.hideSoftInput()
    val popupConfig = NumberKeyboardPopupConfig()
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
}