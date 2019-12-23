package com.angcyo.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import com.angcyo.base.toVisibilityString
import com.angcyo.library.L.i

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
//        if (lastFragment != null) {
//            if (lastFragment is Fragment) {
//                (lastFragment as Fragment).onActivityResult(
//                    requestCode,
//                    resultCode,
//                    data
//                )
//            }
//        }
    }

    //</editor-fold>

    //<editor-fold desc="扩展的方法">

    @CallSuper
    override fun onFragmentShow(bundle: Bundle?) {
        i(buildString {
            append(this@AbsLifecycleFragment.javaClass.simpleName)
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

    override fun onFragmentHide() {
        i(buildString {
            append(this@AbsLifecycleFragment.javaClass.simpleName)
            append(" view:").append((if (view == null) "×" else "√"))
            append(" visible:").append(view?.visibility?.toVisibilityString() ?: "×")
        })
    }

    /**
     * 可以关闭当前界面.
     */
    override fun onBackPressed(activity: Activity): Boolean {
//        if (lastFragment == null) {
//            return true
//        }
//        return if (lastFragment is AbsLifecycleFragment) {
//            lastFragment.onBackPressed(activity)
//        } else true
        return true
    }


    override fun canSwipeBack(): Boolean {
        return view != null
    }

    override fun hideSoftInputOnTouchDown(touchDownView: View?): Boolean {
        return false
    }

    override fun getFragmentTag(): String {
        return this.javaClass.name
    }

    //</editor-fold>
}