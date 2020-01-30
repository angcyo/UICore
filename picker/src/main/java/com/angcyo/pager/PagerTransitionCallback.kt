package com.angcyo.pager

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import androidx.viewpager.widget.ViewPager
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library.ex.copyDrawable
import com.angcyo.loader.LoaderMedia
import com.angcyo.loader.loadPath
import com.angcyo.pager.dslitem.IPlaceholderDrawableProvider
import com.angcyo.transition.ColorTransition
import com.angcyo.widget.DslViewHolder
import com.angcyo.picker.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class PagerTransitionCallback : ViewTransitionCallback(), ViewPager.OnPageChangeListener,
    IPlaceholderDrawableProvider {

    /**单个[View]时使用.*/
    var fromView: View? = null

    /**在[RecyclerView]中启动*/
    var fromRecyclerView: RecyclerView? = null

    /**需要显示的媒体数据*/
    val loaderMedia = mutableListOf<LoaderMedia>()
    /**开始显示的位置*/
    var startPosition: Int = 0
        set(value) {
            field = value
            _primaryPosition = value
        }

    /**获取联动目标的[View]*/
    var onGetFromView: (position: Int) -> View? = { position ->
        if (fromRecyclerView != null) {
            (fromRecyclerView?.findViewHolderForAdapterPosition(position)
                    as? DslViewHolder)?.transitionView() ?: fromView
        } else {
            fromView
        }
    }

    /**页面切换回调*/
    var onPageChanged: (position: Int) -> Unit = { position ->
        fromRecyclerView?.scrollToPosition(position)
    }

    /**过渡动画id列表*/
    @IdRes
    var transitionViewIds = mutableListOf(R.id.lib_image_view)

    /**根据[loadUrl]获取占位图*/
    var onGetPlaceholderDrawable: (loadUrl: String?) -> Drawable? = { loadUrl ->
        var result: Drawable? = null
        loaderMedia.forEachIndexed { index, loaderMedia ->
            if (loaderMedia.loadPath() == loadUrl) {
                onGetFromView(index)?.apply {
                    if (this is ImageView && this.drawable !is ColorDrawable) {
                        result = this.drawable?.copyDrawable()
                    }
                }
            }
        }
        result
    }

    //<editor-fold desc="操作方法">

    /**追加媒体*/
    fun addMedia(url: String?) {
        loaderMedia.add(LoaderMedia(url = url))
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="内部处理方法">

    override fun getPlaceholderDrawable(loadUrl: String?): Drawable? {
        return onGetPlaceholderDrawable(loadUrl)
    }

    override fun transitionTargetView(viewHolder: DslViewHolder): View? {
        return viewHolder.transitionView()
    }

    /**获取转场动画作用的View*/
    fun DslViewHolder.transitionView(): View? {
        var result: View? = null
        for (id in transitionViewIds) {
            val view = view(id)
            if (view != null) {
                result = view
                break
            }
        }
        return result
    }

    override fun onSetShowTransitionSet(
        viewHolder: DslViewHolder,
        transitionSet: TransitionSet
    ): TransitionSet {
        transitionSet.apply {
            addTransition(ColorTransition().addTarget(backgroundTransitionView(viewHolder)))
            //改变左上右下坐标
            addTransition(ChangeBounds())
            //改变绘制的Matrix
            addTransition(ChangeTransform())
            //相当于clip Matrix
            addTransition(ChangeImageTransform())
            //有上面3个即可实现图片转场动画

            //改变clip矩形
            addTransition(ChangeClipBounds())
            //addTransition(Fade(Fade.OUT))
            //addTransition(Fade(Fade.IN))

            viewHolder.transitionView()?.run { addTarget(this) }
            addTarget(backgroundTransitionView(viewHolder))
        }
        return transitionSet
    }

    //临时对象
    val _tempRect = Rect()

    override fun onCaptureShowStartValues(viewHolder: DslViewHolder) {

        val fromView = onGetFromView(_primaryPosition)

        if (transitionShowFromRect == null) {
            fromView?.apply {
                getViewRect(_tempRect)
                transitionShowFromRect = _tempRect
            }
        }

        transitionTargetView(viewHolder)?.apply {
            //图片控件赋值
            if (this is ImageView && fromView is ImageView) {
                scaleType = fromView.scaleType
            }
        }

        super.onCaptureShowStartValues(viewHolder)
    }

    override fun onCaptureShowEndValues(viewHolder: DslViewHolder) {
        super.onCaptureShowEndValues(viewHolder)

        transitionTargetView(viewHolder)?.apply {
            if (this is ImageView) {
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }
    }

    override fun onCaptureHideStartValues(viewHolder: DslViewHolder) {
        super.onCaptureHideStartValues(viewHolder)
    }

    override fun onCaptureHideEndValues(viewHolder: DslViewHolder) {
        val toView = onGetFromView(_primaryPosition)

        if (transitionHideToRect == null) {
            toView?.apply {
                getViewRect(_tempRect)
                transitionHideToRect = _tempRect
            }
        }

        transitionTargetView(viewHolder)?.apply {
            //图片控件赋值
            if (this is ImageView && toView is ImageView) {
                scaleType = toView.scaleType
            }
        }

        super.onCaptureHideEndValues(viewHolder)
    }

    //</editor-fold desc="内部处理方法">

    //<editor-fold desc="ViewPager事件">

    var _primaryPosition: Int = 0

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        _primaryPosition = position
        onPageChanged(position)
    }

    //</editor-fold desc="ViewPager事件">

}