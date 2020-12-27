package com.angcyo.widget.base

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.ex.isPhone
import com.angcyo.library.utils.PATTERN_MOBILE_SIMPLE
import com.angcyo.widget.edit.CharLengthFilter
import com.angcyo.widget.edit.SingleTextWatcher
import kotlin.math.max
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
    manager.showSoftInput(this, InputMethodManager.SHOW_FORCED)
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

/** 判断string是否是手机号码 */
fun EditText.isPhone(regex: String = PATTERN_MOBILE_SIMPLE) = string().isPhone(regex)

/** 返回结果表示是否为空, true:空 */
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

//<editor-fold desc="类型判断">

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

//</editor-fold desc="类型判断">

//<editor-fold desc="事件监听">

/**焦点变化改变监听*/
fun EditText.onFocusChange(notifyFirst: Boolean = true, listener: (Boolean) -> Unit) {
    onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus -> listener.invoke(hasFocus) }
    if (notifyFirst) {
        listener.invoke(this.isFocused)
    }
}

/**只要文本改变就通知*/
fun EditText.onTextChange(
    defaultText: CharSequence? = string(),
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

/**自动初始化输入框文本和保存*/
fun EditText.hawkTextChange(
    key: String,
    def: String?,
    inRv: Boolean = true,
    listener: (CharSequence) -> Unit = {}
) {
    if (inRv) {
        clearListeners()
    }
    setInputText(key.hawkGet(def))
    onTextChange {
        key.hawkPut(it)
        listener(it)
    }
}

//</editor-fold desc="事件监听">

/**设置文本, 并且将光标至于文本最后面*/
fun EditText.setInputText(text: CharSequence? = null) {
    setText(text)
    setSelection(min(text?.length ?: 0, getText().length))
}

/**恢复选中范围*/
fun EditText.restoreSelection(start: Int, stop: Int) {
    val length = text.length
    val _start = if (start in 0..length) {
        start
    } else {
        -1
    }

    val _stop = if (stop in 0..length) {
        stop
    } else {
        -1
    }

    if (_stop >= 0) {
        val min = min(max(0, _start), _stop)
        val max = max(max(0, _start), _stop)
        setSelection(min, max)
    } else if (_start >= 0) {
        setSelection(_start)
    }
}

/**发送删除键*/
fun EditText.sendDelKey() {
    del()
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

fun TextView.hasCharLengthFilter(): Boolean {
    return filters.any { it is CharLengthFilter }
}

fun TextView.getCharLengthFilter(): CharLengthFilter? {
    return filters.find { it is CharLengthFilter } as? CharLengthFilter
}

/**
 * 一个汉字等于2个英文, 一个emoji表情等于2个汉字
 */
fun TextView.getCharLength(): Int {
    if (TextUtils.isEmpty(string(false))) {
        return 0
    }
    var count = 0
    for (element in text) {
        count = if (element <= CharLengthFilter.MAX_CHAR) {
            count + 1
        } else {
            count + 2
        }
    }
    return count
}

//<editor-fold desc="方法扩展">

/**密码可见性*/
fun EditText.showPassword() {
    val selection = selectionEnd
    transformationMethod = null
    setSelection(selection)
}

/**密码不可见*/
fun EditText.hidePassword() {
    val selection = selectionEnd
    transformationMethod = PasswordTransformationMethod.getInstance()
    setSelection(selection)
}

/**密码可见性切换*/
fun EditText.passwordVisibilityToggleRequested() {
    val selection = selectionEnd

    if (hasPasswordTransformation()) {
        transformationMethod = null
    } else {
        transformationMethod = PasswordTransformationMethod.getInstance()
    }

    // And restore the cursor position
    setSelection(selection)
}

fun EditText.hasPasswordTransformation(): Boolean {
    return this.transformationMethod is PasswordTransformationMethod
}

fun EditText.isEditSingleLine(): Boolean {
    return inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE != EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE &&
            maxLines == 1 &&
            minLines == 1
}

/**输入法[OnEditorActionListener]*/
fun TextView.onImeAction(
    option: Int = EditorInfo.IME_ACTION_GO,
    label: CharSequence? = "Go",
    action: () -> Unit
) {
    setImeActionLabel(label, option)
    imeOptions = option

    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == option) {
            action()
            true
        }
        false
    }
}

//</editor-fold desc="方法扩展">