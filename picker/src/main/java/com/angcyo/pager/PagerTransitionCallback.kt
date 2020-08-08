package com.angcyo.pager

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.angcyo.dsladapter.getViewRect
import com.angcyo.image.dslitem.IDrawableProvider
import com.angcyo.library.ex.copyDrawable
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.get

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class PagerTransitionCallback : ViewTransitionCallback(), ViewPager.OnPageChangeListener,
    IDrawableProvider {

    /**单个[View]时使用.*/
    var fromView: View? = null

    /**在[RecyclerView]中启动*/
    var fromRecyclerView: RecyclerView? = null

    /**需要显示的媒体数据*/
    var loaderMediaList = mutableListOf<LoaderMedia>()

    /**开始显示的位置*/
    var startPosition: Int = 0
        set(value) {
            field = value
            _primaryPosition = value
        }

    /**创建页面适配器*/
    var onCreatePagerAdapter: (() -> PagerAdapter?)? = null

    /**界面加载的[position], 可以并没有一一对应在[RecyclerView]的布局中*/
    var onPositionConvert: (position: Int) -> Int = { it }

    /**获取联动目标的[View]*/
    var onGetFromView: (position: Int) -> View? = { position ->
        fromRecyclerView?.get(onPositionConvert(position))?.transitionView() ?: fromView
    }

    /**页面切换回调*/
    var onPageChanged: (position: Int) -> Unit = { position ->
        fromRecyclerView?.scrollToPosition(onPositionConvert(position))
    }

    /**过渡动画id列表*/
    var transitionViewIds = mutableListOf(R.id.lib_image_view)

    /**根据[loadUrl]获取占位图*/
    var onGetPlaceholderDrawable: (loadUri: Uri?) -> Drawable? = { loadUri ->
        var result: Drawable? = null
        loaderMediaList.forEachIndexed { index, loaderMedia ->
            if (loaderMedia.loadUri() == loadUri) {
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
        if (!url.isNullOrEmpty()) {
            addMedia(LoaderMedia(url = url))
        }
    }

    fun addMedia(list: List<String>?) {
        list?.forEach {
            addMedia(it)
        }
    }

    fun addMedia(uri: Uri?) {
        addMedia(LoaderMedia(localUri = uri))
    }

    fun addMedia(media: LoaderMedia) {
        loaderMediaList.add(media)
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="内部处理方法">

    override fun getPlaceholderDrawable(loadUri: Uri?): Drawable? {
        return onGetPlaceholderDrawable(loadUri)
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
            } else if (fromView == null) {
                //未指定fromView时, 使用 scale 动画
                scaleX = 0f
                scaleY = 0f
            }
        }

        super.onCaptureShowStartValues(viewHolder)
    }

    override fun onCaptureShowEndValues(viewHolder: DslViewHolder) {
        super.onCaptureShowEndValues(viewHolder)

        transitionTargetView(viewHolder)?.apply {
            scaleX = 1f
            scaleY = 1f
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
            } else if (toView == null) {
                //目标不存在时, 使用 scale 动画
                scaleX = 0f
                scaleY = 0f
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