package com.angcyo.picker.core

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.base.dslFHelper
import com.angcyo.dialog.fullPopupWindow
import com.angcyo.dsladapter.*
import com.angcyo.library.L
import com.angcyo.loader.DslLoader
import com.angcyo.loader.LoaderFolder
import com.angcyo.picker.R
import com.angcyo.picker.dslitem.DslPickerFolderItem
import com.angcyo.picker.dslitem.DslPickerImageItem
import com.angcyo.putData
import com.angcyo.widget._rv
import com.angcyo.widget.base.Anim
import com.angcyo.widget.recycler.initDslAdapter
import com.angcyo.widget.recycler.localUpdateItem

/**
 * 媒体选择列表界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class PickerImageFragment : BasePickerFragment() {
    val loader = DslLoader()

    init {
        fragmentLayoutId = R.layout.picker_image_fragment
    }

    override fun onInitDslLayout(recyclerView: RecyclerView, dslAdapter: DslAdapter) {
        super.onInitDslLayout(recyclerView, dslAdapter)
        dslAdapter.multiModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //加载配置
        val loaderConfig = pickerViewModel.loaderConfig.value

        /*观察文件夹切换*/
        pickerViewModel.currentFolder.observe {
            _switchFolder(it)
        }

        loaderConfig?.apply {
            loader.onLoaderResult = {
                if (it.isEmpty()) {
                    _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
                } else {
                    _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
                    pickerViewModel.loaderFolderList.value = it
                    pickerViewModel.currentFolder.value =
                        pickerViewModel.currentFolder.value ?: it.first()
                }
            }
            loader.startLoader(activity, loaderConfig)
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
        _recyclerView.localUpdateItem(-1, listOf(DslAdapterItem.PAYLOAD_UPDATE_PART))
    }

    /**切换显示的文件夹*/
    fun _switchFolder(folder: LoaderFolder?) {
        if (folder == null) {
            return
        }
        _vh.visible(R.id.folder_layout)
        _vh.tv(R.id.folder_text_view)?.text = folder.folderName

        _adapter.loadSingleData<DslPickerImageItem>(
            folder.mediaItemList,
            1,
            Int.MAX_VALUE
        ) { oldItem, data ->
            (oldItem ?: DslPickerImageItem().apply {
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
                //点击事件
                onItemClick = {
                    //大图预览
                    val startPosition =
                        pickerViewModel.currentFolder.value?.mediaItemList?.indexOf(loaderMedia)
                            ?: 0
                    _showPreview(PreviewConfig(false, startPosition))
                }
            }).apply {
                //选中状态
                itemIsSelected = pickerViewModel.selectorMediaList.value?.contains(data) ?: false
                (this as? DslPickerImageItem)?.showFileSize =
                    pickerViewModel.loaderConfig.value?.showFileSize ?: false
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
                            async = false
                        }
                        pickerViewModel.loaderFolderList.value?.forEachIndexed { index, folder ->
                            DslPickerFolderItem()() {
                                itemData = folder
                                itemIsSelected = folder == pickerViewModel.currentFolder.value
                                onItemClick = {
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
            enterAnimRes = R.anim.lib_picker_preview_enter_anim
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