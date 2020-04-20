package com.angcyo.media.video.record.inner

import android.graphics.Bitmap
import java.io.File

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/01
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RecordVideoCallback {

    companion object {
        const val TAKE_MODEL_PHOTO = 0x1
        const val TAKE_MODEL_VIDEO = 0x2
    }

    /**
     * 允许最大录制时长 (秒)
     */
    var maxRecordTime = 10

    /**
     * 最小录制时长 (秒)
     */
    var minRecordTime = 3

    /**take模型*/
    var takeModel: Int = TAKE_MODEL_PHOTO or TAKE_MODEL_VIDEO

    var modelPhotoText: String = "轻触拍照"
    var modelVideoText: String = "长按摄像"
    var modelPhotoVideoText: String = "轻触拍照, 长按摄像"

    open fun initConfig() {}

    /**
     * 拍照回调
     */
    open fun onTakePhoto(bitmap: Bitmap, outputFile: File) {}

    /**
     * 录像回调
     */
    open fun onTakeVideo(videoPath: String) {}

    /**录像结束之后, 可以用来修改视频*/
    open fun onTakeVideoAfter(videoPath: String): String {
        return videoPath
    }

    open fun onTakePhotoBefore(photo: Bitmap, width: Int, height: Int): Bitmap {
        return photo
    }

    open fun onTakePhotoAfter(photo: Bitmap, width: Int, height: Int): Bitmap {
        return photo
    }
}