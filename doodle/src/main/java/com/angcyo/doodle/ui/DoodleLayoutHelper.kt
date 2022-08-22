package com.angcyo.doodle.ui

import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.popup.PopupTipConfig
import com.angcyo.dialog.popup.popupTipWindow
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
import com.angcyo.drawable.BubbleDrawable
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library._screenWidth
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.interceptParentTouchEvent
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.resetDslItem
import com.angcyo.widget.progress.DslProgressBar
import com.angcyo.widget.progress.DslSeekBar
import com.angcyo.widget.recycler.renderDslAdapter

/**
 * 涂鸦的界面操作
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
class DoodleLayoutHelper {

    /**涂鸦控件*/
    var doodleView: DoodleView? = null

    /**最小和最大的宽高*/
    var minPaintWidth: Float = 5f

    var maxPaintWidth: Float = 80f

    /**初始化入口*/
    @CallPoint
    fun initLayout(viewHolder: DslViewHolder) {
        //
        doodleView = viewHolder.v<DoodleView>(R.id.lib_doodle_view)
        //items
        viewHolder.rv(R.id.doodle_item_view)?.renderDslAdapter {
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
                //默认使用钢笔
                itemIsSelected = true
                updateBrush(PenBrush())
            }
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
                        onDismissListener = {
                            itemIsSelected = false
                            updateAdapterItem()
                        }
                        colorPickerResultAction = { dialog, color ->
                            doodleConfig?.paintColor = color
                            false
                        }
                    }
                }
            }
        }

        //property
        viewHolder.v<DslSeekBar>(R.id.size_seek_bar)?.apply {
            config {
                onSeekChanged = { value, fraction, fromUser ->
                    doodleView?.doodleDelegate?.doodleConfig?.paintWidth = _value(value)
                }
            }
            val width = doodleView?.doodleDelegate?.doodleConfig?.paintWidth ?: 20f
            setProgress(_progress(width), animDuration = -1)
        }
        viewHolder.touch(R.id.size_seek_bar) { view, event ->
            showBubblePopupTip(view, event)
            true
        }

        //undo redo
        _updateUndoLayout(viewHolder)
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

    fun _progress(value: Float): Int {
        return ((value - minPaintWidth) / (maxPaintWidth - minPaintWidth) * 100).toInt()
    }

    fun _value(progress: Int): Float {
        //
        return minPaintWidth + (maxPaintWidth - minPaintWidth) * progress / 100
    }

    var window: TargetWindow? = null
    var popupTipConfig: PopupTipConfig? = null

    fun showBubblePopupTip(view: View, event: MotionEvent) {
        view.interceptParentTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                window = view.context.popupTipWindow(view, R.layout.lib_doodle_bubble_tip_layout) {
                    touchX = event.x
                    popupTipConfig = this
                    onInitLayout = { window, viewHolder ->
                        viewHolder.view(R.id.lib_bubble_view)?.background = BubbleDrawable()
                        viewHolder.tv(R.id.lib_text_view)?.text = if (view is DslProgressBar) {
                            "${_value(view.progressValue).toInt()}"
                        } else {
                            "${(touchX * 1f / _screenWidth * 100).toInt()}"
                        }
                    }
                    if (view is DslSeekBar) {
                        limitTouchRect = view._progressBound
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                popupTipConfig?.apply {
                    touchX = event.x
                    updatePopup()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //window?.dismiss()
                popupTipConfig?.hide()
            }
        }
    }

}