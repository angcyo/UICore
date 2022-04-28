package com.angcyo.library.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import com.angcyo.library.ex.*
import kotlinx.parcelize.Parcelize

/**
 * Loader 加载出来的媒体数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

@Parcelize
data class LoaderMedia(

    //数据库中的id
    var id: String? = null,

    //网络路径
    var url: String? = null,
    //本地路径, 优先使用uri加载
    var localPath: String? = null,
    //压缩后的路径
    var compressPath: String? = null,
    //剪切后的路径
    var cropPath: String? = null,
    //视频 音频媒体时长, 毫秒
    var duration: Long = -1,

    //测试属性(暂无用处)
    var loaderUri: Uri? = null,

    //Android Q文件存储机制修改成了沙盒模式, 不能直接通过路径的方式访问文件, 优先uri加载
    var localUri: Uri? = null,

    //直接加载的内存图片
    var bitmap: Bitmap? = null,

    //数据库字段↓

    //angcyo
    var width: Int = -1,
    var height: Int = -1,

    /** 1558921509 秒 */
    var modifyTime: Long = -1,

    /** 1558921509 秒 */
    var addTime: Long = -1,

    /** 文件大小, b->kb */
    var fileSize: Long = -1,

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

) : Parcelable {

    override fun hashCode(): Int {
        return loadUri()?.hashCode() ?: super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        return if (other is LoaderMedia) {
            (this.compressPath?.equals(other.compressPath) == true ||
                    this.cropPath?.equals(other.cropPath) == true ||
                    this.localPath?.equals(other.localPath) == true ||
                    this.url?.equals(other.url) == true ||
                    this.localUri?.equals(other.localUri) == true
                    )
        } else {
            false
        }
    }
}

//媒体类型
fun LoaderMedia.mimeType(): String {
    return mimeType ?: (loadPath()?.mimeType() ?: "image/*")
}

fun LoaderMedia?.isVideo(): Boolean {
    return this?.mimeType().isVideoMimeType()
}

fun LoaderMedia?.isAudio(): Boolean {
    return this?.mimeType().isAudioMimeType()
}

fun LoaderMedia?.isImage(): Boolean {
    return this?.mimeType().isImageMimeType()
}

/**加载路径*/
fun LoaderMedia.loadPath(): String? {
    //压缩后的路径
    if (compressPath?.isFileExist() == true) {
        return compressPath
    }
    //剪裁后的路径, 剪裁后再压缩
    if (cropPath?.isFileExist() == true) {
        return cropPath
    }
    //本地原始路径
    if (localPath?.isFileExist() == true) {
        return localPath
    }
    return localUri?.loadUrl() ?: url //网络路径
}

/**加载的[Uri]*/
fun LoaderMedia.loadUri(): Uri? {
    if (compressPath?.isFileExist() == true) {
        return Uri.fromFile(compressPath!!.file())
    }
    if (cropPath?.isFileExist() == true) {
        return Uri.fromFile(cropPath!!.file())
    }
    if (localUri != null) {
        return localUri
    }
    if (localPath?.isFileExist() == true) {
        return Uri.fromFile(localPath!!.file())
    }
    return url?.run { Uri.parse(url) }
}

fun List<String>.toLoaderMediaList(): List<LoaderMedia> {
    val result = mutableListOf<LoaderMedia>()
    forEach {
        result.add(LoaderMedia(url = it))
    }
    return result
}

fun List<LoaderMedia>.toUrlList(): List<String> {
    val result = mutableListOf<String>()
    forEach {
        it.loadPath()?.let { result.add(it) }
    }
    return result
}

fun String.toLoaderMedia(): LoaderMedia = LoaderMedia(url = this)