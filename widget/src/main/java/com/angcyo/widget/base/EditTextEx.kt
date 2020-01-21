package com.angcyo.widget.base

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.angcyo.widget.edit.SingleTextWatcher
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/13
 */

/**显示软键盘*/
fun EditText.showSoftInput() {
    isFocusable = true
    requestFocus()
    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.showSoftInput(this, 0)
}

/**是否是密码输入类型*/
fun EditText.isPasswordType(): Boolean {
    val variation =
        inputType and (EditorInfo.TYPE_MASK_CLASS or EditorInfo.TYPE_MASK_VARIATION)
    val passwordInputType = (variation
            == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
    val webPasswordInputType = (variation
            == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
    val numberPasswordInputType = (variation
            == EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)

    return passwordInputType || webPasswordInputType || numberPasswordInputType
}

/**
 * 输入类型是否是数字
 */
fun EditText.isNumberType(): Boolean {
    return inputType and EditorInfo.TYPE_CLASS_NUMBER == EditorInfo.TYPE_CLASS_NUMBER
}

/**
 * 输入类型是否是小数
 */
fun EditText.isDecimalType(): Boolean {
    return inputType and EditorInfo.TYPE_NUMBER_FLAG_DECIMAL == EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
}

/**焦点变化改变监听*/
fun EditText.onFocusChange(listener: (Boolean) -> Unit) {
    onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus -> listener.invoke(hasFocus) }
    listener.invoke(this.isFocused)
}

fun EditText.setInputText(text: CharSequence?) {
    setText(text)
    setSelection(text?.length ?: 0)
}

/**触发删除或回退键*/
fun EditText.del() {
    dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
}

fun TextView?.isEmpty(): Boolean {
    return this == null || TextUtils.isEmpty(string())
}

fun TextView?.string(trim: Boolean = true): String {
    if (this == null) {
        return ""
    }

    var rawText = if (TextUtils.isEmpty(text)) {
        ""
    } else {
        text.toString()
    }
    if (trim) {
        rawText = rawText.trim { it <= ' ' }
    }
    return rawText
}

/**
 * 判断string是否是手机号码
 */
fun EditText.isPhone(regex: String = "^1[3-9]\\d{9}$"): Boolean {
    val string = string()
    if (TextUtils.isEmpty(string)) {
        return false
    }
    return string.matches(regex.toRegex())
}

/**
 * 返回结果表示是否为空
 */
fun EditText.checkEmpty(phoneRegex: String? = null): Boolean {
    if (isEmpty()) {
        error()
        requestFocus()

        if (!isSoftKeyboardShow()) {
            if (parent is FrameLayout &&
                parent.parent is LinearLayout /*TextInputLayout*/) {
                postDelayed({ showSoftInput() }, 200)
            } else {
                showSoftInput()
            }
        }

        return true
    }
    if (phoneRegex != null) {
        if (isPhone(phoneRegex)) {

        } else {
            error()
            requestFocus()
            return true
        }
    }
    return false
}

/**只要文本改变就通知*/
fun EditText.onTextChange(
    defaultText: CharSequence? = null,
    shakeDelay: Long = -1L,//去频限制, 负数表示不开启
    listener: (CharSequence) -> Unit
) {
    addTextChangedListener(object : SingleTextWatcher() {
        var mainHandle: Handler? = null

        val callback: Runnable = Runnable {
            listener.invoke(lastText ?: "")
        }

        init {
            if (shakeDelay >= 0) {
                mainHandle = Handler(Looper.getMainLooper())
            }
        }

        var lastText: CharSequence? = defaultText

        override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(sequence, start, before, count)
            mainHandle?.removeCallbacks(callback)

            val text = sequence?.toString() ?: ""
            if (TextUtils.equals(lastText, text)) {
            } else {
                lastText = text
                if (mainHandle == null) {
                    callback.run()
                } else {
                    mainHandle?.postDelayed(callback, shakeDelay)
                }
            }
        }
    })
}

fun EditText.addFilter(filter: InputFilter) {
    val oldFilters = filters
    val newFilters = arrayOfNulls<InputFilter>(oldFilters.size + 1)
    System.arraycopy(oldFilters, 0, newFilters, 0, oldFilters.size)
    newFilters[oldFilters.size] = filter
    filters = newFilters
}

fun EditText.setFilter(filter: InputFilter, update: Boolean = true) {
    val newFilters = arrayOfNulls<InputFilter>(1)
    newFilters[0] = filter
    filters = newFilters

    if (update) {
        text = text
    }
}

/**
 * 光标定位在文本的最后面
 */
fun EditText.setSelectionLast() {
    setSelection(if (TextUtils.isEmpty(text)) 0 else text.length)
}

fun EditText.resetSelectionText(text: CharSequence?, startOffset: Int = 0) {
    val start: Int = selectionStart
    setText(text)
    setSelection(min(start + startOffset, text?.length ?: 0))
}