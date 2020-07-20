package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.StateSet
import android.view.*
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.angcyo.library.L
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
class REditDelegate(editText: EditText) : FocusEditDelegate(editText) {
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

    /** 隐藏显示密码, 在touch down一段时候后. 如果是密码类型, 默认开启 */
    var showPasswordOnTouch = false

    /**禁止弹出粘贴系统弹窗.默认等于[showPasswordOnTouch]*/
    var disableEditPaste = showPasswordOnTouch

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

    val _showPasswordRunnable = Runnable {
        editText.showPassword()
    }

    //</editor-fold desc="DslEditText">

    override fun initAttribute(context: Context, attrs: AttributeSet?) {
        super.initAttribute(context, attrs)

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

        showPasswordOnTouch = editText.isPasswordType()

        showPasswordOnTouch =
            typedArray.getBoolean(
                R.styleable.REditDelegate_r_show_password_on_touch,
                showPasswordOnTouch
            )

        disableEditPaste =
            typedArray.getBoolean(
                R.styleable.REditDelegate_r_disable_edit_paste,
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

        if (disableEditPaste) {
            //(无效) 能阻止长按弹出粘贴, 但是阻止不了点击手柄弹出的粘贴
            editText.isLongClickable = false
            editText.setTextIsSelectable(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TextViewCompat.setCustomSelectionActionModeCallback(
                    editText,
                    object : ActionMode.Callback2() {

                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            L.i("$mode : $menu")
                            return false
                        }

                        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            L.i("$mode : $menu")
                            return false
                        }

                        override fun onActionItemClicked(
                            mode: ActionMode?,
                            item: MenuItem?
                        ): Boolean {
                            L.i("$mode : $item")
                            return false
                        }

                        override fun onDestroyActionMode(mode: ActionMode?) {
                            L.i("$mode ")
                        }
                    })
            }
        }
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

        if (showPasswordOnTouch && editText.isPasswordType()) {
            if (action == MotionEvent.ACTION_DOWN) {
                editText.postDelayed(_showPasswordRunnable, 100)
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (isDownInClear) {
                    editText.removeCallbacks(_showPasswordRunnable)
                    editText.hidePassword()
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                editText.removeCallbacks(_showPasswordRunnable)
                editText.hidePassword()
            }
        }

        if (disableEditPaste) {
            //检查是否需要取消此次事件
            cancelEvent(event)
        }

        return true
    }

    //</editor-fold desc="代理View的方法">

    //<editor-fold desc="计算方法">

    fun addClearDrawable() {
        /*是否要显示删除按钮*/
        val clearDrawable = clearDrawable

        val compoundDrawables = TextViewCompat.getCompoundDrawablesRelative(editText)
        if (compoundDrawables[2] != clearDrawable) {
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                editText,
                compoundDrawables[0],
                compoundDrawables[1],
                clearDrawable,
                compoundDrawables[3]
            )
        }
    }

    fun removeClearDrawable() {
        /*移除显示的删除按钮*/
        val compoundDrawables = TextViewCompat.getCompoundDrawablesRelative(editText)
        editText.error = null
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            editText,
            compoundDrawables[0],
            compoundDrawables[1],
            null,
            compoundDrawables[3]
        )

        if (isPasswordDrawable) {
            updateState(false, true)
        }
    }

    override fun onFocusChanged(focused: Boolean) {
        super.onFocusChanged(focused)
        checkEdit(focused)
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

    /**是否需要取消事件. 可以用于禁止密码输入框的粘贴功能
     * 取消事件分发, 禁止弹出系统的 复制, 粘贴功能*/
    fun cancelEvent(event: MotionEvent): Boolean {
        val nowTime = System.currentTimeMillis()
        return if (event.actionMasked == MotionEvent.ACTION_UP && (nowTime - _downTime) > 160) {
            //取消事件分发, 禁止弹出系统的 复制, 粘贴功能.
            event.action = MotionEvent.ACTION_CANCEL
            editText.requestFocus()
            true
        } else {
            //在获取到焦点的情况, 快速点击输入框.还是会弹出粘贴菜单.
            //如果完全禁止, 输入法就会无法弹出...两难
            false
        }
    }

    //</editor-fold desc="计算方法">
}