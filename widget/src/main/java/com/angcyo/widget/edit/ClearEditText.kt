package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.angcyo.widget.base.isEditSingleLine

/**
 * 带删除按钮的输入框
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/06
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class ClearEditText : AppCompatEditText {

    var rEditDelegate: REditDelegate? = null

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
        rEditDelegate = REditDelegate(this)
        rEditDelegate?.initAttribute(context, attrs)
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
        rEditDelegate?.checkEdit(isFocused)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        rEditDelegate?.checkEdit(focused)
        if (!focused) {
            _lastKeyCode = -1
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        rEditDelegate?.drawableStateChanged()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rEditDelegate?.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        rEditDelegate?.onTouchEvent(event)
        super.onTouchEvent(event)
        return true
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
