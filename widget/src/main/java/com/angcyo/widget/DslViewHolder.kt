package com.angcyo.widget

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.lang.ref.WeakReference

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslViewHolder(itemView: View, initialCapacity: Int = 32) : ViewHolder(itemView) {

    /**
     * findViewById是循环枚举所有子View的, 多少也是消耗性能的, +一个缓存
     */
    val sparseArray: SparseArray<WeakReference<View?>> = SparseArray(initialCapacity)

    fun <T : View?> v(@IdRes resId: Int): T? {
        val viewWeakReference =
            sparseArray[resId]
        var view: View?
        if (viewWeakReference == null) {
            view = itemView.findViewById(resId)
            sparseArray.put(resId, WeakReference(view))
        } else {
            view = viewWeakReference.get()
            if (view == null) {
                view = itemView.findViewById(resId)
                sparseArray.put(resId, WeakReference(view))
            }
        }
        return view as? T
    }

    /**
     * 单击某个View
     */
    fun clickView(view: View?) {
        view?.performClick()
    }

    /**
     * 清理缓存
     */
    fun clear() {
        sparseArray.clear()
    }

    fun click(@IdRes id: Int, listener: View.OnClickListener?) {
        val view = v<View>(id)
        view?.setOnClickListener(listener)
    }

    fun clickItem(listener: View.OnClickListener?) {
        click(itemView, listener)
    }

    fun click(view: View?, listener: View.OnClickListener?) {
        view?.setOnClickListener(listener)
    }

    fun post(runnable: Runnable?) {
        itemView.post(runnable)
    }

    fun postDelay(runnable: Runnable?, delayMillis: Long) {
        itemView.postDelayed(runnable, delayMillis)
    }

    fun postDelay(delayMillis: Long, runnable: Runnable?) {
        postDelay(runnable, delayMillis)
    }

    fun removeCallbacks(runnable: Runnable?) {
        itemView.removeCallbacks(runnable)
    }

    fun tv(@IdRes resId: Int): TextView {
        return v<View>(resId) as TextView
    }

    fun img(@IdRes resId: Int): ImageView {
        return v<View>(resId) as ImageView
    }

    fun rv(@IdRes resId: Int): RecyclerView {
        return v<View>(resId) as RecyclerView
    }

    fun group(@IdRes resId: Int): ViewGroup {
        return group(v<View>(resId)!!)
    }

    fun group(view: View): ViewGroup {
        return view as ViewGroup
    }

    fun <T : View?> focus(@IdRes resId: Int): T? {
        val v = v<View>(resId)
        if (v != null) {
            v.isFocusable = true
            v.isFocusableInTouchMode = true
            v.requestFocus()
            return v as T
        }
        return null
    }

    fun view(@IdRes resId: Int): View {
        return v<View>(resId)!!
    }

    fun isVisible(@IdRes resId: Int): Boolean {
        return v<View>(resId)!!.visibility == View.VISIBLE
    }

    fun visible(@IdRes resId: Int): View? {
        return visible(v<View>(resId))
    }

    fun visible(@IdRes resId: Int, visible: Boolean): DslViewHolder {
        val view = v<View>(resId)!!
        if (visible) {
            visible(view)
        } else {
            gone(view)
        }
        return this
    }

    fun invisible(@IdRes resId: Int, visible: Boolean): DslViewHolder {
        val view = v<View>(resId)!!
        if (visible) {
            visible(view)
        } else {
            invisible(view)
        }
        return this
    }

    fun visible(view: View?): View? {
        if (view != null) {
            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
            }
        }
        return view
    }

    fun enable(@IdRes resId: Int, enable: Boolean): DslViewHolder {
        val view = v<View>(resId)!!
        enable(view, enable)
        return this
    }

    private fun enable(view: View?, enable: Boolean) {
        if (view == null) {
            return
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                enable(view.getChildAt(i), enable)
            }
        } else {
            if (view.isEnabled != enable) {
                view.isEnabled = enable
            }
            (view as? EditText)?.clearFocus()
        }
    }

    fun invisible(@IdRes resId: Int): View? {
        return invisible(v<View>(resId))
    }

    fun invisible(view: View?): View? {
        if (view != null) {
            if (view.visibility != View.INVISIBLE) {
                view.clearAnimation()
                view.visibility = View.INVISIBLE
            }
        }
        return view
    }

    fun gone(@IdRes resId: Int) {
        gone(v<View>(resId))
    }

    fun gone(view: View?): DslViewHolder {
        if (view != null) {
            if (view.visibility != View.GONE) {
                view.clearAnimation()
                view.visibility = View.GONE
            }
        }
        return this
    }
}