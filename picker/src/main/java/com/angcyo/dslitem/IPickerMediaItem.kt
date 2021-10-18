package com.angcyo.dslitem

import com.angcyo.core.dslitem.IFragmentItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.library.L
import com.angcyo.library.ex.elseNull
import com.angcyo.library.model.LoaderMedia
import com.angcyo.loader.LoaderConfig

/**
 * 通过[com.angcyo.picker.DslPicker]选择媒体的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IPickerMediaItem : IFragmentItem {

    var pickerMediaItemConfig: PickerMediaItemConfig

    /**调用此方法, 开始选择媒体.
     * 如果配置了多个选择模式, 那么会弹出对话框选择模式*/
    fun startSelectorMedia() {
        itemFragment?.apply {
            pickerMediaItemConfig.itemLoaderConfig.apply(pickerMediaItemConfig.itemUpdateLoaderConfig)

            pickerMediaItemConfig.itemMediaSelectorConfig.takeResult =
                pickerMediaItemConfig.itemTakeResult
            pickerMediaItemConfig.itemMediaSelectorConfig.pickerResult =
                pickerMediaItemConfig.itemPickerResult

            pickerMediaItemConfig.itemLoaderConfig.selectorMediaList =
                pickerMediaItemConfig.itemPickerMediaList

            pickerMediaItemConfig.itemMediaSelectorConfig.doSelector(
                this,
                pickerMediaItemConfig.itemLoaderConfig
            )
        }.elseNull {
            L.w("itemFragment is null, cannot take media.")
        }
    }

}

class PickerMediaItemConfig : IDslItemConfig {

    /**媒体选择时的配置*/
    var itemMediaSelectorConfig: MediaSelectorConfig = MediaSelectorConfig()

    /**媒体加载时的配置*/
    var itemLoaderConfig: LoaderConfig = LoaderConfig()

    var itemUpdateLoaderConfig: (LoaderConfig) -> Unit = {

    }

    /**录像返回*/
    var itemTakeResult: (LoaderMedia?) -> Unit = {

    }

    /**相册选择图片返回*/
    var itemPickerResult: (List<LoaderMedia>?) -> Unit = {

    }

    /**选中的媒体, 放置在此处*/
    var itemPickerMediaList: MutableList<LoaderMedia> = mutableListOf()
}