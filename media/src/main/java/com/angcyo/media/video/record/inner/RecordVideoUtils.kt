package com.angcyo.media.video.record.inner

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by zwl on 16/7/7.
 */
class RecordVideoUtils {

    companion object {

        fun getResolutionList(camera: Camera): List<Camera.Size> {
            val parameters = camera.parameters
            return parameters.supportedPreviewSizes
        }

        val isSdcardExist: Boolean get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

        fun getDurationString(durationMs: Long): String {
            return String.format(
                Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationMs),
                TimeUnit.MILLISECONDS.toSeconds(durationMs) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                durationMs
                            )
                        )
            )
        }

        @ColorInt
        fun darkenColor(@ColorInt color: Int): Int {
            var result = color
            val hsv = FloatArray(3)
            Color.colorToHSV(result, hsv)
            hsv[2] *= 0.8f // value component
            result = Color.HSVToColor(hsv)
            return result
        }

        /**
         * 获取视频第一帧图片
         *
         * @param path
         * @return
         */
        fun getVideoFirstImage(path: String?): Bitmap? {
            val media = MediaMetadataRetriever()
            media.setDataSource(path)
            return media.frameAtTime
        }

        /**
         * 解决录像时清晰度问题
         *
         *
         * 视频清晰度顺序 High 1080 720 480 cif qvga gcif 详情请查看 CamcorderProfile.java
         * 在12秒mp4格式视频大小维持在1M左右时,以下四个选择效果最佳
         *
         *
         * 不同的CamcorderProfile.QUALITY_ 代表每帧画面的清晰度,
         * 变换 profile.videoBitRate 可减少每秒钟帧数
         *
         * @param cameraId 前摄 Camera.CameraInfo.CAMERA_FACING_FRONT /后摄 Camera.CameraInfo.CAMERA_FACING_BACK
         * @return
         */
        fun getBestCamcorderProfile(cameraId: Int): CamcorderProfile {
            var profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW)
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
                //对比上面480 这个选择 动作大时马赛克!!
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P)
                profile.videoBitRate = profile.videoBitRate / 10
                return profile
            }
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                //对比下面720 这个选择 每帧不是很清晰
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P)
                profile.videoBitRate = profile.videoBitRate / 5
                return profile
            }
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_CIF)) {
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_CIF)
                return profile
            }
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA)
                return profile
            }
            return profile
        }

        /**
         * 读取图片属性：旋转的角度
         *
         * @param path 图片绝对路径
         * @return degree旋转的角度
         */
        fun readPictureDegree(path: String): Int {
            var degree = 0
            try {
                val exifInterface = ExifInterface(path)
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return degree
        }

        /**
         * 旋转图片
         *
         * @param angle
         * @param bitmap
         *
         * @return Bitmap
         */
        fun rotateImageView(angle: Int, bitmap: Bitmap): Bitmap {
            // 旋转图片 动作
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            // 创建新的图片
            return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        }
    }


    /**
     * 检测当前设备是否配置闪光灯
     *
     * @param mContext
     * @return
     */
    fun checkFlashlight(mContext: Context): Boolean {
        if (!mContext.applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        ) {
            Toast.makeText(mContext.applicationContext, "当前设备没有闪光灯", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    class ResolutionComparator : Comparator<Camera.Size> {
        override fun compare(
            lhs: Camera.Size,
            rhs: Camera.Size
        ): Int {
            return if (lhs.height != rhs.height) lhs.height - rhs.height else lhs.width - rhs.width
        }
    }
}