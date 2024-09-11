package com.angcyo.widget

import android.annotation.SuppressLint
import android.os.Build
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.angcyo.library.L
import com.angcyo.library.ex.ClickAction
import com.angcyo.library.ex.ViewAction
import com.angcyo.library.ex.each
import com.angcyo.library.ex.eachIndex
import com.angcyo.library.ex.replace
import com.angcyo.library.utils.getMember
import com.angcyo.widget.base.LongTouchListener
import com.angcyo.widget.base.ThrottleClickListener
import com.angcyo.widget.base.ThrottleClickListener.Companion.DEFAULT_THROTTLE_INTERVAL
import com.angcyo.widget.base.setBoldText
import java.lang.ref.WeakReference

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

@MainThread
open class DslViewHolder(
    itemView: View, initialCapacity: Int = DEFAULT_INITIAL_CAPACITY
) : ViewHolder(itemView) {

    companion object {
        var DEFAULT_INITIAL_CAPACITY = 32
    }

    //<editor-fold desc="属性">

    val context get() = itemView.context

    /**
     * findViewById是循环枚举所有子View的, 多少也是消耗性能的, +一个缓存
     */
    val sparseArray: SparseArray<WeakReference<View?>> = SparseArray(initialCapacity)

    /**是否绑定过界面, 用来标识是否是首次创建布局*/
    var isBindView: Boolean = false

    /**自定义的一些Flag*/
    var flag: Int = 0

    //</editor-fold desc="属性">

    //<editor-fold desc="基础">

    /**
     * 清理缓存
     */
    fun clear() {
        sparseArray.clear()
    }

    //</editor-fold desc="基础">

    //<editor-fold desc="事件处理">

    /**
     * 单击某个View, 有音效
     */
    fun clickView(view: View?) {
        view?.performClick()
    }

    fun clickView(@IdRes id: Int) {
        view(id)?.performClick()
    }

    /**
     * 单击某个View, 无音效
     */
    fun clickCallView(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            view?.callOnClick()
        }
    }

    fun clickCallView(@IdRes id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            view(id)?.callOnClick()
        }
    }

    fun click(@IdRes id: Int, listener: View.OnClickListener?) {
        val view = v<View>(id)
        view?.setOnClickListener(listener)
    }

    fun click(@IdRes id: Int, listener: (View) -> Unit) {
        click(id, View.OnClickListener { listener.invoke(it) })
    }

    fun click(@IdRes vararg ids: Int, listener: (View) -> Unit) {
        val clickListener = View.OnClickListener { listener.invoke(it) }
        ids.forEach {
            click(it, clickListener)
        }
    }

    /**枚举子元素
     * [recursively] 是否递归*/
    open fun eachChild(
        recursively: Boolean = false, action: (index: Int, childView: View) -> Unit
    ) {
        eachChild(itemView, recursively, action)
    }

    /**枚举子元素
     * [recursively] 是否递归*/
    open fun eachChild(
        view: View?, recursively: Boolean = false, action: (index: Int, childView: View) -> Unit
    ) {
        if (view is ViewGroup) {
            view.eachIndex(recursively, action)
        } else if (view != null) {
            action(0, view)
        }
    }

    /**依次点击child view
     * [recursively] 是否递归所有 ViewGroup */
    fun clickChild(
        @IdRes groupId: Int, recursively: Boolean = false, action: (childView: View) -> Unit
    ) {
        val view = view(groupId)
        if (view is ViewGroup) {
            view.each(recursively) { childView ->
                click(childView, action)
            }
        } else {
            click(view, action)
        }
    }

    /**是否将所有child的状态设置为选中?*/
    fun selectorChild(
        @IdRes groupViewId: Int,
        selected: Boolean = true,
        recursively: Boolean = false,
        ignore: (view: View?) -> Boolean = { false /*是否忽略处理指定的view*/ }
    ) {
        selectorChild(view(groupViewId), selected, recursively, ignore)
    }

    fun selectorChild(
        view: View?,
        selected: Boolean = true,
        recursively: Boolean = false,
        ignore: (view: View?) -> Boolean = { false /*是否忽略处理指定的view*/ }
    ) {
        if (view is ViewGroup) {
            view.each(recursively) { childView ->
                if (ignore(childView)) {
                    //no op
                } else {
                    childView.isSelected = selected
                }
            }
        } else {
            if (ignore(view)) {
                //no op
            } else {
                view?.isSelected = selected
            }
        }
    }

    /**点击元素, 顺便设置选中状态
     * [def] 默认状态
     * [mutex] 是否互斥, 如果为false, 那么选中之后, 就无法触发取消选中
     * [cancelOtherView] 是否要取消视图的选中状态
     * [selectedAction] 选中后, 需要执行的操作
     * */
    fun selectorClick(
        @IdRes id: Int,
        def: Boolean? = null,
        mutex: Boolean = true,
        cancelOtherView: Boolean = false,
        listener: (selected: Boolean) -> Boolean = { false /*不拦截默认处理*/ },
        selectedAction: ViewAction? = null,
    ) {
        if (def != null) {
            view(id)?.isSelected = def
        }
        click(id) { child ->
            val old = child.isSelected
            if (old && !mutex) {
                // no op
            } else {
                val new = !old

                if (new) {
                    //需要选中view
                    if (cancelOtherView) {
                        if (child.parent is ViewGroup) {
                            selectorChild(child.parent as ViewGroup, false) {
                                it == child
                            }
                        }
                    }
                }

                if (listener(new)) {
                    //no op
                } else {
                    child.isSelected = new
                    if (new) {
                        selectedAction?.invoke(child)
                    }
                }
            }
        }
    }

    /**节流点击事件*/
    fun throttleClick(
        @IdRes id: Int, throttleInterval: Long = DEFAULT_THROTTLE_INTERVAL, action: (View) -> Unit
    ) {
        click(id, ThrottleClickListener(throttleInterval = throttleInterval, action = action))
    }

    /**节流点击一组事件*/
    fun throttleClick(
        @IdRes vararg ids: Int,
        throttleInterval: Long = DEFAULT_THROTTLE_INTERVAL,
        action: (View) -> Unit
    ) {
        val listener = ThrottleClickListener(throttleInterval = throttleInterval, action = action)
        ids.forEach {
            click(it, listener)
        }
    }

    fun clickItem(listener: View.OnClickListener? = null) {
        click(itemView, listener)
    }

    fun clickItem(listener: (View) -> Unit) {
        click(itemView, View.OnClickListener { listener.invoke(it) })
    }

    fun throttleClickItem(action: (View) -> Unit) {
        click(itemView, ThrottleClickListener(action = action))
    }

    fun throttleClickItem(
        throttleInterval: Long = DEFAULT_THROTTLE_INTERVAL, action: (View) -> Unit
    ) {
        click(itemView, ThrottleClickListener(throttleInterval, action = action))
    }

    fun throttleClickItem(vararg ids: Int, action: (View) -> Unit) {
        val listener = ThrottleClickListener(action = action)
        click(itemView, listener)
        ids.forEach {
            click(it, listener)
        }
    }

    fun click(view: View?, listener: View.OnClickListener?) {
        view?.setOnClickListener(listener)
        if (listener == null) {
            view?.isClickable = false
        }
    }

    fun click(view: View?, listener: ClickAction? = null) {
        view?.apply {
            if (listener == null) {
                setOnClickListener(null)
            } else {
                setOnClickListener { v ->
                    listener(v)
                }
            }
        }
    }

    fun longClickItem(listener: ClickAction? = null) {
        itemView.apply {
            if (listener == null) {
                setOnLongClickListener(null)
            } else {
                setOnLongClickListener { v ->
                    listener(v)
                    true
                }
            }
        }
    }

    fun longClick(@IdRes id: Int, listener: ClickAction? = null) {
        view(id)?.apply {
            if (listener == null) {
                setOnLongClickListener(null)
            } else {
                setOnLongClickListener { v ->
                    listener(v)
                    true
                }
            }
        }
    }

    fun longClick(@IdRes id: Int, listener: View.OnLongClickListener?) {
        view(id)?.setOnLongClickListener(listener)
    }

    fun longClick(view: View?, listener: View.OnClickListener?) {
        view?.setOnLongClickListener { v ->
            listener?.onClick(v)
            true
        }
    }

    fun longClick(view: View?, listener: View.OnLongClickListener?) {
        view?.setOnLongClickListener(listener)
    }

    fun check(
        @IdRes resId: Int,
        checked: Boolean,
        listener: (buttonView: CompoundButton, isChecked: Boolean) -> Unit
    ): CompoundButton? {
        val compoundButton: CompoundButton? = v(resId)
        if (compoundButton != null) {
            compoundButton.setOnCheckedChangeListener(listener)
            compoundButton.isChecked = checked
        }
        return compoundButton
    }

    fun touch(@IdRes id: Int, block: (view: View, event: MotionEvent) -> Boolean) {
        touch(v<View>(id), block)
    }

    fun touch(view: View?, block: (view: View, event: MotionEvent) -> Boolean) {
        view?.setOnTouchListener(block)
    }

    /**长按事件识别
     * [loopLongPress] 是否需要循环发送长按事件, 否则只发送一次
     * [EVENT_TYPE_CLICK]
     * [EVENT_TYPE_LONG_PRESS]
     * */
    fun longTouch(
        @IdRes id: Int,
        loopLongPress: Boolean = false,
        block: (view: View, event: MotionEvent?, eventType: Int?, longPressHappened: Boolean) -> Boolean
    ) {
        longTouch(v<View>(id), loopLongPress, block)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun longTouch(
        view: View?,
        loopLongPress: Boolean = false,
        block: (view: View, event: MotionEvent?, eventType: Int?, longPressHappened: Boolean) -> Boolean
    ) {

        view?.let {
            //touch
            view.setOnTouchListener(LongTouchListener(block).apply {
                this.loopLongPress = loopLongPress
            })
        }
    }

    /**初始化, 点击之后再次初始化*/
    fun clickAndInit(@IdRes id: Int, init: (View) -> Unit, click: (View) -> Unit) {
        val view = v<View>(id)
        view?.apply {
            init(this)
            setOnClickListener {
                click(it)
                init(it)
            }
        }
    }

    //</editor-fold desc="事件处理">

    //<editor-fold desc="post回调">

    fun post(runnable: Runnable) {
        itemView.post(runnable)
    }

    fun post(runnable: () -> Unit) {
        postDelay(0, runnable)
    }

    fun postDelay(runnable: Runnable, delayMillis: Long) {
        itemView.postDelayed(runnable, delayMillis)
    }

    fun postDelay(delayMillis: Long, runnable: Runnable) {
        postDelay(runnable, delayMillis)
    }

    fun postOnAnimation(runnable: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            itemView.postOnAnimation(object : Runnable {
                override fun run() {
                    runnable.invoke()
                    removeCallbacks(this)
                }
            })
        } else {
            itemView.post(object : Runnable {
                override fun run() {
                    runnable.invoke()
                    removeCallbacks(this)
                }
            })
        }
    }

    fun postDelay(delayMillis: Long = 0, runnable: () -> Unit) {
        postDelay(object : Runnable {
            override fun run() {
                runnable.invoke()
                removeCallbacks(this)
            }

        }, delayMillis)
    }

    var _onceRunnbale: Runnable? = null

    fun postOnce(delayMillis: Long = 0, runnable: () -> Unit) {
        removeCallbacks(_onceRunnbale)
        _onceRunnbale = Runnable {
            runnable.invoke()
            removeCallbacks(_onceRunnbale)
        }
        postDelay(_onceRunnbale!!, delayMillis)
    }

    fun removeCallbacks(runnable: Runnable?) {
        itemView.removeCallbacks(runnable)
    }

    //</editor-fold desc="post回调">

    //<editor-fold desc="可见性控制">

    /**获取焦点*/
    fun focusView(@IdRes resId: Int) {
        focus<View>(resId)
    }

    /**获取焦点*/
    fun focused(@IdRes resId: Int) {
        focusView(resId)
    }

    /**获取焦点*/
    fun focused(view: View? = itemView) {
        view?.isFocusable = true
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
    }

    /**获取焦点*/
    fun <T : View?> focus(@IdRes resId: Int): T? {
        val v = v<View>(resId)
        if (v != null) {
            focused(v)
            return v as? T
        }
        return null
    }

    fun enable(
        @IdRes resId: Int, enable: Boolean? = true, recursive: Boolean = true
    ): DslViewHolder {
        val view = v<View>(resId)
        enable(view, enable, recursive)
        return this
    }

    /**[enable] 为null时, 表示恢复之前的状态*/
    fun enable(view: View?, enable: Boolean?, recursive: Boolean = true) {
        if (view == null) {
            return
        }
        val _enable = if (enable == null) {
            //恢复状态
            val oldEnable = view.getTag(R.id.lib_tag_enable)
            if (oldEnable is Boolean) {
                oldEnable
            } else {
                return
            }
        } else {
            enable
        }
        if (view is ViewGroup && recursive) {
            for (i in 0 until view.childCount) {
                enable(view.getChildAt(i), _enable, true)
            }
        }
        if (view.isEnabled != _enable) {
            view.setTag(R.id.lib_tag_enable, view.isEnabled)//保存状态
            view.isEnabled = _enable
        }
        if (view is EditText) {
            if (view.isEnabled) {
                view.clearFocus()
                //view.requestFocus() //need?
            } else {
                view.clearFocus()
            }
        }
    }

    fun selected(
        @IdRes resId: Int,
        selected: Boolean = true,
        recursion: Boolean = true,
    ): DslViewHolder {
        val view = v<View>(resId)
        selected(view, selected, recursion)
        return this
    }

    fun selected(selected: Boolean = true, recursion: Boolean = true) {
        selected(itemView, selected, recursion)
    }

    /**选中当前的view, 以及其所有的子view*/
    fun selected(view: View?, selected: Boolean = true, recursion: Boolean = true) {
        if (view == null) {
            return
        }
        if (view.isSelected != selected) {
            view.isSelected = selected
        }
        if (recursion && view is ViewGroup) {
            for (i in 0 until view.childCount) {
                selected(view.getChildAt(i), selected, true)
            }
        }
    }

    /**是否将所有TextView child的文本设置为粗体*/
    fun boldChild(
        @IdRes groupViewId: Int,
        bold: Boolean = true,
        recursively: Boolean = false,
        ignore: (view: View?) -> Boolean = { false /*是否忽略处理指定的view*/ }
    ) {
        boldChild(view(groupViewId), bold, recursively, ignore)
    }

    fun boldChild(
        view: View?,
        bold: Boolean = true,
        recursively: Boolean = false,
        ignore: (view: View?) -> Boolean = { false /*是否忽略处理指定的view*/ }
    ) {
        if (view is ViewGroup) {
            view.each(recursively) { childView ->
                if (ignore(childView)) {
                    //no op
                } else if (childView is TextView) {
                    childView.setBoldText(bold)
                }
            }
        } else {
            if (ignore(view)) {
                //no op
            } else if (view is TextView) {
                view.setBoldText(bold)
            }
        }
    }

    fun isVisible(@IdRes resId: Int): Boolean {
        return v<View>(resId)?.visibility == View.VISIBLE
    }

    fun visible(@IdRes resId: Int): View? {
        return visible(v<View>(resId))
    }

    fun visible(@IdRes resId: Int, visible: Boolean): DslViewHolder {
        val view = v<View>(resId) ?: return this
        if (visible) {
            visible(view)
        } else {
            gone(view)
        }
        return this
    }

    fun invisible(@IdRes resId: Int, invisible: Boolean): DslViewHolder {
        val view = v<View>(resId) ?: return this
        if (invisible) {
            invisible(view)
        } else {
            visible(view)
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

    fun gone(@IdRes resId: Int): DslViewHolder {
        return gone(v<View>(resId))
    }

    fun goneIndex(index: Int, gone: Boolean = true): DslViewHolder {
        val view = itemView
        if (view is ViewGroup) {
            val child = view.getChildAt(index)
            if (child != null) {
                gone(child, gone)
            }
        }
        return this
    }

    fun gone(view: View, gone: Boolean): DslViewHolder {
        if (gone) {
            gone(view)
        } else {
            visible(view)
        }
        return this
    }

    fun gone(@IdRes resId: Int, gone: Boolean): DslViewHolder {
        if (gone) {
            gone(v<View>(resId))
        } else {
            visible(resId)
        }
        return this
    }

    fun gone(@IdRes vararg resId: Int) {
        for (id in resId) {
            gone(id)
        }
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

    fun check(@IdRes resId: Int, check: Boolean = true, notify: Boolean = true): CompoundButton? {
        return v<CompoundButton>(resId)?.apply {
            if (notify) {
                isChecked = check
            } else {
                try {
                    val mOnCheckedChangeListener =
                        getMember(CompoundButton::class.java, "mOnCheckedChangeListener")
                    setOnCheckedChangeListener(null)
                    isChecked = check
                    setOnCheckedChangeListener(mOnCheckedChangeListener as CompoundButton.OnCheckedChangeListener?)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun check(
        @IdRes resId: Int,
        check: Boolean = true,
        notify: Boolean = true,
        onCheckedChanged: (checkView: CompoundButton, isChecked: Boolean) -> Unit
    ): CompoundButton? {
        return v<CompoundButton>(resId)?.apply {
            setOnCheckedChangeListener(onCheckedChanged)
            if (notify) {
                isChecked = check
            } else {
                try {
                    val mOnCheckedChangeListener =
                        getMember(CompoundButton::class.java, "mOnCheckedChangeListener")
                    setOnCheckedChangeListener(null)
                    isChecked = check
                    setOnCheckedChangeListener(mOnCheckedChangeListener as CompoundButton.OnCheckedChangeListener?)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //</editor-fold desc="可见性控制">

    //<editor-fold desc="findViewById">

    fun <T : View?> v(@IdRes resId: Int): T? {
        val viewWeakReference = sparseArray[resId]
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
        return try {
            view as? T?
        } catch (e: Exception) {
            L.w(e)
            null
        }
    }

    fun tv(@IdRes resId: Int): TextView? {
        return v(resId)
    }

    fun et(@IdRes resId: Int): EditText? {
        return v(resId)
    }

    fun ev(@IdRes resId: Int): EditText? {
        return v(resId)
    }

    fun img(@IdRes resId: Int): ImageView? {
        return v(resId)
    }

    fun rv(@IdRes resId: Int): RecyclerView? {
        return v(resId)
    }

    fun group(@IdRes resId: Int): ViewGroup? {
        return v(resId)
    }

    fun group(view: View?): ViewGroup? {
        return view as? ViewGroup
    }

    fun view(@IdRes resId: Int): View? {
        return v<View>(resId)
    }

    fun cb(@IdRes resId: Int): CompoundButton? {
        return v(resId)
    }

    //</editor-fold desc="findViewById">

    //<editor-fold desc="属性控制">

    fun tag(@IdRes resId: Int, key: Int, value: Any?): Any? {
        val view = view(resId)
        val old = view?.getTag(key)
        view?.setTag(key, value)
        return old
    }

    fun isChecked(@IdRes resId: Int): Boolean {
        return cb(resId)?.isChecked == true
    }

    fun isSelected(@IdRes resId: Int): Boolean {
        return view(resId)?.isSelected == true
    }

    fun isEnabled(@IdRes resId: Int): Boolean {
        return view(resId)?.isEnabled == true
    }

    /**是否是在[RecyclerView]中*/
    fun isInRecyclerView(): Boolean {
        return itemView.parent is RecyclerView
    }

    //</editor-fold desc="属性控制">

    //<editor-fold desc="ViewGroup">

    /**
     * 获取指定索引的child
     * DslViewHolder[0]
     * */
    operator fun get(index: Int): View? {
        val view = itemView
        if (view is ViewGroup) {
            return view.getChildAt(index)
        }
        return null
    }

    /**将[itemView]的所有内容替换成新的布局[layoutId]*/
    fun replace(@LayoutRes layoutId: Int, attachToRoot: Boolean = true) {
        replace(itemView, layoutId, attachToRoot)
    }

    /**将[itemView]的所有内容替换成新的布局[layoutId]
     * [groupViewId] group的布局id*/
    fun replace(
        @IdRes groupViewId: Int, @LayoutRes layoutId: Int, attachToRoot: Boolean = true
    ) {
        replace(view(groupViewId), layoutId, attachToRoot)
    }

    /**[replace]*/
    fun replace(rootView: View?, @LayoutRes layoutId: Int, attachToRoot: Boolean = true) {
        if (rootView is ViewGroup) {
            clear()
            rootView.replace(layoutId, attachToRoot)
        }
    }

    //</editor-fold desc="ViewGroup">

    //<editor-fold desc="View">

    /**更新文本控件的文本内容, 并控制可见性*/
    fun updateText(@IdRes resId: Int, text: CharSequence?): TextView? {
        return tv(resId)?.apply {
            visible(resId, text != null)
            this.text = text
        }
    }

    //</editor-fold desc="View">


}