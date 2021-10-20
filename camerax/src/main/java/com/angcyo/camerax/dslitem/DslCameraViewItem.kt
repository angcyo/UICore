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

    var cameraLifecycleOwner: LifecycleOwner? = null

    /**camera的生命周期控制*/
    var lifecycleCameraController: LifecycleCameraController? = null

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
            if (lifecycleCameraController == null) {
                lifecycleCameraController = LifecycleCameraController(itemHolder.context)
                lifecycleCameraController?.apply {
                    //setEnabledUseCases(itemEnabledUseCases)
                    cameraView?.controller = this

                    bindToLifecycle(cameraLifecycleOwner ?: this@DslCameraViewItem)
                }
            }

            if (cameraView?.controller != lifecycleCameraController) {
                cameraView?.controller = lifecycleCameraController
            }
        } catch (e: Exception) {
            L.w(e)
        }
    }
}