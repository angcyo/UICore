package com.angcyo.loader

import android.os.Parcelable
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.isAudio
import com.angcyo.library.model.isVideo
import com.angcyo.library.toast
import com.angcyo.loader.LoaderConfig.Companion.LOADER_TYPE_AUDIO
import com.angcyo.loader.LoaderConfig.Companion.LOADER_TYPE_IMAGE
import com.angcyo.loader.LoaderConfig.Companion.LOADER_TYPE_VIDEO
import com.angcyo.picker.core.PickerActivity
import kotlinx.android.parcel.Parcelize

/**
 * 加载媒体的一些配置参数
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/29
 */
@Parcelize
data class LoaderConfig(
    /**支持组合[LOADER_TYPE_IMAGE] [LOADER_TYPE_VIDEO] [LOADER_TYPE_AUDIO]*/
    var mediaLoaderType: Int = LOADER_TYPE_IMAGE or LOADER_TYPE_VIDEO /*or LOADER_TYPE_AUDIO*/,

    /**是否显示原图按钮, 如果支持就打开, 否则关闭*/
    var enableOrigin: Boolean = false,

    /**最大选择数量, 当最大选择数量为1时, 自动进入单选模式*/
    var maxSelectorLimit: Int = 9,

    /**是否显示编辑按钮, 目前只支持图片编辑*/
    var enableImageEdit: Boolean = true,

    /**显示拍照按钮*/
    var enableCamera: Boolean = true,

    /**图片,解析文件的Exif信息, 耗时操作*/
    var loaderExif: Boolean = false,

    /**指定编辑输出图片的宽度/高度*/
    var outputImageWidth: Int = -1,
    var outputImageHeight: Int = -1,

    /**限制文件大小模式, 混合文件类型时 无法过滤, 请使用 SIZE_MODEL_SELECTOR */
    var limitFileSizeModel: Int = SIZE_MODEL_NONE,
    //b
    var limitFileMinSize: Long = 0L,
    //b
    var limitFileMaxSize: Long = 0L,

    /**显示文件大小*/
    var showFileSize: Boolean = false,

    /**混合选择模式下, 单独限制选择视频/音频的数量,负数不显示*/
    var maxSelectorVideoLimit: Int = -1,
    var maxSelectorAudioLimit: Int = -1,

    /**已经选中的媒体*/
    var selectorMediaList: MutableList<LoaderMedia> = mutableListOf(),

    /**[androidx.core.app.ActivityCompat.startActivityForResult]*/
    var requestCode: Int = PickerActivity.PICKER_REQUEST_CODE
) : Parcelable {
    companion object {
        /**需要加载的mime type 图片,视频,音频*/
        const val LOADER_TYPE_IMAGE = 0x001
        const val LOADER_TYPE_VIDEO = 0x002
        const val LOADER_TYPE_AUDIO = 0x004

        //不限制文件大小
        const val SIZE_MODEL_NONE = 0

        //扫描媒体的时候 加上大小限制条件.
        const val SIZE_MODEL_MEDIA = 1

        //选择媒体的时候, 判断大小是否合适
        const val SIZE_MODEL_SELECTOR = 2
    }
}

/**是否是单选模式*/
fun LoaderConfig.isSingleModel(): Boolean = maxSelectorLimit == 1

/**单选图片配置*/
fun LoaderConfig.singleImage() {
    mediaLoaderType = LOADER_TYPE_IMAGE
    maxSelectorLimit = 1
}

fun LoaderConfig.singleVideo() {
    mediaLoaderType = LOADER_TYPE_VIDEO
    maxSelectorLimit = 1
}

/**是否可以选中文件(b)*/
fun LoaderConfig.canSelectorMedia(
    selectorMediaList: List<LoaderMedia>,
    media: LoaderMedia
): Boolean {

    //最大限制
    if (selectorMediaList.size >= maxSelectorLimit) {
        toast("最多选择${maxSelectorLimit}个媒体!")
        return false
    }

    //最大视频数
    if (mediaLoaderType != LOADER_TYPE_VIDEO) {
        if (maxSelectorVideoLimit > 0) {
            //混合选择, 限制最大视频选择数量
            val videoCount = selectorMediaList.sumBy { if (it.isVideo()) 1 else 0 }

            if (videoCount >= maxSelectorVideoLimit) {
                toast("最多选择${maxSelectorVideoLimit}个视频!")
                return false
            }
        }
    }

    //最大音频数
    if (mediaLoaderType != LOADER_TYPE_AUDIO) {
        if (maxSelectorAudioLimit > 0) {
            //混合选择, 限制最大音频选择数量
            val audioCount = selectorMediaList.sumBy { if (it.isAudio()) 1 else 0 }

            if (audioCount >= maxSelectorAudioLimit) {
                toast("最多选择${maxSelectorAudioLimit}个视频!")
                return false
            }
        }
    }

    //最大文件大小限制
    //b
    val fileSize: Long = media.fileSize
    if (limitFileSizeModel == LoaderConfig.SIZE_MODEL_SELECTOR) {
        if (limitFileMinSize <= 0 && limitFileMaxSize <= 0) {
            return true
        }
        if (limitFileMinSize > 0f && limitFileMaxSize > 0f) {
            val result = fileSize in limitFileMinSize..limitFileMaxSize
            return if (result) {
                true
            } else {
                toast("文件大小需要在 ${limitFileMinSize.fileSizeString()}~${limitFileMaxSize.fileSizeString()} 之间")
                false
            }
        }
        if (limitFileMinSize > 0f) {
            val result = fileSize >= limitFileMinSize
            return if (result) {
                true
            } else {
                toast("文件大小需要大于 ${limitFileMinSize.fileSizeString()}")
                false
            }
        }

        if (limitFileMaxSize > 0f) {
            val result = fileSize <= limitFileMaxSize
            return if (result) {
                true
            } else {
                toast("文件大小需要小于 ${limitFileMaxSize.fileSizeString()}")
                false
            }
        }
    }
    return true
}