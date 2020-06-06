package com.angcyo.media.audio.record

import android.Manifest
import android.app.Activity
import android.view.MotionEvent
import android.view.View
import com.angcyo.library.ex.checkPermissions
import com.angcyo.library.ex.ext
import com.angcyo.library.ex.noExtName
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.fileName
import com.angcyo.library.utils.folderPath
import com.angcyo.widget.base.hideSoftInput
import java.io.File
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/18
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RecordControl {

    companion object {
        /**默认存储的文件夹名称*/
        const val FOLDER_NAME = "record"
    }

    var recordUI = RecordUI()
    var record: RRecord? = null

    /**touch down时, 是否自动隐藏软键盘*/
    var autoHideSoft = false

    /**
     * 监听那个view的事件, 触发录制
     * */
    open fun wrap(
        view: View,
        activity: Activity,
        onRecordStart: () -> Boolean = { true } /*返回true, 表示可以开始录制*/,
        onRecordEnd: (voiceFile: File) -> Unit = {}
    ) {
        if (record == null) {
            val folder = folderPath(FOLDER_NAME)
            record = RRecord(activity, folder)
        }

        fun onEnd(isCancel: Boolean) {
            view.isSelected = false
            recordUI.hide()
            record?.stopRecord()

            if (!isCancel) {
                record?.sampleFile?.let {
                    onRecordEnd(it)
                }
            }
        }

        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (autoHideSoft) {
                        //隐藏软键盘
                        v.hideSoftInput()
                    }
                    if (onRecordStart()) {
                        if (activity.checkPermissions(Manifest.permission.RECORD_AUDIO)) {
                            view.isSelected = true
                            recordUI.onGetMeterCount = {
                                var result = 1

                                record?.let {
                                    result = 7 * it.maxAmplitude / 32768
                                }

                                max(1, result)
                            }
                            recordUI.show(activity, v, event.rawY)
                            record?.stopPlayback()
                            record?.startRecord(fileName())
                        }
                    } else {
                        view.isSelected = false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (view.isSelected) {
                        if (recordUI.isCancel) {
                            onEnd(true)
                        } else if (recordUI.isMinRecordTime) {
                            toastQQ("至少需要录制 " + recordUI.minRecordTime + " 秒")
                            onEnd(true)
                        } else {
                            onEnd(recordUI.isCancel)
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (view.isSelected) {
                        recordUI.checkCancel(event)
                    }
                }
            }
            //L.i("Touch:${event.actionMasked} x:${event.x}  y:${event.y} rawY:${event.rawY}")
            true
        }

        recordUI.onMaxRecordTimeAction = Runnable {
            onEnd(false)
        }
    }

    open fun release() {
        recordUI.hide()
        record?.release()
    }

    /**用录制时间重命名文件*/
    open fun rename(sampleFile: File): File {
        val recordTime = recordUI.currentRecordTime
        val time = (recordTime / 1000).toInt()
        val recordFile = File(
            sampleFile.parent,
            "${sampleFile.name.noExtName()}_t_${time}.${sampleFile.name.ext()}"
        )
        sampleFile.renameTo(recordFile)
        return recordFile
    }
}