package com.angcyo.camerax.dslitem

import androidx.camera.view.CameraView
import androidx.lifecycle.LifecycleOwner
import com.angcyo.camerax.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.L
import com.angcyo.widget.DslViewHolder

/**
 * 预览摄像头使用CameraX, 使用拍照和录像模式
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

open class DslCameraViewItem : DslAdapterItem() {

    var cameraLifecycleOwner: LifecycleOwner? = null

    init {
        itemLayoutId = R.layout.lib_item_camera
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        try {
            cameraLifecycleOwner?.run {
                itemHolder.v<CameraView>(R.id.lib_camera_view)?.bindToLifecycle(this)
            }
        } catch (e: Exception) {
            L.w(e)
        }
    }
}