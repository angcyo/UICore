package com.angcyo.camerax

import android.os.Bundle
import com.angcyo.camerax.dslitem.DslCameraXViewItem
import com.angcyo.camerax.dslitem.itemCameraLifecycleOwner
import com.angcyo.camerax.dslitem.itemCameraPriorityUseController
import com.angcyo.core.fragment.BaseTitleFragment

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/17
 */
open class CameraXPreviewFragment : BaseTitleFragment() {

    val cameraItem = DslCameraXViewItem().apply {
        itemCameraPriorityUseController = true
    }

    init {
        //fragmentLayoutId = R.layout.lib_camerax_preview_layout
        contentLayoutId = R.layout.lib_camerax_preview_layout
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        cameraItem.itemCameraLifecycleOwner = this
        _vh.post {
            cameraItem.itemBind(_vh, 0, cameraItem, emptyList())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
    }
}