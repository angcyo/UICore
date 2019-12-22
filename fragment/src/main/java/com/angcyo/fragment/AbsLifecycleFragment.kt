package com.angcyo.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewpager.widget.ViewPager
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
     * Fragment 是否对用户可见, 不管是GONE, 还是被覆盖, 都是不可见
     */
    var isFragmentVisible = true

    /**
     * Fragment 是否在 ViewPager中
     *
     *
     * ViewPager中, 所有的onFragmentShow方法, 都将在 Adapter中回调
     */
    var isInViewPager = false

    /**
     * 是否显示过第一次
     */
    var firstShowEnd = false

    /**
     * 触发 [.onFragmentShow] 的次数
     */
    var fragmentShowCount = 0

    //<editor-fold desc="生命周期, 系统的方法">

    override fun onVisibleChanged(
        oldHidden: Boolean,
        oldUserVisibleHint: Boolean,
        visible: Boolean
    ) {
        super.onVisibleChanged(oldHidden, oldUserVisibleHint, visible)
        //switchVisible(oldHidden, oldUserVisibleHint, visible)
    }

    override fun onResume() {
        super.onResume()
//        if (!isFragmentHide) {
//            onFragmentShow(null)
//        }
    }

    override fun onPause() {
        super.onPause()
//        if (!isFragmentHide) {
//            onFragmentHide()
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (container is ViewPager) {
            isInViewPager = true
            //ViewPager中, 默认是隐藏状态
            if (savedInstanceState == null) {
                isFragmentVisible = false
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        firstShowEnd = false
        fragmentShowCount = 0
    }

    override fun onDetach() {
        super.onDetach()
    }

    //</editor-fold>
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        if (savedInstanceState != null) { //状态恢复
//            isFragmentVisible = savedInstanceState.getBoolean(
//                KEY_FRAGMENT_VISIBLE,
//                isFragmentVisible
//            )
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

//    fun onFragmentReShow() {
//        i(
//            this.javaClass.simpleName +
//                    " view:" + (if (view == null) "×" else "√") +
//                    " viewHolder:" + if (baseViewHolder == null) "×" else "√"
//        )
//        onFragmentShow(null)
//    }

    @CallSuper
    override fun onFragmentShow(bundle: Bundle?) {
        i(
            this.javaClass.simpleName +
                    " view:" + (if (view == null) "×" else "√") +
                    " viewHolder:" + (if (baseViewHolder == null) "×" else "√") +
                    " bundle:" + if (bundle == null) "×" else "√" +
                    " firstShowEnd:" + if (firstShowEnd) "√" else "×"
        )
        if (view != null) {
            fragmentShowCount++
            if (firstShowEnd) {
                onFragmentNotFirstShow(bundle)
            } else {
                firstShowEnd = true
                onFragmentFirstShow(bundle)
            }
        }
    }

    /**
     * 从 onFragmentShow 分出来的周期事件
     */
    open fun onFragmentFirstShow(bundle: Bundle?) {}

    open fun onFragmentNotFirstShow(bundle: Bundle?) {}

    override fun onFragmentHide() {
        i(
            this.javaClass.simpleName +
                    " view:" + (if (view == null) "×" else "√") +
                    " viewHolder:" + if (baseViewHolder == null) "×" else "√"
        )
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

    override fun canSwipeBack(): Boolean {
        return view != null
    }

    override fun hideSoftInputOnTouchDown(touchDownView: View?): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentShowCount = 0
    }

    //</editor-fold>

    override fun getFragmentTag(): String {
        return this.javaClass.simpleName
    }
}