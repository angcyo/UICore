package com.angcyo.widget.edit

import android.app.Activity
import android.view.View
import com.angcyo.widget.base.hideSoftInput
import java.lang.ref.WeakReference

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/13
 */
class HideSoftInputRunnable(editText: View) : Runnable {

    companion object {
        var hideSoftInputRunnable: HideSoftInputRunnable? = null

        fun doIt(editText: View) {
            cancel()
            hideSoftInputRunnable = HideSoftInputRunnable(editText)
            editText.postDelayed(hideSoftInputRunnable, 60)
        }

        fun cancel() {
            hideSoftInputRunnable?.remove()
        }
    }

    var decorView: WeakReference<View?>? = null

    init {
        val activity = editText.context as? Activity
        activity?.window?.decorView?.run {
            decorView = WeakReference(this)
        }
    }

    fun remove() {
        decorView?.get()?.run {
            removeCallbacks(this@HideSoftInputRunnable)
            decorView?.clear()
            decorView = null
        }
        hideSoftInputRunnable = null
    }

    override fun run() {
        decorView?.get()?.run {
            hideSoftInput()
        }
        remove()
    }
}