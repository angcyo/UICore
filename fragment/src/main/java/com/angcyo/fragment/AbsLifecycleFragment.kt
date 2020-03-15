package com.angcyo.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.angcyo.base.getAllResumedFragment
import com.angcyo.base.toVisibilityString
import com.angcyo.library.L.i
import com.angcyo.library.ex.hash
import com.angcyo.widget.base.hideSoftInput

/**
 * Created by angcyo on 2018/12/03 23:17
 *
 *
 * 生命周期的封装, 只需要关注 [onFragmentShow] 和 [onFragmentHide]
 *
 * @author angcyo
 */
abstract class AbsLifecycleFragment : AbsFragment(), IFragment {

    /**
     * 触发 [.onFragmentShow] 的次数
     */
    var fragmentShowCount = 0

    /**[childFragmentManager]中最后一个可见性的[Fragment]*/
    val lastFragment: Fragment? get() = childFragmentManager.getAllResumedFragment().lastOrNull()

    //<editor-fold desc="生命周期, 系统的方法">

    override fun onResume() {
        super.onResume()
        onFragmentShow(null)
    }

    override fun onPause() {
        super.onPause()
        onFragmentHide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentShowCount = 0
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        lastFragment?.onActivityResult(requestCode, resultCode, data)
    }

    //</editor-fold>

    //<editor-fold desc="扩展的方法">

    @CallSuper
    open fun onFragmentShow(bundle: Bundle?) {
        i(buildString {
            append(this@AbsLifecycleFragment.javaClass.simpleName)
            append("#").append(this@AbsLifecycleFragment.hash())
            append(" view:").append((if (view == null) "×" else "√"))
            append(" visible:").append(view?.visibility?.toVisibilityString() ?: "×")
            append(" bundle:").append((if (bundle == null) "×" else "√"))
            append(" showCount:").append(fragmentShowCount)
        })

        if (view != null) {
            if (fragmentShowCount++ == 0) {
                onFragmentFirstShow(bundle)
            } else {
                onFragmentNotFirstShow(bundle)
            }
        }
    }

    /**
     * 从 onFragmentShow 分出来的周期事件
     */
    open fun onFragmentFirstShow(bundle: Bundle?) {}

    open fun onFragmentNotFirstShow(bundle: Bundle?) {}

    open fun onFragmentHide() {
        i(buildString {
            append(this@AbsLifecycleFragment.javaClass.simpleName)
            append("#").append(this@AbsLifecycleFragment.hash())
            append(" view:").append((if (view == null) "×" else "√"))
            append(" visible:").append(view?.visibility?.toVisibilityString() ?: "×")
        })
    }

    /**
     * 是否可以关闭当前[Fragment]界面.
     * 返回true, 表示可以关闭
     */
    override fun onBackPressed(): Boolean {
        view?.hideSoftInput()
        return (lastFragment as? AbsLifecycleFragment)?.onBackPressed() ?: true
    }

    override fun canSwipeBack(): Boolean {
        return view != null
    }

    override fun canFlingBack(): Boolean {
        return canSwipeBack()
    }

    override fun hideSoftInputOnTouchDown(touchDownView: View?): Boolean {
        return false
    }

    override fun getFragmentTag(): String {
        return this.javaClass.name
    }

    //</editor-fold>

    //<editor-fold desc="高级扩展">

    /**快速观察[LiveData]*/
    fun <T> LiveData<T>.observe(action: (data: T?) -> Unit): Observer<T> {
        val result: Observer<T>
        observe(this@AbsLifecycleFragment, Observer<T> { action(it) }.apply {
            result = this
        })
        return result
    }

    //</editor-fold>
}