package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.StateSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.angcyo.library.ex.dpi
import com.angcyo.widget.R
import com.angcyo.widget.base.hidePassword
import com.angcyo.widget.base.isPasswordType
import com.angcyo.widget.base.showPassword
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/05
 */
class REditDelegate(val editText: EditText) {
    companion object {
        val STATE_NONE = StateSet.WILD_CARD
        val STATE_PRESSED = intArrayOf(android.R.attr.state_pressed)
    }

    //<editor-fold desc="ClearEditText">

    /** 删除按钮区域 */
    var _clearRect = Rect()

    /** 是否在 一键清空 按钮区域按下 */
    var isDownInClear = false

    /** 是否显示删除按钮 */
    var showClearDrawable = true

    /** clear 按钮功能切换成, 显示/隐藏 密码. */
    var isPasswordDrawable = false

    /** 隐藏显示密码, 在touch down一段时候后 */
    var showPasswordOnTouch = false

    /**删除ico*/
    var clearDrawable: Drawable? = null

    /** 按下的时间 */
    var _downTime: Long = 0
    var _downX = 0f
    var _downY = 0f
    var _touchSlop = 0

    /**
     * 当前密码, 是否可见
     */
    val isPasswordShow: Boolean
        get() = editText.transformationMethod !is PasswordTransformationMethod

    //</editor-fold desc="ClearEditText">

    //<editor-fold desc="DslEditText">

    /** 是否是不可编辑模式 */
    var isNoEditMode = false

    /** 是否只有在 touch 事件的时候, 才可以请求焦点. 防止在列表中,自动获取焦点的情况 */
    var requestFocusOnTouch = false

    /** 当失去焦点时, 是否隐藏键盘 */
    var hideSoftInputOnLostFocus = false

    /** 当onDetachedFromWindow时, 是否隐藏键盘 */
    var hideSoftInputOnDetached = false

    /** 当视图不可见时, 是否隐藏键盘 */
    var hideSoftInputOnInvisible = false

    //</editor-fold desc="DslEditText">

    fun initAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.REditDelegate)

        showClearDrawable =
            typedArray.getBoolean(R.styleable.REditDelegate_r_show_clear, showClearDrawable)

        isNoEditMode =
            typedArray.getBoolean(R.styleable.REditDelegate_r_is_no_edit_mode, isNoEditMode)
        requestFocusOnTouch = typedArray.getBoolean(
            R.styleable.REditDelegate_r_request_focus_on_touch,
            requestFocusOnTouch
        )
        hideSoftInputOnLostFocus = typedArray.getBoolean(
            R.styleable.REditDelegate_r_hide_soft_input_on_lost_focus,
            hideSoftInputOnLostFocus
        )
        hideSoftInputOnDetached = typedArray.getBoolean(
            R.styleable.REditDelegate_r_hide_soft_input_on_detached,
            hideSoftInputOnDetached
        )
        hideSoftInputOnInvisible = typedArray.getBoolean(
            R.styleable.REditDelegate_r_hide_soft_input_on_invisible,
            hideSoftInputOnInvisible
        )

        isPasswordDrawable = typedArray.getBoolean(
            R.styleable.REditDelegate_r_is_password_drawable,
            editText.isPasswordType()
        )

        showPasswordOnTouch =
            typedArray.getBoolean(
                R.styleable.REditDelegate_r_show_password_on_touch,
                showPasswordOnTouch
            )

        if (typedArray.hasValue(R.styleable.REditDelegate_r_clear_drawable)) {
            clearDrawable =
                typedArray.getDrawable(R.styleable.REditDelegate_r_clear_drawable)
        } else {
            if (isPasswordDrawable) {
                clearDrawable =
                    ContextCompat.getDrawable(context, R.drawable.lib_password_selector)
            } else if (showClearDrawable) {
                clearDrawable =
                    ContextCompat.getDrawable(context, R.drawable.lib_edit_delete_selector)
            }
            if (isPasswordDrawable || showClearDrawable) {
                if (editText.compoundDrawablePadding == 0) {
                    editText.compoundDrawablePadding = 4 * dpi
                }
            }

        }

        typedArray.recycle()

        _touchSlop = ViewConfiguration.get(editText.context).scaledTouchSlop
    }

    //<editor-fold desc="代理View的方法">

    //代理
    fun drawableStateChanged() {
        //L.i("this...$isDownInClear")
        updateState(false, isDownInClear)
    }

    //代理
    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (showClearDrawable) {
            val offset = editText.compoundDrawablePadding
            _clearRect.set(
                w - editText.paddingRight - clearDrawable!!.intrinsicWidth - offset,
                editText.paddingTop - offset,
                w - editText.paddingRight + offset,
                min(w, h) - editText.paddingBottom + offset
            )
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_DOWN) {
            _downTime = System.currentTimeMillis()
        }

        if (showClearDrawable && editText.isFocused) {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    _downX = event.x
                    _downY = event.y
                    isDownInClear = isTouchInClear(event.x, event.y)
                    updateState(true, isDownInClear)
                }
                MotionEvent.ACTION_MOVE -> {
                    val x = event.x
                    val y = event.y
                    if ((x - _downX).absoluteValue >= _touchSlop ||
                        (y - _downY).absoluteValue >= _touchSlop
                    ) {
                        isDownInClear = false
                        updateState(true, false)
                    }
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
                        editText.hidePassword()
                    } else {
                        editText.showPassword()
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                editText.hidePassword()
            }
        }
        return true
    }

    //</editor-fold desc="代理View的方法">

    //<editor-fold desc="计算方法">

    fun addClearDrawable() {
        /*是否要显示删除按钮*/
        val clearDrawable = clearDrawable

        val compoundDrawables = editText.compoundDrawables
        if (compoundDrawables[2] === clearDrawable) {

        } else {
            editText.setCompoundDrawablesWithIntrinsicBounds(
                compoundDrawables[0],
                compoundDrawables[1],
                clearDrawable,
                compoundDrawables[3]
            )
        }
    }

    fun removeClearDrawable() {
        /*移除显示的删除按钮*/
        val compoundDrawables = editText.compoundDrawables
        editText.error = null
        editText.setCompoundDrawablesWithIntrinsicBounds(
            compoundDrawables[0],
            compoundDrawables[1],
            null,
            compoundDrawables[3]
        )

        if (isPasswordDrawable) {
            updateState(false, true)
        }
    }

    fun checkEdit(focused: Boolean) {
        /*是否要显示删除按钮*/
        if (showClearDrawable) {
            if (TextUtils.isEmpty(editText.text) || !focused) {
                //文本为空, 或者 无焦点
                removeClearDrawable()
            } else {
                addClearDrawable()
                updateState(false, false)
            }
        }
    }

    /**更新删除ico状态*/
    fun updateState(fromTouch: Boolean, isDownIn: Boolean) {
        val clearDrawable = editText.compoundDrawables[2] ?: return

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


    fun isTouchInClear(x: Float, y: Float): Boolean {
        return _clearRect.contains(x.toInt(), y.toInt())
    }


    fun onClickClearDrawable(): Boolean {
        if (isPasswordDrawable) {
            if (isPasswordShow) {
                editText.hidePassword()
            } else {
                editText.showPassword()
            }
            updateState(false, true)
            return true
        }

        if (!TextUtils.isEmpty(editText.text)) {
            editText.setText("")
            editText.setSelection(0)
            return true
        }
        return false
    }

    //</editor-fold desc="计算方法">
}