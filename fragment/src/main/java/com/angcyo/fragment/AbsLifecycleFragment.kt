package com.angcyo.fragment

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.angcyo.base.checkBackPressedDispatcher
import com.angcyo.base.getAllResumedFragment
import com.angcyo.base.setFragmentResult
import com.angcyo.base.toVisibilityString
import com.angcyo.library.L
import com.angcyo.library.L.i
import com.angcyo.library.ex.hash
import com.angcyo.library.ex.hideSoftInput
import com.angcyo.viewmodel.watch
import com.angcyo.viewmodel.watchForever
import com.angcyo.viewmodel.watchOnce

/**
 * Created by angcyo on 2018/12/03 23:17
 *
 *
 * 生命周期的封装, 只需要关注 [onFragmentShow] 和 [onFragmentHide]
 *
 * @author angcyo
 */
abstract class AbsLifecycleFragment : AbsFragment(), IFragment, OnBackPressedDispatcherOwner {

    /**
     * 触发 [.onFragmentShow] 的次数
     */
    var fragmentShowCount = 0

    /**界面是否处于显示状态*/
    var isFragmentShow: Boolean = false

    /**[childFragmentManager]中最后一个可见性的[Fragment]*/
    val lastFragment: Fragment? get() = childFragmentManager.getAllResumedFragment().lastOrNull()

    //<editor-fold desc="属性动画的支持">

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    /**Fragment切换的时候, 尽量使用属性动画. 这样动画会在RenderThread中执行, 流畅度显著提升
     * https://developer.android.google.cn/guide/topics/graphics/prop-animation
     * https://developer.android.com/guide/topics/resources/drawable-resource
     * https://developer.android.com/guide/topics/resources/animation-resource
     * */
    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        /*alpha
        scaleX/scaleY
        translateX/translateY
        rotation*/
        if (transit == 0) {
            return FragmentAnimator.loadAnimator(fContext(), nextAnim)
        }
        return super.onCreateAnimator(transit, enter, nextAnim)
    }

    //</editor-fold desc="属性动画的支持">

    //<editor-fold desc="生命周期, 系统的方法">

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        onFragmentShow(arguments)
    }

    override fun onPause() {
        super.onPause()
        onFragmentHide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentShowCount = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        foreverObserveMap.forEach {
            try {
                it.key.removeObserver(it.value as Observer<in Any>)
            } catch (e: Exception) {
            }
        }
        onFragmentSetResult()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        lastFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDetach() {
        super.onDetach()
    }

    /**重写此方法, 回调[Fragment]的返回值.
     * [com.angcyo.base.IFragmentResultKt.onFragmentResult]*/
    open fun onFragmentSetResult() {
        setFragmentResult(null)
    }

    //</editor-fold desc="生命周期, 系统的方法">

    //<editor-fold desc="扩展的方法">

    @CallSuper
    open fun onFragmentShow(bundle: Bundle?) {
        isFragmentShow = true
        L.d(buildString {
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
        isFragmentShow = false
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
        return if (checkBackPressedDispatcher()) {
            view?.hideSoftInput()
            (lastFragment as? AbsLifecycleFragment)?.onBackPressed() ?: true
        } else {
            false
        }
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
        return tag ?: this.javaClass.name
    }

    val backPressedDispatcher = OnBackPressedDispatcher {
        L.i("无返回Callback需要被执行")
    }

    override fun getOnBackPressedDispatcher(): OnBackPressedDispatcher = backPressedDispatcher

    //</editor-fold>

    //<editor-fold desc="LiveData扩展">

    /**快速观察[LiveData]
     * [autoClear] 收到有效数据后, 是否自动清除数据
     * [allowBackward] 是否允许数据倒灌, 接收到旧数据
     * */
    fun <T> LiveData<T>.observe(
        autoClear: Boolean = false,
        allowBackward: Boolean = true,
        action: (data: T?) -> Unit
    ): Observer<T?> = watch(this@AbsLifecycleFragment, autoClear, allowBackward, action)

    /**快速观察[LiveData]一次, 确保不收到null数据
     * [action] 返回值表示是否处理了数据, 如果没有处理, 则不会remove
     * [allowBackward] 是否允许数据倒灌, 接收到旧数据
     * */
    fun <T> LiveData<T>.observeOnce(
        allowBackward: Boolean = true,
        action: (data: T?) -> Boolean
    ): Observer<T?> = watchOnce(this@AbsLifecycleFragment, allowBackward, action)

    val foreverObserveMap = hashMapOf<LiveData<*>, Observer<*>>()

    /**永久[LiveData], 直到手动移除观察者
     * [autoClear] 收到有效数据后, 是否自动清除数据
     * [allowBackward] 是否允许数据倒灌, 接收到旧数据
     * */
    fun <T> LiveData<T>.observeForever(
        autoClear: Boolean = false,
        allowBackward: Boolean = true,
        action: (data: T?) -> Unit
    ): Observer<T?> {
        val result: Observer<T?> = watchForever(autoClear, allowBackward, action)
        foreverObserveMap[this] = result
        return result
    }

    //</editor-fold>
}