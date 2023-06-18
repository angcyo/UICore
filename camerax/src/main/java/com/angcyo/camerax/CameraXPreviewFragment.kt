package com.angcyo.camerax

import android.os.Bundle
import androidx.camera.view.PreviewView
import com.angcyo.camerax.control.CameraXPreviewControl
import com.angcyo.core.fragment.BaseTitleFragment
import com.angcyo.library.annotation.OverridePoint

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/17
 */
open class CameraXPreviewFragment : BaseTitleFragment() {

    val cameraXPreviewControl: CameraXPreviewControl by lazy {
        CameraXPreviewControl()
    }

    init {
        //fragmentLayoutId = R.layout.lib_camerax_preview_layout
        contentLayoutId = R.layout.lib_camerax_preview_layout
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        initCameraXLayout()
    }

    @OverridePoint
    open fun initCameraXLayout() {
        //
        _vh.v<PreviewView>(R.id.lib_camera_view)?.let {
            cameraXPreviewControl.bindToLifecycle(it, this)
        }
    }

}