package com.angcyo.dslitem

import androidx.fragment.app.Fragment
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.L
import com.angcyo.library.ex.elseNull
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
open class DslAddMediaItem : DslAdapterItem() {

    /**用于预览大图*/
    var itemFragment: Fragment? = null

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

    /**配置如何选择媒体信息*/
    val itemMediaSelectorConfig = MediaSelectorConfig()

    /**媒体加载配置*/
    var itemLoaderConfig = LoaderConfig()

    var itemUpdateLoaderConfig: (LoaderConfig) -> Unit = {}

    /**最小和最大录制时长*/
    var itemMinRecordTime: Int = 3

    var itemMaxRecordTime: Int = 15

    /**录像返回*/
    var itemTakeResult: (LoaderMedia?) -> Unit = {

    }

    /**相册选择图片返回*/
    var itemResult: (List<LoaderMedia>?) -> Unit = {

    }

    init {
        itemLayoutId = R.layout.dsl_add_media_item

        itemClick = {
            itemFragment?.apply {
                itemLoaderConfig.apply(itemUpdateLoaderConfig)

                itemMediaSelectorConfig.minRecordTime = itemMinRecordTime
                itemMediaSelectorConfig.maxRecordTime = itemMaxRecordTime
                itemMediaSelectorConfig.takeResult = itemTakeResult
                itemMediaSelectorConfig.pickerResult = itemResult
                itemMediaSelectorConfig.doSelector(this, itemLoaderConfig)
            }.elseNull {
                L.w("itemFragment is null, cannot take media.")
            }
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