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
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.appendDslItem

/**
 * 数字键盘弹窗window, 支持小数/支持+-自增
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/14
 */
class NumberKeyboardPopupConfig : ShadowAnchorPopupConfig() {

    companion object {

        /**输入延迟*/
        var DEFAULT_INPUT_DELAY = 300L

        /**键盘输入解析*/
        fun keyboardInputValueParse(
            newValueBuild: StringBuilder,
            firstValue: String?,
            shakeInput: Boolean,
            op: String,
            step: Float
        ): String {
            val oldValue: String? = if (shakeInput) {
                firstValue
            } else {
                newValueBuild.toString()
            }
            when (op) {
                "-0" -> {
                    //退格操作
                    if (newValueBuild.isNotEmpty()) {
                        newValueBuild.deleteCharAt(newValueBuild.lastIndex)
                    } else if (oldValue.isNullOrEmpty()) {
                        //no
                    } else {
                        newValueBuild.append(oldValue.substring(0, oldValue.lastIndex))
                    }
                }
                "-1" -> {
                    //自减
                    newValueBuild.clear()
                    newValueBuild.append(oldValue?.toFloatOrNull()?.run { "${this - step}" }
                        ?: "${-step}")
                }
                "+1" -> {
                    //自增
                    newValueBuild.clear()
                    newValueBuild.append(oldValue?.toFloatOrNull()?.run { "${this + step}" }
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
    }

    /**点击按键的回调
     * [number] -0,表示退格
     * [number] -1,表示--
     * [number] +1,表示++
     * @return true 表示自动销毁window*/
    var onClickNumberAction: (number: String) -> Boolean = { onInputValue(it) }

    /**回调此方法, 直接拿到输入后的值
     * 不能覆盖[onClickNumberAction]方法*/
    var onNumberResultAction: (number: Float) -> Unit = { }

    /**自动绑定输入的控件*/
    var keyboardBindTextView: TextView? = null

    /**自增的步长*/
    var incrementStep: Float = 1f

    /**绑定限流*/
    var bindPendingDelay = DEFAULT_INPUT_DELAY

    /**是否激活抖动输入, 关闭之后, 那就相当于普通键盘
     * [bindPendingDelay] 限流依旧有效
     * */
    var enableShakeInput: Boolean = false

    //意图
    var _pendingRunnable: Runnable? = null

    val newValueBuilder: StringBuilder = StringBuilder()

    val mainHandle = Handler(Looper.getMainLooper())

    init {
        contentLayoutId = R.layout.lib_keyboard_number_keyboard_layout
    }

    override fun initContentLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        super.initContentLayout(window, viewHolder)
        val list = mutableListOf<DslAdapterItem>()
        for (i in 1..9) {
            list.add(createNumberItem(window, "$i"))
        }
        list.add(createNumberItem(window, "."))
        list.add(createNumberItem(window, "0"))
        list.add(createNumberImageItem(window))
        list.add(createNumberIncrementItem(window))
        viewHolder.group(R.id.lib_flow_layout)?.appendDslItem(list)

        if (!enableShakeInput) {
            newValueBuilder.clear()
            newValueBuilder.append(keyboardBindTextView?.text?.toString() ?: "")
        }
    }

    /**数字键和.号*/
    fun createNumberItem(window: TargetWindow, number: String): DslAdapterItem =
        KeyboardNumberItem().apply {
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
            itemClick = {
                if (onClickNumberAction("-0")) {
                    if (window is PopupWindow) {
                        window.dismiss()
                    }
                }
            }
        }

    /**自增/自减键*/
    fun createNumberIncrementItem(window: TargetWindow): DslAdapterItem =
        KeyboardNumberIncrementItem().apply {
            itemIncrementAction = {
                if (onClickNumberAction(
                        if (it) "+1" else "-1"
                    )
                ) {
                    if (window is PopupWindow) {
                        window.dismiss()
                    }
                }
            }
        }

    /**自动绑定
     * [onClickNumberAction]*/
    fun onInputValue(value: String): Boolean {
        _pendingRunnable?.let { mainHandle.removeCallbacks(it) }
        val result = keyboardInputValueParse(
            newValueBuilder,
            keyboardBindTextView?.text?.toString(),
            enableShakeInput,
            value,
            incrementStep
        ).apply {
            toFloatOrNull()?.let { toValue ->
                _pendingRunnable = Runnable {
                    onNumberResultAction(toValue)
                    if (enableShakeInput) {
                        //清空输入
                        newValueBuilder.clear()
                    }
                }
                mainHandle.postDelayed(_pendingRunnable!!, bindPendingDelay)
            }
        }
        keyboardBindTextView?.text = result
        return false
    }
}

/**Dsl*/
fun Context.keyboardNumberWindow(
    anchor: View?,
    config: NumberKeyboardPopupConfig.() -> Unit
): TargetWindow {
    val popupConfig = NumberKeyboardPopupConfig()
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
}