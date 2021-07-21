package com.angcyo.dslitem

import androidx.fragment.app.Fragment
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.model.LoaderMedia
import com.angcyo.loader.LoaderConfig
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder

/**
 * 添加媒体的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslAddMediaItem : DslAdapterItem(), IPickerMediaItem {

    /**是否使用录制视频和拍照功能, 否则就是选择相册*/
    @Deprecated("请使用[itemMediaSelectorConfig]")
    var itemTakeVideo: Boolean = false
        set(value) {
            field = value
            if (value) {
                itemMediaSelectorConfig.selectorMode = MediaSelectorConfig.MODE_TAKE
            } else {
                itemMediaSelectorConfig.selectorMode = MediaSelectorConfig.MODE_PICKER
            }
        }


    /**最小和最大录制时长, 秒*/
    var itemMinRecordTime: Int = 3

    var itemMaxRecordTime: Int = 15

    override var itemMediaSelectorConfig: MediaSelectorConfig = MediaSelectorConfig()

    override var itemLoaderConfig: LoaderConfig = LoaderConfig()

    override var itemUpdateLoaderConfig: (LoaderConfig) -> Unit = {

    }
    override var itemTakeResult: (LoaderMedia?) -> Unit = {

    }
    override var itemPickerResult: (List<LoaderMedia>?) -> Unit = {

    }
    override var itemPickerMediaList: MutableList<LoaderMedia> = mutableListOf()

    override var itemFragment: Fragment? = null

    init {
        itemLayoutId = R.layout.dsl_add_media_item

        itemClick = {
            itemMediaSelectorConfig.minRecordTime = itemMinRecordTime
            itemMediaSelectorConfig.maxRecordTime = itemMaxRecordTime

            startSelectorMedia()
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.view(R.id.lib_image_view)?.apply {
            setOnClickListener(_clickListener)
        }
    }
}