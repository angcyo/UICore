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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
        initCameraXLayout()
    }

    @OverridePoint
    open fun initCameraXLayout() {
        //
        _vh.v<PreviewView>(R.id.lib_camera_view)?.let { view ->
            if (cameraXPreviewControl.havePermission()) {
                cameraXPreviewControl.bindToLifecycle(view, this)
            } else {
                view.post {
                    cameraXPreviewControl.requestPermission {
                        if (it) {
                            cameraXPreviewControl.bindToLifecycle(view, this)
                        }
                    }
                }
            }
        }

        //切换摄像头
        _vh.click(R.id.lib_camera_switch_view) {
            it.isEnabled = cameraXPreviewControl.hasCamera()
            cameraXPreviewControl.switchCamera()
            cameraXPreviewControl.updateImageAnalysisTargetSize()
        }
    }

}