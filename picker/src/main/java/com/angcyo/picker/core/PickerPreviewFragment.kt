package com.angcyo.picker.core

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.angcyo.base.dslFHelper
import com.angcyo.dsladapter.*
import com.angcyo.getData
import com.angcyo.library.ex._drawable
import com.angcyo.loader.LoaderMedia
import com.angcyo.loader.loadUri
import com.angcyo.pager.dslitem.DslPhotoViewItem
import com.angcyo.picker.R
import com.angcyo.picker.dslitem.DslPickerMiniImageItem
import com.angcyo.widget._rv
import com.angcyo.widget._vp
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.fullscreen
import com.angcyo.widget.pager.DslPagerAdapter
import com.angcyo.widget.recycler.initDsl
import com.angcyo.widget.span.span
import com.angcyo.widget.vp

/**
 * 媒体预览界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/18
 */
class PickerPreviewFragment : BasePickerFragment() {

    /**预览配置信息*/
    var previewConfig = PreviewConfig()

    /**页面监听*/
    val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            _updatePageSelector(position)
        }
    }

    /**小图预览适配器*/
    val miniAdapter = DslAdapter().apply {
        singleModel()
    }

    /**媒体列表*/
    val previewMediaList = mutableListOf<LoaderMedia>()
    /**预览模式下, 保存之前选中的媒体列表*/
    val previewSelectorMediaList = mutableListOf<LoaderMedia>()

    init {
        fragmentLayoutId = R.layout.picker_preview_fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewConfig = getData() ?: previewConfig

        val mediaList =
            if (previewConfig.previewSelectorList) pickerViewModel.selectorMediaList.value else pickerViewModel.currentFolder.value?.mediaItemList

        if (previewConfig.previewSelectorList) {
            previewSelectorMediaList.addAll(pickerViewModel.selectorMediaList.value ?: emptyList())
        }

        previewMediaList.addAll(mediaList ?: emptyList())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _vh._vp(R.id.lib_view_pager)?.apply {

            offscreenPageLimit = 1

            val items = mutableListOf<DslAdapterItem>()

            previewMediaList.forEach {
                items.add(DslPhotoViewItem().apply {
                    itemData = it
                    itemLoadUri = it.loadUri()

                    //点击图片关闭界面
                    onItemClick = {
                        _fullscreen()
                    }
                })
            }

            adapter = DslPagerAdapter(items)

            setCurrentItem(previewConfig.previewStartPosition, false)

            //在后面添加事件, 那么第一次就不会触发[onPageSelected]
            addOnPageChangeListener(pageChangeListener)

            pageChangeListener.onPageSelected(previewConfig.previewStartPosition)
        }

        _vh.click(R.id.selected_cb) {
            //切换选中状态
            val media = previewMediaList.getOrNull(_vh.vp(R.id.lib_view_pager)?.currentItem ?: 0)
            media?.run {
                if (_vh.isChecked(R.id.selected_cb)) {
                    //点击完之后, 是checked的状态
                    if (pickerViewModel.canSelectorMedia(this)) {
                        pickerViewModel.addSelectedMedia(this)
                    } else {
                        _vh.cb(R.id.selected_cb)?.isChecked = false
                    }
                } else {
                    pickerViewModel.removeSelectedMedia(this)
                }
            }
        }

        //编辑按钮
        _vh.visible(
            R.id.edit_text_view,
            pickerViewModel.loaderConfig.value?.enableImageEdit ?: true
        )

        //小图预览
        _vh.rv(R.id.mini_recycler_view)?.run {
            initDsl()
            adapter = miniAdapter
        }

        _showMiniPreview()
    }

    fun _fullscreen() {
        val full = !_vh.itemView.isSelected
        _vh.itemView.isSelected = full
        _vh.itemView.fullscreen(full)

        if (full) {
            _vh.view(R.id.title_wrap_layout)?.run {
                animate()
                    .translationY((-measuredHeight).toFloat())
                    .setDuration(Anim.ANIM_DURATION)
                    .start()
            }

            _vh.view(R.id.bottom_wrap_layout)?.run {
                animate()
                    .translationY((measuredHeight).toFloat())
                    .setDuration(Anim.ANIM_DURATION)
                    .start()
            }
        } else {
            _vh.view(R.id.title_wrap_layout)?.run {
                animate()
                    .translationY(0f)
                    .setDuration(Anim.ANIM_DURATION)
                    .start()
            }

            _vh.view(R.id.bottom_wrap_layout)?.run {
                animate()
                    .translationY(0f)
                    .setDuration(Anim.ANIM_DURATION)
                    .start()
            }
        }
    }

    /**[ViewPager]页面切换*/
    fun _updatePageSelector(position: Int) {
        _vh.tv(R.id.picker_close_view)?.text = span {
            drawable {
                backgroundDrawable = _drawable(R.drawable.lib_back)
            }
            drawable("${position + 1}/${previewMediaList.size}") {
                textGravity = Gravity.CENTER
            }
        }

        _vh.cb(R.id.selected_cb)?.isChecked =
            pickerViewModel.selectorMediaList.value?.contains(previewMediaList.getOrNull(position))
                ?: false

        _showMiniPreview()
    }

    /**小图预览*/
    fun _showMiniPreview() {

        //真正选中的列表
        val selectorList = pickerViewModel.selectorMediaList.value

        //需要显示的列表
        val mediaList = if (previewConfig.previewSelectorList) {
            //预览选择模式
            previewSelectorMediaList
        } else {
            //预览模式
            selectorList
        }

        val currentMedia: LoaderMedia? =
            previewMediaList.getOrNull(_vh.vp(R.id.lib_view_pager)?.currentItem ?: -1)

        if (selectorList.isNullOrEmpty()) {
            _vh.gone(R.id.mini_recycler_view)
            _vh.gone(R.id.mini_line_view)
        } else {
            //滚动到当前位置
            miniAdapter.onDispatchUpdatesAfterOnce = {
                val currentItem = it.findItem {
                    it.itemData == currentMedia
                }?.apply {
                    _vh._rv(R.id.mini_recycler_view)
                        ?.scrollToPosition(itemIndexPosition(miniAdapter))
                }

                //选中一个null item, 会取消之前的选中
                miniAdapter.selector().selector(currentItem, true)

                if (previewConfig.previewSelectorList) {
                    currentItem?.updateAdapterItem()
                }
            }

            _vh.visible(R.id.mini_recycler_view)
            _vh.visible(R.id.mini_line_view)
            miniAdapter.loadSingleData<DslPickerMiniImageItem>(
                mediaList,
                1,
                Int.MAX_VALUE
            ) { oldItem, _ ->
                (oldItem ?: DslPickerMiniImageItem()).apply {
                    itemIsDeleted = selectorList.contains(loaderMedia) != true

                    //滚动到选中的item
                    onItemClick = {
                        _vh.vp(R.id.lib_view_pager)
                            ?.setCurrentItem(previewMediaList.indexOf(loaderMedia), false)
                    }
                }
            }
        }
    }

    override fun onSelectorMediaListChange(mediaList: MutableList<LoaderMedia>?) {
        super.onSelectorMediaListChange(mediaList)
        _showMiniPreview()
    }

    override fun onBackPressed(): Boolean {
        dslFHelper {
            exitAnimRes = R.anim.lib_picker_preview_exit_anim
            remove(this@PickerPreviewFragment)
        }
        return false
    }

}