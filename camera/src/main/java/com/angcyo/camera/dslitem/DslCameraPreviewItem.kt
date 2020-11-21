package com.angcyo.camera.dslitem

import com.angcyo.camera.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import cool.capturer.android.capturer.CameraView

/**
 * 摄像头预览item, 支持 Camera1 和 Camera2, 使用GL渲染
 * https://github.com/HuaDanJson/CameraCapturer
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/21
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

open class DslCameraPreviewItem : DslAdapterItem() {
    
    init {
        itemLayoutId = R.layout.lib_camera_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        val cameraView: CameraView? = itemHolder.v(R.id.lib_camera_view)
        cameraView?.apply {
            //switchCamera()
        }
    }
}