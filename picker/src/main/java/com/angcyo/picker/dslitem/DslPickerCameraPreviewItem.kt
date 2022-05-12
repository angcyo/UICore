package com.angcyo.picker.dslitem

import com.angcyo.camera.CameraPreviewView
import com.angcyo.camera.dslitem.DslCameraPreviewItem
import com.angcyo.dsladapter.itemViewHolder
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/21
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

class DslPickerCameraPreviewItem : DslCameraPreviewItem() {

    init {
        itemLayoutId = R.layout.dsl_picker_camera_preview_layout
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
    }

    /**请在完全不使用[DslPickerCameraPreviewItem]时, 调用此方法释放资源*/
    fun release() {
        itemViewHolder()?.v<CameraPreviewView>(R.id.lib_camera_view)?.release()
    }
}