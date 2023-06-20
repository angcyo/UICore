package com.angcyo.camerax.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
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

fun ImageProxy.toBitmap(): Bitmap? {
    val image = this
    val jpegQuality = 100

    val shouldCropImage =
        image.cropRect.width() > 0 && image.cropRect.height() > 0 && ImageUtil.shouldCropImage(image)
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
        Log.w("angcyo", "Unrecognized image format: $imageFormat")
        return null
    }

    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}