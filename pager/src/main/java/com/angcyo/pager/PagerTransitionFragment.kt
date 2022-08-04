package com.angcyo.pager

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.angcyo.base.interceptTouchEvent
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.set
import com.angcyo.library.model.isAudio
import com.angcyo.library.model.isVideo
import com.angcyo.library.model.loadUri
import com.angcyo.media.dslitem.DslPreviewAudioItem
import com.angcyo.media.dslitem.DslTextureVideoItem
import com.angcyo.pager.dslitem.DslPagerPhotoViewItem
import com.angcyo.tablayout.evaluateColor
import com.angcyo.widget._vp
import com.angcyo.widget.layout.MatrixLayout
import com.angcyo.widget.pager.DslPagerAdapter
import com.angcyo.widget.pager.TextIndicator
import com.angcyo.widget.pager.getPrimaryViewHolder
import com.github.chrisbanes.photoview.PhotoView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class PagerTransitionFragment : ViewTransitionFragment() {

    val pagerTransitionCallback get() = transitionCallback as PagerTransitionCallback

    //页面监听
    val pageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
            pagerTransitionCallback.pageSelected(this@PagerTransitionFragment, position)
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

    //下拉返回配置
    val onMatrixTouchListener: MatrixLayout.OnMatrixTouchListener =
        object : MatrixLayout.OnMatrixTouchListener {
            override fun checkTouchEvent(matrixLayout: MatrixLayout): Boolean {
                val vh = _vh._vp(R.id.lib_view_pager)?.getPrimaryViewHolder()

                if (vh == null) {
                    return true
                } else {
                    val transitionView = pagerTransitionCallback.transitionTargetView(vh)
                    if (transitionView is PhotoView) {
                        return transitionView.scale <= 1
                    }
                    return true
                }
            }

            override fun onMatrixChange(
                matrixLayout: MatrixLayout,
                matrix: Matrix,
                fromRect: RectF,
                toRect: RectF
            ) {
                val color = evaluateColor(
                    (1 - toRect.top / fromRect.bottom),
                    pagerTransitionCallback.backgroundStartColor,
                    pagerTransitionCallback.backgroundEndColor
                )
                pagerTransitionCallback.backgroundTransitionView(_vh)
                    .setBackgroundColor(color)
            }

            override fun onTouchEnd(
                matrixLayout: MatrixLayout,
                matrix: Matrix,
                fromRect: RectF,
                toRect: RectF
            ): Boolean {
                return if (toRect.top / fromRect.bottom > 0.3f) {
                    //关闭界面
                    pagerTransitionCallback.transitionHideFromRect = Rect().set(toRect)
                    matrixLayout.resetMatrix()
                    backTransition()
                    true
                } else {
                    false
                }
            }
        }

    init {
        fragmentLayoutId = R.layout.lib_pager_transition_fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (transitionCallback !is PagerTransitionCallback) {
            throw IllegalArgumentException("transitionCallback not PagerTransitionCallback.")
        }
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        _vh.v<MatrixLayout>(R.id.lib_matrix_layout)?.onMatrixTouchListener = onMatrixTouchListener

        _vh._vp(R.id.lib_view_pager)?.apply {
            val pagerAdapter = onCreatePagerAdapter()
            adapter = pagerAdapter

            //页面监听
            addOnPageChangeListener(pageChangeListener)

            val startPosition = pagerTransitionCallback.startPosition

            val fixIndex = when {
                pagerAdapter == null -> 0
                startPosition < 0 -> 0
                startPosition >= pagerAdapter.count -> pagerAdapter.count - 1
                else -> startPosition
            }

            setCurrentItem(fixIndex, false)

            //主动通知监听
            if (currentItem == fixIndex) {
                pageChangeListener.onPageSelected(currentItem)
            }

            //在后面添加事件, 那么第一次就不会触发[onPageSelected]
            addOnPageChangeListener(pagerTransitionCallback)

            //文本指示器
            _vh.v<TextIndicator>(R.id.lib_text_indicator)?.setupViewPager(this)
        }
    }

    //<editor-fold desc="其他元素控制">

    fun hideOtherView() {
        _vh.invisible(R.id.lib_text_indicator)
    }

    fun showOtherView() {
        if (pagerTransitionCallback.loaderMediaList.size > 1) {
            _vh.visible(R.id.lib_text_indicator)
        }
    }

    override fun onTransitionShowStart() {
        super.onTransitionShowStart()
        hideOtherView()
    }

    override fun onTransitionShowEnd() {
        super.onTransitionShowEnd()
        showOtherView()
    }

    override fun onTransitionHideStart() {
        super.onTransitionHideStart()
        hideOtherView()
    }

    //</editor-fold desc="其他元素控制">

    override fun startTransition(start: Boolean) {
        activity?.interceptTouchEvent(true)

        dslTransition.apply {
            sceneRoot = _vh.itemView as? ViewGroup

            transitionCallback.sceneRoot = sceneRoot
            val vh = _vh._vp(R.id.lib_view_pager)?.getPrimaryViewHolder() ?: _vh
            _configTransition(start, vh)
        }
    }

    open fun onCreatePagerAdapter(): PagerAdapter? {
        val adapter = pagerTransitionCallback.onCreatePagerAdapter?.invoke()

        if (adapter != null) {
            return adapter
        }

        val items = mutableListOf<DslAdapterItem>()

        pagerTransitionCallback.loaderMediaList.forEach {
            when {
                it.isVideo() -> items.add(DslTextureVideoItem().apply {
                    itemData = it
                    itemVideoUri = it.loadUri()
                })
                it.isAudio() -> items.add(DslPreviewAudioItem().apply {
                    itemData = it
                    itemAudioTitle = it.displayName
                    itemAudioDuration = it.duration
                    itemAudioUri = it.loadUri()
                })
                else -> items.add(DslPagerPhotoViewItem().apply {
                    itemData = it
                    itemLoadUri = it.loadUri()

                    //占位图提供
                    drawableProvider = pagerTransitionCallback

                    //点击图片关闭界面
                    itemClick = {
                        backTransition()
                    }
                })
            }
        }

        return DslPagerAdapter(items)
    }
}