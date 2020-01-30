package com.angcyo.loader

import android.os.Parcelable
import com.angcyo.library.ex.isFileExist
import com.angcyo.library.ex.mimeType
import kotlinx.android.parcel.Parcelize

/**
 * Loader 加载出来的媒体数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

@Parcelize
data class LoaderMedia(

    //网络路径
    var url: String? = null,
    //本地路径
    var localPath: String? = null,
    //压缩后的路径
    var compressPath: String? = null,
    //剪切后的路径
    var cutPath: String? = null,
    //视频 音频媒体时长, 毫秒
    var duration: Long = 0,

    //数据库字段↓

    //angcyo
    var width: Int = 0,
    var height: Int = 0,

    /** 1558921509 秒 */
    var modifyTime: Long = 0,

    /** 1558921509 秒 */
    var addTime: Long = 0,

    /** 文件大小, b->kb */
    var fileSize: Long = 0,

    var displayName: String? = null,

    var mimeType: String? = null,

    /**
     * The orientation for the media item, expressed in degrees. For
     * example, 0, 90, 180, or 270 degrees.
     * [android.provider.MediaStore.MediaColumns.ORIENTATION]*/
    var orientation: Int = 0,

    //纬度
    var latitude: Double = 0.0,
    //经度
    var longitude: Double = 0.0

) : Parcelable

//媒体类型
fun LoaderMedia.mimeType(): String {
    return loadPath()?.mimeType() ?: "image/*"
}

fun LoaderMedia.isVideo(): Boolean {
    return mimeType().startsWith("video")
}

fun LoaderMedia.isAudio(): Boolean {
    return mimeType().startsWith("audio")
}

fun LoaderMedia.isImage(): Boolean {
    return mimeType().startsWith("image")
}

/**加载路径*/
fun LoaderMedia.loadPath(): String? {
    if (cutPath?.isFileExist() == true) {
        return cutPath
    }
    if (compressPath?.isFileExist() == true) {
        return compressPath
    }
    if (localPath?.isFileExist() == true) {
        return localPath
    }
    return url
}