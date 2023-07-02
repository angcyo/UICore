package com.angcyo.camerax.dslitem

import com.angcyo.camerax.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 * 预览摄像头使用CameraX, 使用拍照和录像模式
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

open class DslCameraXViewItem : DslAdapterItem(), ICameraXItem {

    override var cameraItemConfig: CameraItemConfig = CameraItemConfig()

    init {
        itemLayoutId = R.layout.lib_camerax_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        initCameraXItem(itemHolder, itemPosition, adapterItem, payloads)
    }

}