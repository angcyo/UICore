package com.angcyo.camerax

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.angcyo.DslAHelper
import com.angcyo.base.dslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.base.translucentStatusBar
import com.angcyo.core.activity.BasePermissionsActivity
import com.angcyo.media.video.record.RecordVideoActivity.Companion.KEY_DATA_PATH
import com.angcyo.media.video.record.RecordVideoActivity.Companion.KEY_DATA_TYPE
import com.angcyo.media.video.record.RecordVideoActivity.Companion.KEY_MAX_TIME
import com.angcyo.media.video.record.RecordVideoActivity.Companion.KEY_MIN_TIME
import com.angcyo.media.video.record.RecordVideoActivity.Companion.REQUEST_CODE
import com.angcyo.media.video.record.RecordVideoActivity.Companion.getResultPath
import com.angcyo.media.video.record.RecordVideoActivity.Companion.recordVideoCallback
import com.angcyo.media.video.record.inner.RecordVideoCallback
import java.io.File

/**
 * 使用[CameraX]实现的视频录制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-4-23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class CameraXRecordVideoActivity : BasePermissionsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translucentStatusBar(true)
    }

    override fun onPermissionGranted(savedInstanceState: Bundle?) {
        super.onPermissionGranted(savedInstanceState)

        dslFHelper {
            show(CameraXRecordVideoFragment::class.java) {
                (this as? CameraXRecordVideoFragment)?.callback = object : RecordVideoCallback() {

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
                        recordVideoCallback?.onTakeVideo(videoPath) ?: super.onTakeVideo(videoPath)
                        result(videoPath, "video/mp4")
                    }

                    private fun result(path: String, mimeType: String) {
                        this@CameraXRecordVideoActivity.dslAHelper {
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

/**快速启动录制视频界面, 并拿到返回数据*/
fun DslAHelper.recordVideoCameraX(
    maxRecordTime: Int = 15,
    minRecordTime: Int = 3,
    result: (String?) -> Unit
) {
    start(CameraXRecordVideoActivity::class.java) {
        intent.putExtra(KEY_MIN_TIME, minRecordTime)
        intent.putExtra(KEY_MAX_TIME, maxRecordTime)
        requestCode = REQUEST_CODE
        onActivityResult = { resultCode, data ->
            result(getResultPath(requestCode, resultCode, data))
        }
    }
}