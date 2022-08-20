package com.angcyo.doodle.ui

import com.angcyo.doodle.R
import com.angcyo.doodle.ui.dslitem.DoodleFunItem
import com.angcyo.doodle.ui.dslitem.DoodleIconItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.annotation.CallPoint
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.resetDslItem
import com.angcyo.widget.recycler.renderDslAdapter

/**
 * 涂鸦的界面操作
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
class DoodleLayoutHelper {

    /**初始化入口*/
    @CallPoint
    fun initUI(viewHolder: DslViewHolder) {
        //items
        viewHolder.rv(R.id.doodle_item_view)?.renderDslAdapter {
            DoodleFunItem()() {
                itemIco = R.drawable.doodle_brush
                itemText = "毛笔"
                itemClick = {
                    itemIsSelected = true
                    updateAdapterItem()
                }
            }
            DoodleFunItem()() {
                itemIco = R.drawable.doodle_pencil
                itemText = "钢笔"
                itemClick = {
                    itemIsSelected = true
                    updateAdapterItem()
                }
            }
            DoodleFunItem()() {
                itemIco = R.drawable.doodle_eraser
                itemText = "橡皮擦"
                itemClick = {
                    itemIsSelected = true
                    updateAdapterItem()
                }
            }
            DoodleIconItem()() {
                itemIco = R.drawable.doodle_palette
                itemText = "颜色"
                itemClick = {
                    itemIsSelected = true
                    updateAdapterItem()
                }
            }
        }

        //property
        _updateUndoLayout(viewHolder)
    }

    val undoItemList = mutableListOf<DslAdapterItem>().apply {
        add(DoodleIconItem().apply {
            itemIco = R.drawable.doodle_undo
            itemText = "撤销"
            itemClick = {
                itemIsSelected = true
            }
        })
        add(DoodleIconItem().apply {
            itemIco = R.drawable.doodle_redo
            itemText = "重做"
            itemClick = {
                itemIsSelected = true
            }
        })
    }

    /**undo redo*/
    fun _updateUndoLayout(viewHolder: DslViewHolder) {
        viewHolder.group(R.id.undo_wrap_layout)?.resetDslItem(undoItemList)
    }

}