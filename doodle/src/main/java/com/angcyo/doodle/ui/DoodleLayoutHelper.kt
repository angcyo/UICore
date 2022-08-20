package com.angcyo.doodle.ui

import android.graphics.Color
import com.angcyo.dialog.singleColorPickerDialog
import com.angcyo.doodle.DoodleView
import com.angcyo.doodle.R
import com.angcyo.doodle.brush.EraserBrush
import com.angcyo.doodle.brush.PenBrush
import com.angcyo.doodle.brush.ZenCircleBrush
import com.angcyo.doodle.core.DoodleUndoManager
import com.angcyo.doodle.core.IDoodleListener
import com.angcyo.doodle.core.ITouchRecognize
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

    /**涂鸦控件*/
    var doodleView: DoodleView? = null

    /**初始化入口*/
    @CallPoint
    fun initLayout(viewHolder: DslViewHolder) {
        //
        doodleView = viewHolder.v<DoodleView>(R.id.lib_doodle_view)
        //items
        viewHolder.rv(R.id.doodle_item_view)?.renderDslAdapter {
            DoodleFunItem()() {
                itemIco = R.drawable.doodle_brush
                itemText = "毛笔"
                itemClick = {
                    if (!itemIsSelected) {
                        itemIsSelected = true
                        updateAdapterItem()
                        viewHolder.visible(R.id.size_wrap_layout)
                        updateBrush(ZenCircleBrush())
                    }
                }
                //默认使用毛笔
                itemIsSelected = true
                updateBrush(ZenCircleBrush())
            }
            DoodleFunItem()() {
                itemIco = R.drawable.doodle_pencil
                itemText = "钢笔"
                itemClick = {
                    if (!itemIsSelected) {
                        itemIsSelected = true
                        updateAdapterItem()
                        viewHolder.visible(R.id.size_wrap_layout)
                        updateBrush(PenBrush())
                    }
                }
            }
            DoodleFunItem()() {
                itemIco = R.drawable.doodle_eraser
                itemText = "橡皮擦"
                itemClick = {
                    if (!itemIsSelected) {
                        itemIsSelected = true
                        updateAdapterItem()
                        viewHolder.visible(R.id.size_wrap_layout)
                        updateBrush(EraserBrush())
                    }
                }
            }
            DoodleIconItem()() {
                itemIco = R.drawable.doodle_palette
                itemText = "颜色"
                itemClick = {
                    itemIsSelected = true
                    updateAdapterItem()

                    val doodleConfig = doodleView?.doodleDelegate?.doodleConfig
                    viewHolder.context.singleColorPickerDialog {
                        initialColor = doodleConfig?.paintColor ?: Color.BLACK
                        colorPickerResultAction = { dialog, color ->
                            doodleConfig?.paintColor = color
                            itemIsSelected = false
                            updateAdapterItem()
                            false
                        }
                    }
                }
            }
        }

        //property
        _updateUndoLayout(viewHolder)

        //undo redo
        doodleView?.doodleDelegate?.doodleListenerList?.add(object : IDoodleListener {
            override fun onDoodleUndoChanged(undoManager: DoodleUndoManager) {
                undoItemList[0].itemEnable = undoManager.canUndo()
                undoItemList[1].itemEnable = undoManager.canRedo()
                _updateUndoLayout(viewHolder)
            }
        })
    }

    /**更新画笔*/
    fun updateBrush(recognize: ITouchRecognize?) {
        doodleView?.doodleDelegate?.doodleTouchManager?.updateTouchRecognize(recognize)
    }

    //undo redo
    val undoItemList = mutableListOf<DslAdapterItem>().apply {
        add(DoodleIconItem().apply {
            itemIco = R.drawable.doodle_undo
            itemText = "撤销"
            itemEnable = false
            itemClick = {
                doodleView?.doodleDelegate?.undoManager?.undo()
            }
        })
        add(DoodleIconItem().apply {
            itemIco = R.drawable.doodle_redo
            itemText = "重做"
            itemEnable = false
            itemClick = {
                doodleView?.doodleDelegate?.undoManager?.redo()
            }
        })
    }

    /**undo redo*/
    fun _updateUndoLayout(viewHolder: DslViewHolder) {
        viewHolder.group(R.id.undo_wrap_layout)?.resetDslItem(undoItemList)
    }

}