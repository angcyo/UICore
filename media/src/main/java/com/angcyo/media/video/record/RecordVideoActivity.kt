package com.angcyo.media.video.record

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.angcyo.base.dslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.base.translucentStatusBar
import com.angcyo.core.activity.BasePermissionsActivity
import com.angcyo.media.video.record.inner.RecordVideoCallback
import java.io.File

/**
 * 使用[MediaRecorder]实现的视频录制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RecordVideoActivity : BasePermissionsActivity() {

    companion object {
        const val REQUEST_CODE = 200
        const val KEY_DATA_PATH = "KEY_DATA_PATH"
        const val KEY_DATA_TYPE = "KEY_DATA_TYPE"
        const val KEY_MAX_TIME = "KEY_MAX_TIME"
        const val KEY_MIN_TIME = "KEY_MIN_TIME"

        var recordVideoCallback: RecordVideoCallback? = null

        /**限制录制界面*/
        fun show(activity: Activity, maxRecordTime: Int = 15, minRecordTime: Int = 3) {
            activity.dslAHelper {
                start(RecordVideoActivity::class.java) {
                    intent.putExtra(KEY_MIN_TIME, minRecordTime)
                    intent.putExtra(KEY_MAX_TIME, maxRecordTime)
                    resultCode = REQUEST_CODE
                }
            }
        }

        /**获取录制的媒体路径*/
        fun getResultPath(requestCode: Int, resultCode: Int, data: Intent?): String? {
            return if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                data?.getStringExtra(KEY_DATA_PATH)
            } else {
                null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translucentStatusBar(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        recordVideoCallback = null
    }

    override fun onPermissionGranted() {
        super.onPermissionGranted()

        dslFHelper {
            show(RecordVideoFragment::class.java) {
                (this as? RecordVideoFragment)?.callback = object : RecordVideoCallback() {

                    init {
                        intent.extras?.apply {
                            minRecordTime = getInt(KEY_MIN_TIME, minRecordTime)
                            maxRecordTime = getInt(KEY_MAX_TIME, maxRecordTime)
                        }
                    }

                    override fun onTakePhotoBefore(photo: Bitmap, width: Int, height: Int): Bitmap {
                        return recordVideoCallback?.onTakePhotoBefore(photo, width, height)
                            ?: super.onTakePhotoBefore(photo, width, height)
                    }

                    override fun onTakePhotoAfter(photo: Bitmap, width: Int, height: Int): Bitmap {
                        return recordVideoCallback?.onTakePhotoAfter(photo, width, height)
                            ?: super.onTakePhotoAfter(photo, width, height)
                    }

                    override fun onTakePhoto(bitmap: Bitmap, outputFile: File) {
                        super.onTakePhoto(bitmap, outputFile)
                        result(outputFile.absolutePath, "image/png")
                    }

                    override fun onTakeVideo(videoPath: String) {
                        super.onTakeVideo(videoPath)
                        result(videoPath, "video/mp4")
                    }

                    private fun result(path: String, mimeType: String) {
                        this@RecordVideoActivity.dslAHelper {
                            finish {
                                resultCode = RESULT_OK
                                resultData = Intent().apply {
                                    putExtra(KEY_DATA_PATH, path)
                                    putExtra(KEY_DATA_TYPE, mimeType)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
