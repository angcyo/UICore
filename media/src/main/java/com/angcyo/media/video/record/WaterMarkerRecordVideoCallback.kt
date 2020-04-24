package com.angcyo.media.video.record

import android.graphics.Bitmap
import android.view.View
import com.angcyo.library.app
import com.angcyo.library.component.DslWaterMarker
import com.angcyo.library.ex.undefined_res
import com.angcyo.media.video.record.inner.RecordVideoCallback

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class WaterMarkerRecordVideoCallback(
    var waterLayoutId: Int = undefined_res,
    var waterLayoutInit: (rootView: View) -> Unit = {}
) : RecordVideoCallback() {

    /**水印*/
    val waterMarker = DslWaterMarker()

    override fun onTakePhotoBefore(photo: Bitmap, width: Int, height: Int): Bitmap {
        waterMarker.targetBitmap = photo
        waterMarker.waterLayoutId = waterLayoutId
        waterMarker.waterLayoutInit = waterLayoutInit
        waterMarker.outputWidth = width
        waterMarker.outputHeight = height
        return waterMarker.doWaterMarker(app())
    }
}