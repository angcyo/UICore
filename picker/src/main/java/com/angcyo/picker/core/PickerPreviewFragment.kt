package com.angcyo.picker.core

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.angcyo.base.dslFHelper
import com.angcyo.dsladapter.*
import com.angcyo.getData
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.fileSize
import com.angcyo.library.ex.isResultOk
import com.angcyo.loader.*
import com.angcyo.media.dslitem.DslPreviewAudioItem
import com.angcyo.media.dslitem.DslTextureVideoItem
import com.angcyo.pager.dslitem.DslPhotoViewItem
import com.angcyo.picker.R
import com.angcyo.picker.dslitem.DslPickerMiniImageItem
import com.angcyo.ucrop.dslCrop
import com.angcyo.widget._rv
import com.angcyo.widget._vp
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.lowProfile
import com.angcyo.widget.pager.DslPagerAdapter
import com.angcyo.widget.recycler.initDsl
import com.angcyo.widget.span.span
import com.angcyo.widget.vp
import com.yalantis.ucrop.UCrop

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

    /**当前[ViewPager]对应的[LoaderMedia]*/
    val pageLoaderMedia: LoaderMedia?
        get() = previewMediaList.getOrNull(_vh.vp(R.id.lib_view_pager)?.currentItem ?: -1)

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
                    else -> items.add(DslPhotoViewItem().apply {
                        itemData = it
                        itemLoadUri = it.loadUri()

                        //点击图片关闭界面
                        onItemClick = {
                            _fullscreen()
                        }
                    })
                }
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

        _vh.click(R.id.edit_text_view) {
            //编辑图片
            pageLoaderMedia?.also { loaderMedia ->
                dslCrop(this@PickerPreviewFragment) {
                    cropUri = loaderMedia.loadUri()!!
                    maxResultWidth = pickerViewModel.loaderConfig.value?.outputImageWidth ?: -1
                    maxResultHeight = pickerViewModel.loaderConfig.value?.outputImageHeight ?: -1
                    onResult = { resultCode, cropUri, data ->
                        if (resultCode.isResultOk()) {
                            loaderMedia.fileSize = cropUri.path.fileSize()
                            loaderMedia.width = data!!.getIntExtra(
                                UCrop.EXTRA_OUTPUT_IMAGE_WIDTH,
                                loaderMedia.width
                            )
                            loaderMedia.height = data.getIntExtra(
                                UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT,
                                loaderMedia.height
                            )
                            loaderMedia.cropPath = cropUri.path

                            _vh._vp(R.id.lib_view_pager)?.dslPagerAdapter?.notifyItemChanged()

                            if (pickerViewModel.selectorMediaList.value?.contains(loaderMedia) == true) {
                                //已经选中
                                _showMiniPreview(true)
                            } else {
                                //添加选中
                                if (pickerViewModel.canSelectorMedia(loaderMedia)) {
                                    pickerViewModel.addSelectedMedia(loaderMedia)
                                    _vh.cb(R.id.selected_cb)?.isChecked = true
                                }
                                //更新模拟预览图
                                _showMiniPreview()
                            }

                        }
                    }
                }
            }
        }

        //小图预览初始化
        _vh.rv(R.id.mini_recycler_view)?.run {
            initDsl()
            adapter = miniAdapter
        }

        _showMiniPreview()
    }

    fun _fullscreen(yes: Boolean? = null) {
        val full = yes ?: !_vh.itemView.isSelected
        _vh.itemView.isSelected = full
        //_vh.itemView.fullscreen(full) //全面屏 切换全屏时, 状态栏会跳动.改用体验更好的低调模式.
        _vh.itemView.lowProfile(full)

        if (full) {
            _vh.view(R.id.title_wrap_layout)?.run {
                animate()
                    .translationY((-measuredHeight).toFloat())
                    .setDuration(Anim.ANIM_DURATION)
                    .withEndAction {
                        _vh.gone(R.id.title_wrap_layout)
                    }
                    .start()
            }

            _vh.view(R.id.bottom_wrap_layout)?.run {
                animate()
                    .translationY((measuredHeight).toFloat())
                    .setDuration(Anim.ANIM_DURATION)
                    .withEndAction {
                        _vh.gone(R.id.bottom_wrap_layout)
                    }
                    .start()
            }
        } else {
            _vh.visible(R.id.bottom_wrap_layout)
            _vh.visible(R.id.title_wrap_layout)
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

        //可见性控制
        pageLoaderMedia?.apply {
            if (isImage()) {
                //编辑按钮
                _vh.visible(
                    R.id.edit_text_view,
                    pickerViewModel.loaderConfig.value?.enableImageEdit == true
                )
                //原图选择按钮
                _vh.visible(
                    R.id.origin_cb,
                    pickerViewModel.loaderConfig.value?.enableOrigin == true
                )
            } else {
                _vh.gone(R.id.edit_text_view)
                _vh.gone(R.id.origin_cb)

                //切换到非图片
                //_fullscreen(false)
            }
        }

        _showMiniPreview()
    }

    /**小图预览*/
    fun _showMiniPreview(updateMedia: Boolean = false) {

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

        if (selectorList.isNullOrEmpty()) {
            _vh.gone(R.id.mini_recycler_view)
            _vh.gone(R.id.mini_line_view)
        } else {
            //滚动到当前位置
            miniAdapter.onDispatchUpdatesAfterOnce = {
                val currentItem = it.findItem {
                    it.itemData == pageLoaderMedia
                }?.apply {
                    _vh._rv(R.id.mini_recycler_view)
                        ?.scrollToPosition(itemIndexPosition(miniAdapter))
                }

                //选中一个null item, 会取消之前的选中
                miniAdapter.selector().selector(currentItem, true)

                if (previewConfig.previewSelectorList || updateMedia) {
                    //更新删除标识,更新剪裁后的图片
                    currentItem?.updateAdapterItem(
                        if (updateMedia) listOf(
                            DslAdapterItem.PAYLOAD_UPDATE_PART,
                            DslAdapterItem.PAYLOAD_UPDATE_MEDIA
                        ) else DslAdapterItem.PAYLOAD_UPDATE_PART
                    )
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
                    //只在预览选中列表时, 开启删除标识模式
                    itemIsDeleted =
                        previewConfig.previewSelectorList && selectorList.contains(loaderMedia) != true

                    //滚动到选中的item
                    onItemClick = {
                        val indexOf = previewMediaList.indexOf(loaderMedia)
                        if (indexOf != -1) {
                            _vh.vp(R.id.lib_view_pager)
                                ?.setCurrentItem(indexOf, false)
                        }
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
            anim(0, R.anim.lib_picker_preview_exit_anim)
            remove(this@PickerPreviewFragment)
        }
        return false
    }

}