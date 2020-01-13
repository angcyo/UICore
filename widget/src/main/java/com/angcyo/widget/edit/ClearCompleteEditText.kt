package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.ContextCompat
import com.angcyo.drawable.dpi
import com.angcyo.widget.R
import com.angcyo.widget.base.isPasswordType
import com.angcyo.widget.edit.ClearEditText.Companion.STATE_NONE
import com.angcyo.widget.edit.ClearEditText.Companion.STATE_PRESSED
import kotlin.math.min

/**
 * 带删除按钮的输入框
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/06
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class ClearCompleteEditText : AppCompatAutoCompleteTextView {

    /**
     * 删除按钮区域
     */
    var clearRect = Rect()

    /**
     * 是否在 一键清空 按钮区域按下
     */
    var isDownInClear = false

    /**
     * 是否显示删除按钮
     */
    var showClearDrawable = true

    /**
     * clear 按钮功能切换成, 显示/隐藏 密码.
     */
    var isPasswordDrawable = false

    /**
     * 隐藏显示密码, 在touch down一段时候后
     */
    var showPasswordOnTouch = false

    var clearDrawable: Drawable? = null

    /**
     * 按下的时间
     */
    var _downTime: Long = 0

    /**
     * 当前密码, 是否可见
     */
    val isPasswordShow: Boolean
        get() = transformationMethod !is PasswordTransformationMethod

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClearCompleteEditText)

        showClearDrawable =
            typedArray.getBoolean(R.styleable.ClearCompleteEditText_r_show_clear, showClearDrawable)

        isPasswordDrawable = typedArray.getBoolean(
            R.styleable.ClearCompleteEditText_r_is_password_drawable,
            isPasswordType()
        )

        showPasswordOnTouch =
            typedArray.getBoolean(
                R.styleable.ClearCompleteEditText_r_show_password_on_touch,
                showPasswordOnTouch
            )

        if (typedArray.hasValue(R.styleable.ClearCompleteEditText_r_clear_drawable)) {

            clearDrawable =
                typedArray.getDrawable(R.styleable.ClearCompleteEditText_r_clear_drawable)
        } else {

            if (isPasswordDrawable) {
                clearDrawable =
                    ContextCompat.getDrawable(context, R.drawable.lib_password_selector)
            } else if (showClearDrawable) {
                clearDrawable =
                    ContextCompat.getDrawable(context, R.drawable.lib_edit_delete_selector)
            }

            if (isPasswordDrawable || showClearDrawable) {
                if (compoundDrawablePadding == 0) {
                    compoundDrawablePadding = 4 * dpi
                }
            }

        }

        typedArray.recycle()
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        //checkEdit(isFocused());
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        checkEdit(isFocused)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        checkEdit(focused)
        if (!focused) {
            _lastKeyCode = -1
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        //L.i("this...$isDownInClear")
        updateState(false, isDownInClear)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (showClearDrawable) {
            val offset = compoundDrawablePadding
            clearRect.set(
                w - paddingRight - clearDrawable!!.intrinsicWidth - offset,
                paddingTop - offset,
                w - paddingRight + offset,
                min(w, h) - paddingBottom + offset
            )
        }
    }

    fun addClearDrawable() {
        /*是否要显示删除按钮*/
        val clearDrawable = clearDrawable

        val compoundDrawables = compoundDrawables
        if (compoundDrawables[2] === clearDrawable) {

        } else {
            setCompoundDrawablesWithIntrinsicBounds(
                compoundDrawables[0],
                compoundDrawables[1],
                clearDrawable,
                compoundDrawables[3]
            )
        }
    }

    fun removeClearDrawable() {
        /*移除显示的删除按钮*/
        val compoundDrawables = compoundDrawables
        error = null
        setCompoundDrawablesWithIntrinsicBounds(
            compoundDrawables[0],
            compoundDrawables[1],
            null,
            compoundDrawables[3]
        )

        if (isPasswordDrawable) {
            updateState(false, true)
        }
    }

    fun updateState(fromTouch: Boolean, isDownIn: Boolean) {
        val clearDrawable = compoundDrawables[2] ?: return

        if (isPasswordDrawable) {
            if (fromTouch) {
            } else {
                if (isPasswordShow) {
                    clearDrawable.state = STATE_PRESSED
                } else {
                    clearDrawable.state = STATE_NONE
                }
            }
        } else {
            if (isDownIn) {
                clearDrawable.state = STATE_PRESSED
            } else {
                clearDrawable.state = STATE_NONE
            }
        }
    }

    fun checkEdit(focused: Boolean) {
        /*是否要显示删除按钮*/
        if (showClearDrawable) {
            if (TextUtils.isEmpty(text) || !focused) {
                //文本为空, 或者 无焦点
                removeClearDrawable()
            } else {
                addClearDrawable()
                updateState(false, false)
            }
        }
    }

    fun isTouchInClear(x: Float, y: Float): Boolean {
        return clearRect.contains(x.toInt(), y.toInt())
    }

    fun onClickClearDrawable(): Boolean {
        if (isPasswordDrawable) {
            if (isPasswordShow) {
                hidePassword()
            } else {
                showPassword()
            }
            updateState(false, true)
            return true
        }

        if (!TextUtils.isEmpty(text)) {
            setText("")
            setSelection(0)
            return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val action = event.action

        if (action == MotionEvent.ACTION_DOWN) {
            _downTime = System.currentTimeMillis()
        }

        if (showClearDrawable && isFocused) {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    isDownInClear = isTouchInClear(event.x, event.y)
                    updateState(true, isDownInClear)
                }
                MotionEvent.ACTION_MOVE -> {
                    isDownInClear = false
                    updateState(true, false)
                }
                MotionEvent.ACTION_UP -> {
                    updateState(true, false)
                    if (isDownInClear && isTouchInClear(event.x, event.y)) {
                        isDownInClear = false

                        if (onClickClearDrawable()) {
                            return true
                        }
                    }
                    isDownInClear = false
                }
                MotionEvent.ACTION_CANCEL -> {
                    updateState(true, false)
                    isDownInClear = false
                }
            }
        }

        if (showPasswordOnTouch) {
            if (action == MotionEvent.ACTION_DOWN) {
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (System.currentTimeMillis() - _downTime > 100) {
                    if (isDownInClear) {
                        hidePassword()
                    } else {
                        showPassword()
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                hidePassword()
            }
        }
        super.onTouchEvent(event)
        return true
    }

    private fun hasPasswordTransformation(): Boolean {
        return this.transformationMethod is PasswordTransformationMethod
    }

    /**密码可见性*/
    fun showPassword() {
        val selection = selectionEnd
        transformationMethod = null
        setSelection(selection)
    }

    /**密码不可见*/
    fun hidePassword() {
        val selection = selectionEnd
        transformationMethod = PasswordTransformationMethod.getInstance()
        setSelection(selection)
    }

    /**密码可见性切换*/
    fun passwordVisibilityToggleRequested() {
        val selection = selectionEnd

        if (hasPasswordTransformation()) {
            transformationMethod = null
        } else {
            transformationMethod = PasswordTransformationMethod.getInstance()
        }

        // And restore the cursor position
        setSelection(selection)
    }

    var _lastKeyCode = -1

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        _lastKeyCode = keyCode

        //L.i("${isEditSingleLine()} $maxLines $minLines")

        return super.onKeyDown(keyCode, event)
    }

    override fun hasOnClickListeners(): Boolean {
        //使用AutoCompleteTextView时, 会被默认设置onClickListener
        //这个时候, 输入法中的"下一步"触发的onKeyUp事件, 就会根据这个方法的返回值,
        //将焦点切换到下一个`EditText`.
        if (isEditSingleLine() && _lastKeyCode == KeyEvent.KEYCODE_ENTER) {
            return false
        }
        return super.hasOnClickListeners()
    }

    fun isEditSingleLine(): Boolean {
        return inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE != EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE &&
                maxLines == 1 &&
                minLines == 1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //L.i("this...draw")
    }
}
