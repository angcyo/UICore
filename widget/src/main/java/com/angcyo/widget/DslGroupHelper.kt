package com.angcyo.widget

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import com.angcyo.library.ex.color
import com.angcyo.widget.base.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class DslGroupHelper(val parentView: View) : DslViewHolder(parentView) {
    var selectorView: View? = null

    init {
        selectorView = parentView
    }

    fun selector(id: Int) {
        selectorView = v(id)
    }

    fun selectorIndex(index: Int) {
        if (parentView is ViewGroup) {
            selectorView = parentView.getChildOrNull(index)
        }
    }

    fun setBackground(color: Int) {
        selectorView?.setBackgroundColor(color)
    }

    fun setBackground(drawable: Drawable?) {
        selectorView?.run { ViewCompat.setBackground(this, drawable) }
    }

    fun setText(text: CharSequence?) {
        selectorView?.let {
            when (it) {
                is TextView -> it.text = text
            }
        }
    }

    //---

    fun setTextSize(@Px textSize: Float) {
        selectorView?.let {
            setTextSize(it, textSize)
        }
    }

    fun setTextSize(view: View, @Px textSize: Float) {
        when (view) {
            is TextView -> view.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                textSize
            )
            is ViewGroup -> view.eachChild { _, child ->
                setTextSize(child, textSize)
            }
        }
    }

    /**[Typeface.NORMAL]
     * [Typeface.BOLD]
     * [Typeface.ITALIC]
     * [Typeface.BOLD_ITALIC]
     * */
    fun setTextStyle(type: Int) {
        selectorView?.let {
            setTextStyle(it, type)
        }
    }

    fun setTextStyle(view: View, type: Int) {
        when (view) {
            is TextView -> view.setTypeface(null, type)
            is ViewGroup -> view.eachChild { _, child ->
                setTextStyle(child, type)
            }
        }
    }

    //---

    fun setTextColor(color: Int) {
        selectorView?.let {
            setTextColor(it, color)
        }
    }

    fun setTextColor(view: View, color: Int) {
        when (view) {
            is TextView -> view.setTextColor(color)
            is ViewGroup -> view.eachChild { _, child ->
                setTextColor(child, color)
            }
        }
    }

    fun setTextColor(colors: ColorStateList?) {
        selectorView?.let {
            setTextColor(it, colors)
        }
    }

    fun setTextColor(view: View, colors: ColorStateList?) {
        when (view) {
            is TextView -> view.setTextColor(colors)
            is ViewGroup -> view.eachChild { _, child ->
                setTextColor(child, colors)
            }
        }
    }

    //---

    fun setDrawableColor(color: Int) {
        selectorView?.let {
            setDrawableColor(it, color)
        }
    }

    fun setDrawableColor(view: View, color: Int) {
        when (view) {
            is TextView -> view.run {
                val compoundDrawables: Array<Drawable?> = compoundDrawables
                setCompoundDrawablesWithIntrinsicBounds(
                    compoundDrawables[0]?.color(color),
                    compoundDrawables[1]?.color(color),
                    compoundDrawables[2]?.color(color),
                    compoundDrawables[3]?.color(color)
                )
            }
            is ImageView -> view.run {
                setImageDrawable(drawable?.color(color))
            }
            is ViewGroup -> view.eachChild { _, child ->
                setDrawableColor(child, color)
            }
        }
    }

    //---

    fun inflate(
        @LayoutRes layoutId: Int,
        attachToRoot: Boolean = true,
        action: View.() -> Unit = {}
    ): View? {
        if (selectorView is ViewGroup) {
            return (selectorView as ViewGroup).inflate(layoutId, attachToRoot).apply {
                action()
            }
        }
        return null
    }

    fun append(@LayoutRes layoutId: Int, action: View.() -> Unit = {}) {
        if (selectorView is ViewGroup) {
            val view = (selectorView as ViewGroup).inflate(layoutId, false)
            append(view, action)
        }
    }

    fun <T : View> append(view: T?, action: T.() -> Unit = {}) {
        if (view != null && selectorView is ViewGroup) {
            (selectorView as ViewGroup).addView(view)
            view.action()
        }
    }

    fun eachChild(recursively: Boolean = false, map: (index: Int, child: View) -> Unit) {
        if (selectorView is ViewGroup) {
            (selectorView as ViewGroup).eachChild(recursively, map)
        } else {
            selectorView?.apply { map(0, this) }
        }
    }

    fun removeAllChild() {
        if (selectorView is ViewGroup) {
            (selectorView as ViewGroup).removeAllViews()
        }
    }

    fun setVisible(value: Boolean = true) {
        selectorView?.visible(value)
    }

    fun setGone(value: Boolean = true) {
        selectorView?.gone(value)
    }
}

/**返回[DslGroupHelper]*/
fun View?.helper(): DslGroupHelper? = this?.run { DslGroupHelper(this) }