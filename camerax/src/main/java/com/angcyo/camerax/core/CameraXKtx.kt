package com.angcyo.camerax.core

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.angcyo.camerax.utils.YuvToRgbConverter
import com.angcyo.library.L
import java.nio.ByteBuffer

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/18
 */

fun Int.toImageFormatStr() = when (this) {
    ImageFormat.DEPTH_JPEG -> "DEPTH_JPEG"
    ImageFormat.DEPTH_POINT_CLOUD -> "DEPTH_POINT_CLOUD"
    ImageFormat.FLEX_RGBA_8888 -> "FLEX_RGBA_8888"
    ImageFormat.FLEX_RGB_888 -> "FLEX_RGB_888"
    ImageFormat.JPEG -> "JPEG"
    ImageFormat.NV16 -> "NV16"
    ImageFormat.NV21 -> "NV21"
    ImageFormat.PRIVATE -> "PRIVATE"
    ImageFormat.RAW10 -> "RAW10"
    ImageFormat.RAW12 -> "RAW12"
    ImageFormat.RAW_PRIVATE -> "RAW_PRIVATE"
    ImageFormat.RAW_SENSOR -> "RAW_SENSOR"
    ImageFormat.RGB_565 -> "RGB_565"
    ImageFormat.UNKNOWN -> "UNKNOWN"
    ImageFormat.YUV_420_888 -> "YUV_420_888" //默认
    ImageFormat.YUV_422_888 -> "YUV_422_888"
    ImageFormat.YUV_444_888 -> "YUV_444_888"
    ImageFormat.YUY2 -> "YUY2"
    ImageFormat.YV12 -> "YV12"
    else -> "未知"
}

/**CameraXBasic*/
fun ByteBuffer.toByteArray(): ByteArray {
    rewind()    // Rewind the buffer to zero
    val data = ByteArray(remaining())
    get(data)   // Copy the buffer into a byte array
    return data // Return the byte array
}


/**拍出来的照片, 需要进行的矩阵变换
 * 默认拍出来的照片都是带旋转的.
 * 前摄拍出来的照片水平是翻转的*/
fun ImageProxy.getBitmapTransform(cameraSelector: CameraSelector?): Matrix? {
    val cameraSelector = cameraSelector ?: return null
    val degrees = imageInfo.rotationDegrees.toFloat()
    return when (cameraSelector) {
        CameraSelector.DEFAULT_BACK_CAMERA -> Matrix().apply {
            postRotate(degrees)
        }

        CameraSelector.DEFAULT_FRONT_CAMERA -> Matrix().apply {
            postRotate(degrees)
            postScale(-1f, 1f)
        }

        else -> {
            null
        }
    }
}

/**
 * 耗时:88 183 165 165 191 179 232 157 175 163 ms
 * [shouldCropImage] 是否需要裁剪图片
 * [jpegQuality] 图片质量
 * 要考虑 [androidx.camera.core.ImageProxy.getCropRect], 否则会有形变
 * */
@SuppressLint("RestrictedApi")
fun ImageProxy.toBitmap(
    shouldCropImage: Boolean = !cropRect.isEmpty,
    jpegQuality: Int = 100
): Bitmap? {
    val image = this
    //image.image.transformMatrix

    //val shouldCropImage = !image.cropRect.isEmpty && ImageUtil.shouldCropImage(image)
    val imageFormat = image.format
    val bytes = if (imageFormat == ImageFormat.JPEG) {
        if (!shouldCropImage) {
            // When cropping is unnecessary, the byte array doesn't need to be decoded and
            // re-encoded again. Therefore, jpegQuality is unnecessary in this case.
            ImageUtil.jpegImageToJpegByteArray(image)
        } else {
            ImageUtil.jpegImageToJpegByteArray(image, image.cropRect, jpegQuality)
        }
    } else if (imageFormat == ImageFormat.YUV_420_888) {
        ImageUtil.yuvImageToJpegByteArray(
            image,
            if (shouldCropImage) image.cropRect else null,
            jpegQuality
        )
    } else {
        L.w("angcyo", "Unrecognized image format: $imageFormat")
        return null
    }

    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

/**这种方法, 耗时: 72 85 88 93 104 185 141 125ms
 * 要考虑 [androidx.camera.core.ImageProxy.getCropRect], 否则会有形变
 * */
@SuppressLint("UnsafeOptInUsageError")
fun ImageProxy.toBitmapConverter(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    YuvToRgbConverter().yuvToRgb(image!!, bitmap)
    return bitmap
}

