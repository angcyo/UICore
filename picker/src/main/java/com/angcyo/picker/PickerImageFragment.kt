package com.angcyo.picker

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.angcyo.base.back
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dialog.fullPopupWindow
import com.angcyo.dsladapter.DslAdapterStatusItem
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*观察文件夹切换*/
        pickerViewModel.currentFolder.observe {
            _switchFolder(it)
        }

        //样式调整
        _adapter.dslAdapterStatusItem = DslPickerStatusItem()
        _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)

        //加载配置
        val loaderConfig = pickerViewModel.loaderConfig.value
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
    }

    /**切换显示的文件夹*/
    fun _switchFolder(folder: LoaderFolder) {
        _vh.visible(R.id.folder_layout)
        _vh.tv(R.id.folder_text_view)?.text = folder.folderName

        _adapter.loadSingleData(folder.mediaItemList, 1, Int.MAX_VALUE) { oldItem, _ ->
            oldItem ?: DslPickerImageItem()
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