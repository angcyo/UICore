package com.angcyo.fragment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.angcyo.base.getColor
import com.angcyo.library.L.d
import com.angcyo.library.L.i
import com.angcyo.library.L.v
import com.angcyo.library.L.w
import com.angcyo.widget.DslViewHolder

/**
 * Created by angcyo on 2018/12/03 23:17
 *
 * 一些生命周期日志的输出,和创建跟视图
 *
 * @author angcyo
 */
abstract class AbsFragment : Fragment() {

    //<editor-fold desc="对象变量">

    /**
     * ViewHolder 中 SparseArray 初始化的容量, 防止扩容带来的性能损失
     */
    var viewHolderInitialCapacity: Int = DslViewHolder.DEFAULT_INITIAL_CAPACITY

    @LayoutRes
    var fragmentLayoutId: Int = -1

    //</editor-fold desc="对象变量">

    //<editor-fold desc="对象属性">
    lateinit var baseViewHolder: DslViewHolder

    lateinit var attachContext: Context

    //</editor-fold">

    /**
     * 保存回调方法之前的状态值
     */
    var mUserVisibleHintOld = true
    var mHiddenOld = false

    //<editor-fold desc="生命周期, 系统的方法">

    /**
     * 此方法, 通常在 hide show fragment的时候调用
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val old = mHiddenOld
        mHiddenOld = hidden
        v(this.javaClass.simpleName + " hiddenOld:" + old + " hidden:" + hidden + " isAdded:" + isAdded)
        onVisibleChanged(old, mUserVisibleHintOld, !hidden)
    }

    /**
     * 此方法, 通常在 FragmentStatePagerAdapter 中调用
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val old = mUserVisibleHintOld
        mUserVisibleHintOld = isVisibleToUser
        v(this.javaClass.simpleName + " isVisibleToUserOld:" + old + " isVisibleToUser:" + isVisibleToUser + " isAdded:" + isAdded)
        onVisibleChanged(mHiddenOld, old, isVisibleToUser)
    }

    /**
     * 可见性变化
     */
    open fun onVisibleChanged(
        oldHidden: Boolean,
        oldUserVisibleHint: Boolean,
        visible: Boolean /*是否可见*/
    ) {
        d(
            this.javaClass.simpleName + " isAdded:" + isAdded
                    + " hidden:" + oldHidden + "->" + isHidden + " visible:" + oldUserVisibleHint + "->" + userVisibleHint + " ->" + visible
        )
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        val builder = StringBuilder()
        //FragmentHelper.logFragment(childFragment, builder)
        d(this.javaClass.simpleName + builder)
    }

    override fun getContext(): Context {
        return super.getContext() ?: attachContext
    }

    /**
     * OnAttach -> OnCreate -> OnCreateView (initBaseView) -> OnActivityCreated -> OnViewStateRestored -> OnStart -> OnResume
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachContext = context
        d(this.javaClass.simpleName + "\n" + context + " id:" + id + " tag:" + tag + "\nParent:" + parentFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        d(this.javaClass.simpleName + " " + savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        d(this.javaClass.simpleName + "\n" + container + " state:" + if (savedInstanceState == null) "×" else "√")
        val layoutId = fragmentLayoutId
        val rootView: View
        rootView = if (layoutId != -1) {
            inflater.inflate(layoutId, container, false)
        } else {
            createRootView()
        }
        baseViewHolder = DslViewHolder(rootView, viewHolderInitialCapacity)
        initBaseView(baseViewHolder, arguments, savedInstanceState)
        return rootView
    }

    /**
     * 状态恢复, 回调顺序 最优先
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        d(this.javaClass.simpleName + " state:" + if (savedInstanceState == null) "×" else "√")
    }

    override fun onStart() {
        super.onStart()
        d(this.javaClass.simpleName)
    }

    override fun onResume() {
        super.onResume()
        d(this.javaClass.simpleName)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        d(this.javaClass.simpleName + " " + outState)
    }

    /**
     * View需要恢复状态
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        d(this.javaClass.simpleName + " state:" + if (savedInstanceState == null) "×" else "√")
    }

    /**
     * OnPause -> OnStop -> OnDestroyView -> OnDestroy -> OnDetach
     */
    override fun onPause() {
        super.onPause()
        d(this.javaClass.simpleName)
    }

    override fun onStop() {
        super.onStop()
        d(this.javaClass.simpleName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        d(this.javaClass.simpleName)
        baseViewHolder.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        d(this.javaClass.simpleName)
    }

    override fun onDetach() {
        super.onDetach()
        d(this.javaClass.simpleName)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        i(this.javaClass.simpleName + " request:" + requestCode + " result:" + resultCode + " " + data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        w("\n" + newConfig.toString())
        onOrientationChanged(newConfig.orientation)
    }

    open fun onOrientationChanged(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) { //切换到横屏
            onOrientationToLandscape()
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) { //切换到竖屏
            onOrientationToPortrait()
        }
    }

    open fun onOrientationToLandscape() {}

    open fun onOrientationToPortrait() {}

    //</editor-fold>

    //<editor-fold desc="自定义, 可以重写 的方法">

    /**
     * 不指定布局Id的时候, 可以用代码创建跟视图
     */
    open fun createRootView(): View {
        val view = View(context)
        view.setBackgroundColor(getColor(R.color.status_bar_color))
        return view
    }

    /***/
    open fun initBaseView(
        viewHolder: DslViewHolder,
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

    }

    //</editor-fold>
}