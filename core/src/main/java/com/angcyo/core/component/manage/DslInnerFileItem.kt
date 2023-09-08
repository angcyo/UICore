package com.angcyo.core.component.manage

import com.angcyo.core.R
import com.angcyo.core.dslitem.DslFileSelectorItem
import com.angcyo.dialog.itemsDialog
import com.angcyo.dialog.messageDialog
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.allSelectedItem
import com.angcyo.dsladapter.updateItemSelected
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex._string
import com.angcyo.library.ex.size
import com.angcyo.widget.DslViewHolder
import java.io.File

/**
 * 内部文件管理界面的[DslAdapterItem]模型
 *
 * [com.angcyo.core.dslitem.DslFileSelectorItem]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/07
 */
class DslInnerFileItem : DslAdapterItem() {

    /**对应的文件对象*/
    val itemFile: File?
        get() = itemData as? File

    /**最多允许选择多少个文件*/
    var itemMaxSelectCount = 0
        set(value) {
            field = value
            updateSelectModel()
        }

    /**是否是选择模式*/
    private val _isSelectModel: Boolean
        get() = itemMaxSelectCount > 0

    init {
        itemLayoutId = R.layout.item_inner_file

        itemLongClick = {
            if (itemFile != null) {
                val item = this
                it.context.itemsDialog {
                    addDialogItem {
                        itemText = _string(R.string.ui_delete)
                        itemClick = {
                            lastContext.messageDialog {
                                dialogTitle = _string(R.string.ui_warn)
                                dialogMessage = _string(R.string.ui_delete_tip)
                                needPositiveButton { dialog, _ ->
                                    dialog.dismiss()
                                    itemFile?.delete()
                                    item.updateItemSelected(false, false)
                                    item.itemChanging = true
                                    item.removeAdapterItemJust()
                                }
                            }
                        }
                    }
                }
            }
            true
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.visible(R.id.lib_choose_view, _isSelectModel)

        val fileName = itemFile?.name
        itemHolder.tv(R.id.lib_text_view)?.text = fileName
        itemHolder.img(R.id.lib_image_view)?.apply {
            setImageResource(DslFileSelectorItem.getFileIconRes(fileName))
        }

        itemHolder.selected(R.id.lib_choose_view, itemIsSelected)
    }

    /**选择模式下的逻辑*/
    fun updateSelectModel() {
        if (_isSelectModel) {
            itemClick = {
                val selectIntent = !itemIsSelected

                if (itemMaxSelectCount == 1) {
                    //单选模式
                    if (selectIntent) {
                        //取消其他选中
                        itemDslAdapter?.allSelectedItem()?.forEach {
                            it.updateItemSelected(false)
                        }
                        updateItemSelected(true)
                        itemChanging = true
                    } else {
                        //取消选中, 不允许
                    }
                } else {
                    //多选模式
                    val selectedList = itemDslAdapter?.allSelectedItem()
                    val size = selectedList.size()
                    if (size >= itemMaxSelectCount && selectIntent) {
                        //超过最大选择数量, 取消多余元素的选中状态
                        selectedList?.subList(0, size - itemMaxSelectCount + 1)?.forEach {
                            it.updateItemSelected(false)
                        }
                        updateItemSelected(true)
                        itemChanging = true
                    } else {
                        updateItemSelected(selectIntent)
                        itemChanging = true
                    }
                }
            }
        } else {
            itemClick = null
        }
    }

}