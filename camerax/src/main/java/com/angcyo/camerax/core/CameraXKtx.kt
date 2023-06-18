package com.angcyo.camerax.core

import android.graphics.ImageFormat

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