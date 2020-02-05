package com.angcyo.picker

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.base.back
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dialog.fullPopupWindow
import com.angcyo.dsladapter.*
import com.angcyo.library.L
import com.angcyo.library.ex.getColor
import com.angcyo.loader.DslLoader
import com.angcyo.loader.LoaderFolder
import com.angcyo.picker.dslitem.DslPickerFolderItem
import com.angcyo.picker.dslitem.DslPickerImageItem
import com.angcyo.picker.dslitem.DslPickerStatusItem
import com.angcyo.viewmodel.VMAProperty
import com.angcyo.widget._rv
import com.angcyo.widget.recycler.initDslAdapter
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class PickerImageFragment : BaseDslFragment() {
    val loader = DslLoader()

    val pickerViewModel: PickerViewModel by VMAProperty(PickerViewModel::class.java)

    init {
        fragmentLayoutId = R.layout.picker_image_fragment
        fragmentConfig.apply {
            fragmentBackgroundDrawable = ColorDrawable(getColor(R.color.picker_fragment_bg_color))
        }
    }

    override fun onInitDslLayout(recyclerView: RecyclerView, dslAdapter: DslAdapter) {
        super.onInitDslLayout(recyclerView, dslAdapter)
        _adapter.multiModel()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //加载配置
        val loaderConfig = pickerViewModel.loaderConfig.value

        /*观察文件夹切换*/
        pickerViewModel.currentFolder.observe {
            _switchFolder(it)
        }

        /*选中改变*/
        pickerViewModel.selectorMediaList.observe {
            if (it.isNotEmpty()) {
                _vh.enable(R.id.send_button)
                _vh.tv(R.id.send_button)?.text = span {
                    append("发送(${it.size}/${loaderConfig?.maxSelectorLimit ?: -1})")
                }
            } else {
                _vh.enable(R.id.send_button, false)
                _vh.tv(R.id.send_button)?.text = span {
                    append("发送")
                }
            }
        }

        //样式调整
        _adapter.dslAdapterStatusItem = DslPickerStatusItem()
        _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)

        loaderConfig?.apply {
            loader.onLoaderResult = {
                if (it.isEmpty()) {
                    _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
                } else {
                    _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
                    pickerViewModel.loaderFolderList.value = it
                    pickerViewModel.currentFolder.value = it.first()
                }
            }
            loader.startLoader(activity, loaderConfig)
        } ?: L.w("loaderConfig is null.")

        //事件
        _vh.click(R.id.close_image_view) {
            back()
        }
        _vh.click(R.id.folder_layout) {
            _showFolderDialog()
        }
        _vh.click(R.id.send_button) {
            //发送选择
            PickerActivity.send(this)
        }
    }

    /**切换显示的文件夹*/
    fun _switchFolder(folder: LoaderFolder) {
        _vh.visible(R.id.folder_layout)
        _vh.tv(R.id.folder_text_view)?.text = folder.folderName

        _adapter.loadSingleData(folder.mediaItemList, 1, Int.MAX_VALUE) { oldItem, data ->
            (oldItem ?: DslPickerImageItem().apply {
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
                onSelectorItem = {
                    if (it) {
                        //已经选中, 则取消选择
                        pickerViewModel.removeSelectedMedia(loaderMedia)
                    } else {
                        //未选中, 则选择
                        pickerViewModel.addSelectedMedia(loaderMedia)
                    }

                    //之前选中的列表
                    val oldSelectorList = _adapter.itemSelectorHelper.getSelectorItemList()

                    //当前item选中切换
                    _adapter.itemSelectorHelper.selector(
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
            }).apply {
                //选中状态
                itemIsSelected = pickerViewModel.selectorMediaList.value?.contains(data) ?: false
            }
        }
    }

    /**文件夹切换布局*/
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
                _vh.view(R.id.folder_image_view)
                    ?.run {
                        animate()
                            .rotationBy(180f)
                            .setDuration(300)
                            .withEndAction {
                                selectorFolder?.run {
                                    pickerViewModel.currentFolder.value = this
                                }
                            }
                            .start()
                    }
                false
            }

            _vh.view(R.id.folder_image_view)
                ?.run { animate().rotationBy(180f).setDuration(300).start() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loader.destroyLoader()
    }
}