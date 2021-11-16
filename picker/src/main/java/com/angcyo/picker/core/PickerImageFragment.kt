package com.angcyo.picker.core

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.base.dslFHelper
import com.angcyo.dialog.fullPopupWindow
import com.angcyo.dsladapter.*
import com.angcyo.dsladapter.data.loadSingleData2
import com.angcyo.library.L
import com.angcyo.library.component.DslCalendar
import com.angcyo.library.ex.*
import com.angcyo.library.model.LoaderMedia
import com.angcyo.loader.DslLoader
import com.angcyo.loader.FolderCreator
import com.angcyo.loader.LoaderConfig
import com.angcyo.loader.LoaderFolder
import com.angcyo.picker.DslPicker
import com.angcyo.picker.R
import com.angcyo.picker.dslitem.DslPickerCameraPreviewItem
import com.angcyo.picker.dslitem.DslPickerFolderItem
import com.angcyo.picker.dslitem.DslPickerImageItem
import com.angcyo.putData
import com.angcyo.widget._rv
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.anim
import com.angcyo.widget.recycler.decoration.DslDrawItemDecoration
import com.angcyo.widget.recycler.decoration.isLayoutFirst
import com.angcyo.widget.recycler.initDslAdapter
import com.angcyo.widget.recycler.localUpdateItem

/**
 * 媒体选择列表界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class PickerImageFragment : BasePickerFragment() {

    //媒体loader
    val loader = DslLoader()

    //日期绘制¬
    val timeItemDecoration = DslDrawItemDecoration()

    //通过控制透明度, 达到显示和隐藏的效果
    var dslDrawItemDecorationAlpha = 0
    var dslDrawItemDecorationAlphaAnimator: ValueAnimator? = null

    init {
        fragmentLayoutId = R.layout.picker_image_fragment
    }

    override fun onInitDslLayout(recyclerView: RecyclerView, dslAdapter: DslAdapter) {
        super.onInitDslLayout(recyclerView, dslAdapter)

        //多选模式
        dslAdapter.multiModel()

        timeItemDecoration.attachToRecyclerView(recyclerView)

        //日期绘制
        timeItemDecoration.onItemDrawOver = {
            timeItemDecoration.drawItemDecoration(it) {
                draw = { canvas, paint, _, drawRect ->
                    if (it.isLayoutFirst()) {
                        _getPositionTime(it.viewHolder.adapterPosition)?.run {
                            drawRect.set(0, 0, it.parent.measuredWidth, 30 * dpi)
                            paint.color = "#000000".toColorInt()
                                .alpha(dslDrawItemDecorationAlpha * 1f / 255 * 168)
                            canvas.drawRect(drawRect, paint)
                            paint.color = Color.WHITE.alpha(dslDrawItemDecorationAlpha)
                            canvas.drawText(
                                this,
                                10 * dp,
                                drawRect.height() / 2 + paint.textHeight() / 2 - paint.descent(),
                                paint
                            )
                        }
                    }
                }
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //L.i(newState.scrollStateStr())
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //滚动结束, 隐藏日期提示
                    dslDrawItemDecorationAlphaAnimator = anim(255, 0) {
                        onAnimatorConfig = {
                            it.startDelay = 300
                        }
                        onAnimatorUpdateValue = { value, _ ->
                            dslDrawItemDecorationAlpha = value as Int
                            recyclerView.postInvalidateOnAnimation()
                        }
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    dslDrawItemDecorationAlphaAnimator?.cancel()
                    //开始滚动, 显示日期提示
                    dslDrawItemDecorationAlphaAnimator = anim(dslDrawItemDecorationAlpha, 255) {
                        onAnimatorUpdateValue = { value, _ ->
                            dslDrawItemDecorationAlpha = value as Int
                            recyclerView.postInvalidateOnAnimation()
                        }
                    }
                }
            }
        })
    }

    /**获取指定位置, 需要显示的时间*/
    fun _getPositionTime(position: Int): String? {
        if (position in 0 until _adapter.itemCount) {
            _adapter.getItemData(position)?.run {
                return if (this is DslPickerImageItem) {
                    this.loaderMedia?.addTime?.run {
                        val dslCalendar = DslCalendar(this)
                        when {
                            dslCalendar.isThisWeek() -> "本周"
                            dslCalendar.isThisMonth() -> "这个月"
                            else -> "${dslCalendar.year}/${dslCalendar.month}"
                        }
                    }
                } else {
                    _getPositionTime(position + 1)
                }
            }
        }
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*观察文件夹切换*/
        pickerViewModel.currentFolder.observe {
            _switchFolder(it)
        }

        loaderConfig?.apply {
            loader.onLoaderResult = {
                val folderList = mutableListOf<LoaderFolder>()
                folderList.addAll(it)

                //系统相册无媒体, 此时开启了摄像头功能
                if (it.isEmpty() && enableCamera) {
                    if (mediaLoaderType.have(LoaderConfig.LOADER_TYPE_IMAGE)) {
                        folderList.add(LoaderFolder("所有图片", FolderCreator.ALL_IMAGE))
                    } else if (mediaLoaderType.have(LoaderConfig.LOADER_TYPE_VIDEO)) {
                        folderList.add(LoaderFolder("所有视频", FolderCreator.ALL_VIDEO))
                    }
                }

                folderList.forEach { folder ->
                    //手动追加拍摄的图片
                    if (folder.folderPath == FolderCreator.ALL_IMAGE) {
                        folder.mediaItemList.addAll(0, pickerViewModel.takeImageList)
                    } else if (folder.folderPath == FolderCreator.ALL_VIDEO) {
                        folder.mediaItemList.addAll(0, pickerViewModel.takeVideoList)
                    }
                }

                if (folderList.isEmpty()) {
                    _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
                } else {
                    _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
                    pickerViewModel.loaderFolderList.value = folderList

                    val currentFolder = pickerViewModel.currentFolder.value
                    if (currentFolder == null) {
                        //未选中文件夹, 默认选中第一个
                        pickerViewModel.currentFolder.value = folderList.first()
                    } else {
                        //如果已经选中了, 替换当前的数据
                        val find =
                            folderList.find { folder -> folder.folderPath == currentFolder.folderPath }

                        if (find == null) {
                            //未找到
                            pickerViewModel.currentFolder.value = folderList.first()
                        } else {
                            //找到
                            currentFolder.mediaItemList = find.mediaItemList
                            _switchFolder(currentFolder, false)
                        }

                    }
                }
            }
            loader.startLoader(activity, this)
        } ?: L.w("loaderConfig is null.")

        //事件
        _vh.click(R.id.folder_layout) {
            //切换文件夹
            _showFolderDialog()
        }
        _vh.click(R.id.preview_text_view) {
            //预览
            _showPreview(PreviewConfig(true, 0))
        }
    }

    override fun onFragmentNotFirstShow(bundle: Bundle?) {
        super.onFragmentNotFirstShow(bundle)
        //当从preview界面选中item之后, 需要刷新一下界面
        _recycler.localUpdateItem { adapterItem, itemHolder, itemPosition ->
            val payloads = if (adapterItem.itemIsSelected) {
                listOf(DslAdapterItem.PAYLOAD_UPDATE_PART, DslAdapterItem.PAYLOAD_UPDATE_MEDIA)
            } else {
                listOf(DslAdapterItem.PAYLOAD_UPDATE_PART)
            }
            adapterItem.itemBind(itemHolder, itemPosition, adapterItem, payloads)
        }
    }

    /**在adapter头部, 添加一个摄像头预览item*/
    fun _addCameraItem() {
        if (_adapter.headerItems.isEmpty()) {
            _adapter.headerItems.add(DslPickerCameraPreviewItem().apply {
                itemClick = {
                    val folderPath = pickerViewModel.currentFolder.value?.folderPath
                    val isVideoFolder2 = folderPath == FolderCreator.ALL_VIDEO
                    //val isImageFolder2 = folderPath == FolderCreator.ALL_IMAGE
                    if (isVideoFolder2) {
                        DslPicker.takeVideo(activity) {
                            if (it != null) {
                                val path = it.loadUrl()
                                val meta = path?.getMediaMetadata()

                                val loaderMedia = LoaderMedia(
                                    localUri = it,
                                    fileSize = path?.fileSize() ?: -1,
                                    duration = meta?.getOrNull(0) ?: -1,
                                    width = (meta?.getOrNull(1) ?: -1).toInt(),
                                    height = (meta?.getOrNull(2) ?: -1).toInt(),
                                )
                                pickerViewModel.takeVideoList.add(loaderMedia)

                                if (pickerViewModel.isSingleModel()) {
                                    pickerViewModel.removeSelectedAll()
                                }
                                pickerViewModel.addSelectedMedia(loaderMedia)

                                //单选模式下, 直接返回
                                if (pickerViewModel.isSingleModel() && loaderConfig?.cameraTakeResult == true) {
                                    PickerActivity.send(this@PickerImageFragment)
                                }
                            }
                        }
                    } else {
                        DslPicker.takePhoto(activity) {
                            if (it != null) {
                                val path = it.loadUrl()
                                val size = path?.bitmapSize()

                                val loaderMedia = LoaderMedia(
                                    localUri = it,
                                    fileSize = path?.fileSize() ?: -1,
                                    width = size?.getOrNull(0) ?: -1,
                                    height = size?.getOrNull(0) ?: -1
                                )
                                pickerViewModel.takeImageList.add(loaderMedia)

                                if (pickerViewModel.isSingleModel()) {
                                    pickerViewModel.removeSelectedAll()
                                }
                                pickerViewModel.addSelectedMedia(loaderMedia)

                                //单选模式下, 直接返回
                                if (pickerViewModel.isSingleModel() && loaderConfig?.cameraTakeResult == true) {
                                    PickerActivity.send(this@PickerImageFragment)
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    /**切换显示的文件夹*/
    fun _switchFolder(folder: LoaderFolder?, scrollToFirst: Boolean = true) {
        if (folder == null) {
            return
        }

        //文件夹
        _vh.visible(R.id.folder_layout)
        _vh.tv(R.id.folder_text_view)?.text = folder.folderName

        //摄像头预览
        val isVideoFolder = folder.folderPath == FolderCreator.ALL_VIDEO
        val isImageFolder = folder.folderPath == FolderCreator.ALL_IMAGE
        if (loaderConfig?.enableCamera == true && (isVideoFolder || isImageFolder)) {
            _addCameraItem()
        } else {
            _adapter.headerItems.clear()
        }

        //图片列表
        _adapter.loadSingleData2<DslPickerImageItem>(
            folder.mediaItemList,
            1,
            Int.MAX_VALUE,
            _adapter.defaultFilterParams!!.apply {
                payload = listOf(
                    DslAdapterItem.PAYLOAD_UPDATE_PART,
                    DslAdapterItem.PAYLOAD_UPDATE_MEDIA
                )
                justRun = true
            },
        ) { data ->
            //获取选中状态
            onGetSelectedState = {
                it?.run {
                    pickerViewModel.selectorMediaList.value?.contains(this)
                } ?: itemIsSelected
            }
            //获取选中索引
            onGetSelectedIndex = {
                it?.run {
                    val index = pickerViewModel.selectorMediaList.value?.indexOf(this) ?: -1
                    if (index >= 0) {
                        "${index + 1}"
                    } else {
                        null
                    }
                }
            }
            //选择回调
            onSelectorItem = {
                if (pickerViewModel.isSingleModel()) {
                    //单选模式
                    if (itemIsSelected) {
                        //单选模式下, 不允许取消选择
                    } else {
                        //取消之前, 选中
                        val oldSelectorList = _adapter.itemSelectorHelper.getSelectorItemList()
                        pickerViewModel.removeSelectedAll()
                        _adapter.selector().selector(
                            oldSelectorList,
                            SelectorParams(
                                selector = false.toSelectOption(),
                                payload = listOf(
                                    DslPickerImageItem.PAYLOAD_UPDATE_ANIM,
                                    DslAdapterItem.PAYLOAD_UPDATE_PART
                                )
                            )
                        )

                        //选中, 当前
                        pickerViewModel.addSelectedMedia(loaderMedia)
                        _adapter.selector().selector(
                            SelectorParams(
                                this,
                                true.toSelectOption(),
                                payload = listOf(
                                    DslPickerImageItem.PAYLOAD_UPDATE_ANIM,
                                    DslAdapterItem.PAYLOAD_UPDATE_PART
                                )
                            )
                        )
                    }
                } else {
                    var pass = false
                    if (it) {
                        //已经选中, 则取消选择
                        pickerViewModel.removeSelectedMedia(loaderMedia)
                    } else {
                        //未选中, 则选择
                        if (pickerViewModel.canSelectorMedia(loaderMedia)) {
                            pickerViewModel.addSelectedMedia(loaderMedia)
                        } else {
                            pass = true
                        }
                    }

                    if (pass) {
                        //播放无法选中的动画
                        _adapter.notifyItemChanged(
                            this,
                            payload = listOf(
                                DslPickerImageItem.PAYLOAD_UPDATE_CANCEL_ANIM,
                                DslAdapterItem.PAYLOAD_UPDATE_PART
                            )
                        )
                    } else {
                        //之前选中的列表
                        val oldSelectorList = _adapter.itemSelectorHelper.getSelectorItemList()

                        //当前item选中切换
                        _adapter.selector().selector(
                            SelectorParams(
                                this,
                                (!itemIsSelected).toSelectOption(),
                                payload = listOf(
                                    DslPickerImageItem.PAYLOAD_UPDATE_ANIM,
                                    DslAdapterItem.PAYLOAD_UPDATE_PART
                                )
                            )
                        )
                        //更新其他item的索引值
                        _adapter.updateItems(oldSelectorList, DslAdapterItem.PAYLOAD_UPDATE_PART)
                    }
                }
            }
            //点击事件
            itemClick = {
                //大图预览
                val startPosition =
                    pickerViewModel.currentFolder.value?.mediaItemList?.indexOf(loaderMedia)
                        ?: 0
                _showPreview(PreviewConfig(false, startPosition))
            }

            //选中状态
            itemIsSelected =
                pickerViewModel.selectorMediaList.value?.contains(data) ?: false
            (this as? DslPickerImageItem)?.showFileSize =
                pickerViewModel.loaderConfig.value?.showFileSize ?: false
        }

        //_adapter.updateItemDepend(FilterParams(null, true, true, payload = mediaPayload()))
        if (scrollToFirst) {
            _adapter.onDispatchUpdatesOnce {
                _recycler.scrollHelper.lockScrollToFirst {
                    scrollAnim = false
                }
            }
        }
    }

    /**显示文件夹切换布局*/
    fun _showFolderDialog() {
        var selectorFolder: LoaderFolder? = null
        fContext().fullPopupWindow(_vh.view(R.id.title_wrap_layout)) {
            showWithActivity = true
            layoutId = R.layout.picker_folder_dialog_layout
            onInitLayout = { _, viewHolder ->
                viewHolder._rv(R.id.lib_recycler_view)?.apply {
                    initDslAdapter {
                        defaultFilterParams = _defaultFilterParams().apply {
                            asyncDiff = false
                        }
                        pickerViewModel.loaderFolderList.value?.forEachIndexed { index, folder ->
                            DslPickerFolderItem()() {
                                itemData = folder
                                itemIsSelected = folder == pickerViewModel.currentFolder.value
                                itemClick = {
                                    selectorFolder = folder
                                    hide()
                                }
                                showFolderLine =
                                    index != pickerViewModel.loaderFolderList.value!!.lastIndex
                            }
                        }
                    }
                }
            }

            onDismiss = {
                //箭头旋转动画
                _vh.view(R.id.folder_image_view)
                    ?.run {
                        animate()
                            .rotationBy(180f)
                            .setDuration(Anim.ANIM_DURATION)
                            .withEndAction {
                                selectorFolder?.run {
                                    pickerViewModel.currentFolder.value = this
                                }
                            }
                            .start()
                    }
                false
            }

            //箭头旋转动画
            _vh.view(R.id.folder_image_view)
                ?.run { animate().rotationBy(180f).setDuration(Anim.ANIM_DURATION).start() }
        }
    }

    fun _showPreview(previewConfig: PreviewConfig) {
        //大图预览
        dslFHelper {
            anim(R.anim.lib_picker_preview_enter_anim, 0)
            show(PickerPreviewFragment().apply {
                putData(previewConfig)
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loader.destroyLoader()
    }
}