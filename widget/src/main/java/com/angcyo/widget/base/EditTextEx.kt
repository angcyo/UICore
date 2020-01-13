package com.angcyo.widget.base

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

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
