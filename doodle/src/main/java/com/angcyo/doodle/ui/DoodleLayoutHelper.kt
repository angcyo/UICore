package com.angcyo.doodle.ui

import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.angcyo.core.loadingAsyncTg
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.inputDialog
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
import com.angcyo.doodle.singlePhotoViewDialog
import com.angcyo.doodle.ui.dslitem.DoodleFunItem
import com.angcyo.doodle.ui.dslitem.DoodleIconItem
import com.angcyo.drawable.BubbleDrawable
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter._dslAdapter
import com.angcyo.http.DslHttp
import com.angcyo.http.base.getArray
import com.angcyo.http.base.getString
import com.angcyo.http.base.jsonObject
import com.angcyo.http.download.download
import com.angcyo.http.get
import com.angcyo.http.post
import com.angcyo.http.rx.doBack
import com.angcyo.http.rx.observe
import com.angcyo.item.style.itemHaveNew
import com.angcyo.item.style.itemNewHawkKeyStr
import com.angcyo.library._screenWidth
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex._string
import com.angcyo.library.ex.interceptParentTouchEvent
import com.angcyo.library.ex.save
import com.angcyo.library.ex.sleep
import com.angcyo.library.ex.syncSingle
import com.angcyo.library.ex.toBitmap
import com.angcyo.library.ex.toUri
import com.angcyo.library.ex.undefined_size
import com.angcyo.library.libCacheFile
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.fileNameUUID
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
class DoodleLayoutHelper(val dialogConfig: DoodleDialogConfig) {

    companion object {
        const val TAG_DOODLE_AI_DRAW = "doodle_ai_draw"
    }

    /**涂鸦控件*/
    var doodleView: DoodleView? = null

    /**最小和最大的宽高*/
    var minPaintWidth: Float = 5f

    var maxPaintWidth: Float = 80f

    /**item的宽度*/
    var doodleItemWidth: Int = undefined_size

    /**是否显示色盘*/
    var showDoodlePalette = false

    /**是否显示ai绘图*/
    var showAIDraw = LibHawkKeys.enableAIDraw

    var _rootViewHolder: DslViewHolder? = null

    val _doodleItemAdapter: DslAdapter?
        get() = _rootViewHolder?.rv(R.id.doodle_item_view)?._dslAdapter

    /**初始化入口*/
    @CallPoint
    fun initLayout(viewHolder: DslViewHolder) {
        _rootViewHolder = viewHolder
        //
        doodleView = viewHolder.v<DoodleView>(R.id.lib_doodle_view)
        //items
        viewHolder.rv(R.id.doodle_item_view)?.renderDslAdapter {
            DoodleFunItem()() {
                itemIco = R.drawable.doodle_pencil
                itemText = _string(R.string.doodle_paintbrush)
                itemMinWidth = doodleItemWidth
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
                itemText = _string(R.string.doodle_brush)
                itemMinWidth = doodleItemWidth
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
                itemText = _string(R.string.doodle_eraser)
                itemMinWidth = doodleItemWidth
                itemClick = {
                    if (!itemIsSelected) {
                        itemIsSelected = true
                        updateAdapterItem()
                        viewHolder.visible(R.id.size_wrap_layout)
                        updateBrush(EraserBrush())
                    }
                }
            }
            if (showDoodlePalette) {
                DoodleIconItem()() {
                    itemIco = R.drawable.doodle_palette
                    itemText = _string(R.string.doodle_color)
                    itemMinWidth = doodleItemWidth
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
            if (showAIDraw) {
                val uploadFileAction = DslHttp.uploadFileAction
                if (uploadFileAction != null) {
                    DoodleIconItem()() {
                        itemTag = TAG_DOODLE_AI_DRAW
                        itemIco = R.drawable.doodle_ai_draw
                        itemText = _string(R.string.doodle_ai_draw)
                        itemNewHawkKeyStr = TAG_DOODLE_AI_DRAW
                        itemEnable = false
                        itemClick = {
                            itemHaveNew = false
                            it.context.inputDialog {
                                dialogTitle = "Describe image"
                                hintInputString = "Describe the image you want to create..."
                                maxInputLength = 1000
                                canInputEmpty = false
                                defaultInputString = _lastPrompt
                                onInputResult = { dialog, inputText ->
                                    startAiDraw("$inputText") {
                                        affirmAiDraw(it)
                                    }
                                    false
                                }
                            }
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
            itemText = _string(R.string.doodle_undo)
            itemEnable = false
            itemClick = {
                doodleView?.doodleDelegate?.undoManager?.undo()
            }
        })
        add(DoodleIconItem().apply {
            itemIco = R.drawable.doodle_redo
            itemText = _string(R.string.doodle_redo)
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

    fun _progress(value: Float): Float {
        return (value - minPaintWidth) / (maxPaintWidth - minPaintWidth) * 100
    }

    fun _value(progress: Float): Float {
        //
        return minPaintWidth + (maxPaintWidth - minPaintWidth) * progress / 100
    }

    var window: TargetWindow? = null
    var popupTipConfig: PopupTipConfig? = null

    fun showBubblePopupTip(view: View, event: MotionEvent) {
        view.interceptParentTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                window = view.context.popupTipWindow(view, R.layout.lib_bubble_tip_layout) {
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

    //---

    private var _lastPrompt: CharSequence? = null

    /**[prompt] ai绘图的图片描述文本
     * [action] 请求成功后, 图片本地路径*/
    private fun startAiDraw(prompt: String, action: (String?) -> Unit = {}) {
        _lastPrompt = prompt
        val doodleView = doodleView ?: return
        val doodleDelegate = doodleView.doodleDelegate

        dialogConfig.loadingAsyncTg({
            var result: String? = null
            syncSingle { countDownLatch ->
                val originBitmap = doodleDelegate.getPreviewBitmap()!!
                val cacheFile = libCacheFile(fileNameUUID(".png"))
                originBitmap.save(cacheFile.absolutePath)

                //开始上传图片
                DslHttp.uploadFileAction?.invoke(cacheFile.absolutePath) { bitmapUrl, error ->
                    if (error == null && !bitmapUrl.isNullOrEmpty()) {
                        //图片上传成功
                        cacheFile.delete()//删除缓存文件
                        post {
                            url = "https://scribblediffusion.com/api/predictions"
                            body = jsonObject {
                                add("prompt", prompt)
                                add("image", bitmapUrl)
                                /*add(
                                    "image",
                                    "https://upcdn.io/FW25b4F/raw/uploads/scribble-diffusion/1.0.0/2023-04-22/scribble_input_XhANmnDt.png"
                                )*/
                                add("structure", "scribble")
                            }
                            isSuccessful = {
                                it.isSuccessful
                            }
                        }.observe { data, error2 ->
                            val bitmapId = data?.body()?.getString("id")
                            if (!bitmapId.isNullOrEmpty()) {
                                //请求成功, 然后通过id获取图片地址
                                getBitmapUrl(bitmapId) {
                                    result = it
                                    if (it == null) {
                                        toastQQ("error please retry!")
                                    }
                                    countDownLatch.countDown()
                                }
                            } else {
                                //请求失败失败
                                error2?.let {
                                    toastQQ(it.message)
                                }
                                countDownLatch.countDown()
                            }
                        }
                    } else {
                        error?.let {
                            toastQQ(it.message)
                        }
                        countDownLatch.countDown()
                    }
                }
            }
            result
        })
        { bitmapPath ->
            action(bitmapPath)
        }
    }

    private fun getBitmapUrl(
        bitmapId: String,
        retryCount: Int = 0,/*重试次数*/
        action: (String?) -> Unit
    ) {
        if (retryCount >= 30) {
            action(null)
            return
        }
        get {
            url = "https://scribblediffusion.com/api/predictions/$bitmapId"
            isSuccessful = {
                it.isSuccessful
            }
        }.observe { data, error ->
            val outputList = data?.body()?.getArray("output")
            if (outputList != null && !outputList.isEmpty) {
                //请求成功, 然后通过id获取图片地址
                val bitmapUrl = outputList[0].asString

                //开始下载图片
                bitmapUrl.download(fileNameUUID(".png")) { task, error2 ->
                    if (error2 == null) {
                        //下载成功
                        if (task.isFinish) {
                            val bitmapPath = task.savePath
                            action(bitmapPath)
                        }
                    } else {
                        toastQQ(error2.message)
                    }
                }
            } else {
                //失败后, 重试
                doBack {
                    sleep(1000)
                    getBitmapUrl(bitmapId, retryCount + 1, action)
                }
            }
        }
    }

    /**确认是否要使用ai生成的图片*/
    private fun affirmAiDraw(bitmapPath: String?) {
        bitmapPath ?: return
        _rootViewHolder?.context?.singlePhotoViewDialog(bitmapPath.toUri()) {
            positiveButton { dialog, dialogViewHolder ->
                dialog.dismiss()
                dialogConfig.onDoodleResultAction(bitmapPath.toBitmap()!!)
                dialogConfig._dialog?.dismiss()
            }
        }
    }
}