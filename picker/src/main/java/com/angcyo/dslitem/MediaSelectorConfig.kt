package com.angcyo.dslitem

import androidx.fragment.app.Fragment
import com.angcyo.base.dslAHelper
import com.angcyo.dialog.itemsDialog
import com.angcyo.library.ex.elseNull
import com.angcyo.library.ex.have
import com.angcyo.library.model.LoaderMedia
import com.angcyo.loader.LoaderConfig
import com.angcyo.media.video.record.recordPhotoOnly
import com.angcyo.media.video.record.recordVideo
import com.angcyo.media.video.record.recordVideoOnly
import com.angcyo.picker.dslPicker

/**
 * 选择媒体的配置信息, 比如拍照, 相册选择. 只选图片, 只选视频等
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class MediaSelectorConfig {

    companion object {

        /**自定义的拍照界面*/
        const val MODE_TAKE = 0x01

        /**相册选择*/
        const val MODE_PICKER = 0x02

        /**手动选择*/
        const val MODE_MANUAL = MODE_TAKE or MODE_PICKER

        const val TYPE_IMAGE = 0x01
        const val TYPE_VIDEO = 0x02
        const val TYPE_AUDIO = 0x04
    }

    /**默认相册选择*/
    var selectorMode: Int = MODE_PICKER

    /**需要选择的媒体类型*/
    var selectorType: Int = TYPE_IMAGE or TYPE_VIDEO

    /**最小和最大录制时长, 秒*/
    var minRecordTime: Int = 3

    var maxRecordTime: Int = 15

    /**[MODE_TAKE]返回*/
    var takeResult: (LoaderMedia?) -> Unit = {

    }

    /**[MODE_PICKER]返回*/
    var pickerResult: (List<LoaderMedia>?) -> Unit = {

    }

    fun configLoader(loaderConfig: LoaderConfig) {
        if (selectorMode.have(MODE_PICKER)) {
            var type = 0
            if (selectorType.have(TYPE_IMAGE)) {
                type = type or LoaderConfig.LOADER_TYPE_IMAGE
            }
            if (selectorType.have(TYPE_VIDEO)) {
                type = type or LoaderConfig.LOADER_TYPE_VIDEO
            }
            if (selectorType.have(TYPE_AUDIO)) {
                type = type or LoaderConfig.LOADER_TYPE_AUDIO
            }
            loaderConfig.mediaLoaderType = type
        }
    }

    /**开始*/
    fun doSelector(fragment: Fragment, loaderConfig: LoaderConfig) {
        fragment.apply {
            if (selectorMode.have(MODE_TAKE) && selectorMode.have(MODE_PICKER)) {
                _selector(fragment, loaderConfig)
            } else if (selectorMode.have(MODE_TAKE)) {
                _take(fragment)
            } else if (selectorMode.have(MODE_PICKER)) {
                _picker(fragment, loaderConfig)
            }
        }
    }

    fun typeAll() = selectorType == TYPE_IMAGE or TYPE_VIDEO
    fun typeOnlyVideo() = selectorType == TYPE_VIDEO
    fun typeOnlyPhoto() = selectorType == TYPE_IMAGE

    fun _selector(fragment: Fragment, loaderConfig: LoaderConfig) {
        fragment.apply {
            context?.itemsDialog {
                addDialogItem {
                    itemText = when {
                        typeAll() -> "拍摄图片和视频"
                        typeOnlyPhoto() -> "拍摄图片"
                        typeOnlyVideo() -> "拍摄视频"
                        else -> "拍摄选择"
                    }
                    itemClick = {
                        _take(fragment)
                    }
                }
                addDialogItem {
                    itemText = when {
                        typeAll() -> "选择图片和视频"
                        typeOnlyPhoto() -> "选择图片"
                        typeOnlyVideo() -> "选择视频"
                        else -> "相册选择"
                    }
                    itemClick = {
                        _picker(fragment, loaderConfig)
                    }
                }
            }
        }
    }

    fun _take(fragment: Fragment) {
        fragment.apply {
            dslAHelper {
                if (typeAll()) {
                    recordVideo(maxRecordTime, minRecordTime) {
                        it?.apply {
                            takeResult(LoaderMedia(localPath = this))
                        }.elseNull {
                            //被取消
                            takeResult(null)
                        }
                    }
                } else if (typeOnlyPhoto()) {
                    recordPhotoOnly {
                        it?.apply {
                            takeResult(LoaderMedia(localPath = this))
                        }.elseNull {
                            //被取消
                            takeResult(null)
                        }
                    }
                } else if (typeOnlyVideo()) {
                    recordVideoOnly(maxRecordTime, minRecordTime) {
                        it?.apply {
                            takeResult(LoaderMedia(localPath = this))
                        }.elseNull {
                            //被取消
                            takeResult(null)
                        }
                    }
                }
            }
        }
    }

    fun _picker(fragment: Fragment, loaderConfig: LoaderConfig) {
        fragment.apply {
            configLoader(loaderConfig)
            dslPicker(loaderConfig) {
                pickerResult(it)
            }
        }
    }
}