package com.angcyo.camerax.dslitem

import android.annotation.SuppressLint
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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

    /**需要绑定的生命周期, 不指定则使用[DslAdapterItem]的生命周期*/
    var itemCameraLifecycleOwner: LifecycleOwner? = null

    /**camera的生命周期控制管理器*/
    var itemLifecycleCameraController: LifecycleCameraController? = null

    /**需要激活的使用用例*/
    @SuppressLint("UnsafeOptInUsageError")
    var itemEnabledUseCases: Int =
        CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS or CameraController.VIDEO_CAPTURE

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

        val cameraView = itemHolder.v<PreviewView>(R.id.lib_camera_view)
        try {
            if (itemLifecycleCameraController == null) {
                itemLifecycleCameraController = LifecycleCameraController(itemHolder.context)
                itemLifecycleCameraController?.apply {
                    //setEnabledUseCases(itemEnabledUseCases)
                    cameraView?.controller = this

                    bindToLifecycle(itemCameraLifecycleOwner ?: this@DslCameraViewItem)
                }
            }

            if (cameraView?.controller != itemLifecycleCameraController) {
                cameraView?.controller = itemLifecycleCameraController
            }
        } catch (e: Exception) {
            L.w(e)
        }
    }
}